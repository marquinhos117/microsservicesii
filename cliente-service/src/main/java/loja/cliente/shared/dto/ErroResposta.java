package loja.cliente.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErroResposta {
    private LocalDateTime timestamp;
    private Integer status;
    private String codigo;
    private String mensagem;
    private List<CampoErro> campos;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CampoErro {
        private String campo;
        private String mensagem;
    }
}
