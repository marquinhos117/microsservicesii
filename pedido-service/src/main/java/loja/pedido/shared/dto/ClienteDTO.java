package loja.pedido.shared.dto;

import java.math.BigDecimal;

public record ClienteDTO(
        Long id,
        String nome,
        String cpf,
        String email,
        String telefone,
        BigDecimal limiteCredito,
        Boolean ativo
) {
}
