package loja.pedido.shared.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record NovoPedidoRequest(
        @NotNull(message = "O ID do cliente é obrigatório.")
        Long clienteId,

        @NotEmpty(message = "O pedido deve possuir pelo menos um item.")
        @Valid
        List<ItemPedidoRequest> itens,

        String observacao
) {
}
