package loja.estoque.shared.dto;

import loja.estoque.Produto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoDTO(
        Long id,
        String codigo,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer quantidadeEstoque,
        Boolean ativo,
        LocalDateTime criadoEm
) {
    public static ProdutoDTO fromEntity(Produto produto) {
        return new ProdutoDTO(
                produto.getId(),
                produto.getCodigo(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getQuantidadeEstoque(),
                produto.getAtivo(),
                produto.getCriadoEm()
        );
    }
}
