package loja.estoque;

import loja.estoque.shared.dto.ProdutoDTO;
import loja.estoque.shared.dto.ProdutoRequest;
import loja.estoque.shared.exception.EstoqueInsuficienteException;
import loja.estoque.shared.exception.NegocioException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ProdutoDTO> listarTodos() {
        return repository.findAll().stream()
                .map(ProdutoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProdutoDTO buscarPorId(Long id) {
        Produto produto = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto com ID " + id + " não encontrado."));
        return ProdutoDTO.fromEntity(produto);
    }

    @Transactional(readOnly = true)
    public Produto buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto com ID " + id + " não encontrado."));
    }

    @Transactional
    public ProdutoDTO salvar(ProdutoRequest request) {
        if (repository.existsByCodigo(request.codigo())) {
            throw new NegocioException("Já existe um produto com o código: " + request.codigo());
        }

        Produto produto = new Produto();
        produto.setCodigo(request.codigo());
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setPreco(request.preco());
        produto.setQuantidadeEstoque(request.quantidadeEstoque());
        produto.setAtivo(request.ativo() != null ? request.ativo() : true);

        return ProdutoDTO.fromEntity(repository.save(produto));
    }

    @Transactional
    public ProdutoDTO atualizar(Long id, ProdutoRequest request) {
        Produto existente = buscarEntidadePorId(id);

        if (!existente.getCodigo().equals(request.codigo()) && repository.existsByCodigo(request.codigo())) {
            throw new NegocioException("Já existe outro produto com o código: " + request.codigo());
        }

        existente.setCodigo(request.codigo());
        existente.setNome(request.nome());
        existente.setDescricao(request.descricao());
        existente.setPreco(request.preco());
        existente.setQuantidadeEstoque(request.quantidadeEstoque());
        if (request.ativo() != null) {
            existente.setAtivo(request.ativo());
        }

        return ProdutoDTO.fromEntity(repository.save(existente));
    }

    @Transactional
    public void excluir(Long id) {
        Produto existente = buscarEntidadePorId(id);
        repository.delete(existente);
    }

    /**
     * Realiza a reserva/baixa do estoque.
     * Retorna HTTP 409 em caso de estoque insuficiente.
     */
    @Transactional
    public ProdutoDTO baixarEstoque(Long id, int quantidade) {
        if (quantidade <= 0) {
            throw new NegocioException("A quantidade para baixa deve ser maior que zero.");
        }

        Produto produto = buscarEntidadePorId(id);

        if (!Boolean.TRUE.equals(produto.getAtivo())) {
            throw new NegocioException("Produto ID " + id + " está inativo.");
        }

        if (produto.getQuantidadeEstoque() < quantidade) {
            throw new EstoqueInsuficienteException("Estoque insuficiente para o produto: " + produto.getNome() 
                    + ". Solicitado: " + quantidade + ", Disponível: " + produto.getQuantidadeEstoque());
        }

        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - quantidade);
        return ProdutoDTO.fromEntity(repository.save(produto));
    }

    /**
     * Ação de Compensação (Padrão Saga / Compensating Transaction):
     * Restitui a quantidade ao estoque caso o pedido falhe após a baixa.
     */
    @Transactional
    public void reporEstoque(Long id, int quantidade) {
        if (quantidade <= 0) {
            throw new NegocioException("A quantidade para reposição deve ser maior que zero.");
        }

        Produto produto = buscarEntidadePorId(id);
        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + quantidade);
        repository.save(produto);
    }
}
