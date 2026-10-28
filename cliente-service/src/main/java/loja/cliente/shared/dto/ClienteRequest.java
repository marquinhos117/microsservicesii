package loja.cliente.shared.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ClienteRequest(
        @NotBlank(message = "O nome é obrigatório.")
        String nome,

        @NotBlank(message = "O CPF é obrigatório.")
        String cpf,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Formato de e-mail inválido.")
        String email,

        String telefone,

        @NotNull(message = "O limite de crédito é obrigatório.")
        @PositiveOrZero(message = "O limite de crédito deve ser maior ou igual a zero.")
        BigDecimal limiteCredito,

        Boolean ativo
) {
}
