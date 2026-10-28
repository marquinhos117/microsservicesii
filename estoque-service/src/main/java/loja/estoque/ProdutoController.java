package loja.estoque;

import loja.estoque.shared.dto.ProdutoDTO;
import loja.estoque.shared.dto.ProdutoRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProdutoDTO>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<ProdutoDTO> salvar(@Valid @RequestBody ProdutoRequest request) {
        ProdutoDTO criado = service.salvar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoDTO> atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint de Baixa de Estoque chamado pelo PedidoService
     */
    @PostMapping("/{id}/baixa-estoque")
    public ResponseEntity<ProdutoDTO> baixarEstoque(@PathVariable Long id, @RequestParam int quantidade) {
        ProdutoDTO atualizado = service.baixarEstoque(id, quantidade);
        return ResponseEntity.ok(atualizado);
    }

    /**
     * Endpoint de Compensação (Saga) chamado pelo PedidoService em caso de rollback
     */
    @PostMapping("/{id}/repor-estoque")
    public ResponseEntity<Void> reporEstoque(@PathVariable Long id, @RequestParam int quantidade) {
        service.reporEstoque(id, quantidade);
        return ResponseEntity.ok().build();
    }
}
