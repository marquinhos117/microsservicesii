-- Inserção de Pedidos de Teste
INSERT INTO pedido (numero_pedido, cliente_id, data_pedido, status, valor_total, observacao) VALUES
('PED-2026-0001', 1, '2026-10-27 10:00:00', 'FINALIZADO', 650.00, 'Pedido inicial de teste via integracao'),
('PED-2026-0002', 2, '2026-10-27 11:30:00', 'CRIADO', 890.00, 'Compra de monitor gamer');

-- Inserção de Itens relacionados aos Pedidos
INSERT INTO item_pedido (pedido_id, produto_id, quantidade, preco_unitario, subtotal) VALUES
(1, 1, 2, 250.00, 500.00), -- 2 Teclados Mecanicos
(1, 2, 1, 150.00, 150.00), -- 1 Mouse Gamer
(2, 3, 1, 890.00, 890.00); -- 1 Monitor 24 Pol
