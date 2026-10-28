package loja.cliente.shared.dto;

import loja.cliente.Cliente;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClienteDTO(
        Long id,
        String nome,
        String cpf,
        String email,
        String telefone,
        BigDecimal limiteCredito,
        Boolean ativo,
        LocalDateTime criadoEm
) {
    public static ClienteDTO fromEntity(Cliente cliente) {
        return new ClienteDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getEmail(),
                cliente.getTelefone(),
                cliente.getLimiteCredito(),
                cliente.getAtivo(),
                cliente.getCriadoEm()
        );
    }
}
