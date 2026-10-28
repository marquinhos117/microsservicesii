package loja.pedido;

import loja.pedido.shared.dto.*;
import loja.pedido.shared.enums.StatusPedido;
import loja.pedido.shared.exception.NegocioException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository repository;
    private final RestClient clienteClient;
    private final RestClient produtoClient;

    public PedidoService(PedidoRepository repository,
                         @Qualifier("clienteClient") RestClient clienteClient,
                         @Qualifier("produtoClient") RestClient produtoClient) {
        this.repository = repository;
        this.clienteClient = clienteClient;
        this.produtoClient = produtoClient;
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodos() {
        return repository.findAll().stream()
                .map(PedidoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long id) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido com ID " + id + " não encontrado."));
        return PedidoResponse.fromEntity(pedido);
    }

    /**
     * Orquestração de Criação de Pedido:
     * 1. Consulta e validação remota de Cliente
     * 2. Consulta de Preços e Baixa no Estoque (estoque-service)
     * 3. Persistência de Pedido + Itens (1:N)
     * 4. Compensação automática (Rollback Saga) caso ocorra qualquer erro
     */
    @Transactional
    public PedidoResponse criarPedido(NovoPedidoRequest request) {
        log.info("Iniciando processo de criacao de pedido para o cliente ID: {}", request.clienteId());

        // 1. Valida se o cliente existe e está ativo via cliente-service
        ClienteDTO cliente = consultarCliente(request.clienteId());
        if (!Boolean.TRUE.equals(cliente.ativo())) {
            throw new NegocioException("O cliente " + cliente.nome() + " está inativo e não pode efetuar compras.");
        }

        // Lista para rastrear produtos que tiveram baixa com sucesso para eventual compensação
        List<ItemPedidoRequest> itensBaixados = new ArrayList<>();
        Pedido pedido = new Pedido();
        pedido.setNumeroPedido("PED-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        pedido.setClienteId(cliente.id());
        pedido.setStatus(StatusPedido.FINALIZADO);
        pedido.setObservacao(request.observacao());

        BigDecimal totalPedido = BigDecimal.ZERO;

        try {
            // 2. Itera sobre cada item, obtém preço atual e solicita baixa de estoque
            for (ItemPedidoRequest itemReq : request.itens()) {
                ProdutoDTO produto = consultarProduto(itemReq.produtoId());

                // Solicita baixa do estoque
                realizarBaixaEstoque(produto, itemReq.quantidade());
                itensBaixados.add(itemReq);

                // Constrói o item do pedido
                BigDecimal precoUnitario = produto.preco();
                BigDecimal subtotal = precoUnitario.multiply(BigDecimal.valueOf(itemReq.quantidade()));

                ItemPedido itemPedido = new ItemPedido();
                itemPedido.setProdutoId(produto.id());
                itemPedido.setQuantidade(itemReq.quantidade());
                itemPedido.setPrecoUnitario(precoUnitario);
                itemPedido.setSubtotal(subtotal);

                pedido.adicionarItem(itemPedido);
                totalPedido = totalPedido.add(subtotal);
            }

            pedido.setValorTotal(totalPedido);

            // 3. Salva o pedido e os itens no banco local
            // saveAndFlush força o INSERT agora, para que erros de banco caiam no catch e acionem a compensação
            Pedido salvo = repository.saveAndFlush(pedido);
            log.info("Pedido {} criado com sucesso! Total: R$ {}", salvo.getNumeroPedido(), salvo.getValorTotal());
            return PedidoResponse.fromEntity(salvo);

        } catch (RuntimeException ex) {
            log.error("Erro durante a execucao do pedido. Iniciando compensacao de estoque: {}", ex.getMessage());
            // Executa compensação restituindo os itens que já haviam sido debitados
            executarCompensacao(itensBaixados);
            throw ex;
        }
    }

    /**
     * Consulta remota ao cliente-service via RestClient
     */
    private ClienteDTO consultarCliente(Long clienteId) {
        try {
            return clienteClient.get()
                    .uri("/api/clientes/{id}", clienteId)
                    .retrieve()
                    .body(ClienteDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NegocioException("Cliente com ID " + clienteId + " não foi encontrado no microsserviço de clientes.");
        } catch (ResourceAccessException e) {
            throw e; // serviço fora do ar -> tratado como HTTP 503 no ApiExceptionHandler
        } catch (Exception e) {
            throw new NegocioException("Erro ao comunicar com o serviço de clientes: " + e.getMessage());
        }
    }

    /**
     * Consulta remota ao estoque-service via RestClient
     */
    private ProdutoDTO consultarProduto(Long produtoId) {
        try {
            return produtoClient.get()
                    .uri("/api/produtos/{id}", produtoId)
                    .retrieve()
                    .body(ProdutoDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NegocioException("Produto com ID " + produtoId + " não encontrado no estoque.");
        } catch (ResourceAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new NegocioException("Erro ao comunicar com o serviço de estoque: " + e.getMessage());
        }
    }

    /**
     * Chamada remota para debitar estoque
     */
    private void realizarBaixaEstoque(ProdutoDTO produto, int quantidade) {
        try {
            produtoClient.post()
                    .uri("/api/produtos/{id}/baixa-estoque?quantidade={quantidade}", produto.id(), quantidade)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict e) {
            throw new NegocioException("Estoque insuficiente para o produto '" + produto.nome() + "'.");
        } catch (ResourceAccessException e) {
            throw e;
        } catch (Exception e) {
            throw new NegocioException("Falha ao dar baixa de estoque para o produto '" + produto.nome() + "': " + e.getMessage());
        }
    }

    /**
     * Transação Compensatória (Saga / Rollback distribuído)
     */
    private void executarCompensacao(List<ItemPedidoRequest> itensBaixados) {
        for (ItemPedidoRequest item : itensBaixados) {
            try {
                log.warn("Compensando estoque: devolvendo {} unidades do produto ID {}", item.quantidade(), item.produtoId());
                produtoClient.post()
                        .uri("/api/produtos/{id}/repor-estoque?quantidade={quantidade}", item.produtoId(), item.quantidade())
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception e) {
                log.error("FALHA GRAVE NA COMPENSACAO do produto ID {}: {}", item.produtoId(), e.getMessage());
            }
        }
    }

    @Transactional
    public void cancelarPedido(Long id) {
        Pedido pedido = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido com ID " + id + " não encontrado."));

        if (pedido.getStatus() == StatusPedido.CANCELADO) {
            throw new NegocioException("Este pedido já se encontra cancelado.");
        }

        // Ao cancelar, repõe o estoque de todos os itens do pedido
        for (ItemPedido item : pedido.getItens()) {
            try {
                produtoClient.post()
                        .uri("/api/produtos/{id}/repor-estoque?quantidade={quantidade}", item.getProdutoId(), item.getQuantidade())
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception e) {
                log.error("Erro ao repor estoque no cancelamento do pedido: {}", e.getMessage());
            }
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        repository.save(pedido);
    }
}
