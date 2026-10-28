package loja.cliente;

import loja.cliente.shared.dto.ClienteDTO;
import loja.cliente.shared.dto.ClienteRequest;
import loja.cliente.shared.exception.NegocioException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> listarTodos() {
        return repository.findAll().stream()
                .map(ClienteDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClienteDTO buscarPorId(Long id) {
        Cliente cliente = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente com ID " + id + " não encontrado."));
        return ClienteDTO.fromEntity(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente com ID " + id + " não encontrado."));
    }

    @Transactional
    public ClienteDTO salvar(ClienteRequest request) {
        if (repository.existsByCpf(request.cpf())) {
            throw new NegocioException("Já existe um cliente cadastrado com o CPF: " + request.cpf());
        }
        if (repository.existsByEmail(request.email())) {
            throw new NegocioException("Já existe um cliente cadastrado com o e-mail: " + request.email());
        }

        Cliente cliente = new Cliente();
        cliente.setNome(request.nome());
        cliente.setCpf(request.cpf());
        cliente.setEmail(request.email());
        cliente.setTelefone(request.telefone());
        cliente.setLimiteCredito(request.limiteCredito());
        cliente.setAtivo(request.ativo() != null ? request.ativo() : true);

        return ClienteDTO.fromEntity(repository.save(cliente));
    }

    @Transactional
    public ClienteDTO atualizar(Long id, ClienteRequest request) {
        Cliente existente = buscarEntidadePorId(id);

        if (!existente.getCpf().equals(request.cpf()) && repository.existsByCpf(request.cpf())) {
            throw new NegocioException("Já existe outro cliente com o CPF: " + request.cpf());
        }
        if (!existente.getEmail().equals(request.email()) && repository.existsByEmail(request.email())) {
            throw new NegocioException("Já existe outro cliente com o e-mail: " + request.email());
        }

        existente.setNome(request.nome());
        existente.setCpf(request.cpf());
        existente.setEmail(request.email());
        existente.setTelefone(request.telefone());
        existente.setLimiteCredito(request.limiteCredito());
        if (request.ativo() != null) {
            existente.setAtivo(request.ativo());
        }

        return ClienteDTO.fromEntity(repository.save(existente));
    }

    @Transactional
    public void excluir(Long id) {
        Cliente existente = buscarEntidadePorId(id);
        repository.delete(existente);
    }
}
