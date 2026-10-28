package loja.pedido.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestConfig {

    @Bean
    public RestClient clienteClient(@Value("${cliente.service.url:http://localhost:8081}") String url) {
        return RestClient.builder().baseUrl(url).build();
    }

    @Bean
    public RestClient produtoClient(@Value("${estoque.service.url:http://localhost:8082}") String url) {
        return RestClient.builder().baseUrl(url).build();
    }
}
