package loja.pedido.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pedido Service API - Loja")
                        .version("1.0.0")
                        .description("Microsserviço de Pedidos com orquestração REST, tabelas 1:N relacionadas e transação compensatória (Saga)."));
    }
}
