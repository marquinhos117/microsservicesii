package loja.pedido;

import loja.pedido.shared.enums.StatusPedido;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "pedido")
@EqualsAndHashCode(exclude = "itens")
@ToString(exclude = "itens")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O número do pedido é obrigatório.")
    @Column(nullable = false, unique = true, length = 30)
    private String numeroPedido;

    // Desacoplamento DDD: Apenas o ID referencial do Bounded Context de Clientes
    @NotNull(message = "O ID do cliente é obrigatório.")
    @Column(nullable = false)
    private Long clienteId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataPedido;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status = StatusPedido.CRIADO;

    @NotNull
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(length = 255)
    private String observacao;

    // Relacionamento 1:N entre tabelas do mesmo microsserviço
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    public void adicionarItem(ItemPedido item) {
        itens.add(item);
        item.setPedido(this);
    }

    public void removerItem(ItemPedido item) {
        itens.remove(item);
        item.setPedido(null);
    }
}
