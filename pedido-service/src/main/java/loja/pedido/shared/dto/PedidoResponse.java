package loja.pedido.shared.dto;

import loja.pedido.Pedido;
import loja.pedido.shared.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record PedidoResponse(
        Long id,
        String numeroPedido,
        Long clienteId,
        LocalDateTime dataPedido,
        StatusPedido status,
        BigDecimal valorTotal,
        String observacao,
        List<ItemPedidoResponse> itens
) {
    public static PedidoResponse fromEntity(Pedido pedido) {
        List<ItemPedidoResponse> itensResponse = pedido.getItens() != null
                ? pedido.getItens().stream().map(ItemPedidoResponse::fromEntity).collect(Collectors.toList())
                : List.of();

        return new PedidoResponse(
                pedido.getId(),
                pedido.getNumeroPedido(),
                pedido.getClienteId(),
                pedido.getDataPedido(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getObservacao(),
                itensResponse
        );
    }
}
