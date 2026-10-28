-- Tabela principal de Pedidos
CREATE TABLE pedido (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_pedido VARCHAR(30) NOT NULL UNIQUE,
    cliente_id BIGINT NOT NULL,
    data_pedido DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CRIADO',
    valor_total DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    observacao VARCHAR(255)
);

-- Tabela relacionada de Itens do Pedido (Relacionamento 1:N)
CREATE TABLE item_pedido (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    pedido_id INTEGER NOT NULL REFERENCES pedido(id) ON DELETE CASCADE,
    produto_id BIGINT NOT NULL,
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    preco_unitario DECIMAL(14,2) NOT NULL CHECK (preco_unitario >= 0),
    subtotal DECIMAL(14,2) NOT NULL CHECK (subtotal >= 0)
);

CREATE INDEX ix_item_pedido_id ON item_pedido (pedido_id);
