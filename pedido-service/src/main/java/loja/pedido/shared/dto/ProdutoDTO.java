package loja.pedido.shared.dto;

import java.math.BigDecimal;

public record ProdutoDTO(
        Long id,
        String codigo,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer quantidadeEstoque,
        Boolean ativo
) {
}
