package loja.pedido;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "item_pedido")
@EqualsAndHashCode(exclude = "pedido")
@ToString(exclude = "pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento N:1 com a entidade pai Pedido
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    // Desacoplamento DDD: Apenas o ID referencial do Bounded Context de Estoque
    @NotNull(message = "O ID do produto é obrigatório.")
    @Column(nullable = false)
    private Long produtoId;

    @NotNull(message = "A quantidade é obrigatória.")
    @Positive(message = "A quantidade deve ser maior que zero.")
    @Column(nullable = false)
    private Integer quantidade;

    @NotNull(message = "O preço unitário é obrigatório.")
    @PositiveOrZero(message = "O preço unitário não pode ser negativo.")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precoUnitario;

    @NotNull(message = "O subtotal é obrigatório.")
    @PositiveOrZero(message = "O subtotal não pode ser negativo.")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;
}
