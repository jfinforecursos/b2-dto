package com.example.apibasica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Básica de Clientes, Pedidos y Productos")
                        .version("1.0.0")
                        .description("API REST para la gestión de Clientes, Pedidos (Relación 1:N) y Productos usando el patrón DTO (Data Transfer Object), con manejo global de excepciones y siembra de datos.")
                        .contact(new Contact()
                                .name("Soporte Técnico")
                                .email("contacto@ejemplo.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
