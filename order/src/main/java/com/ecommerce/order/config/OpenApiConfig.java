package com.ecommerce.order.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderOpenAPI() {
        Contact contact = new Contact()
                .name("Integration Team")
                .email("integration@ecommerce.com");

        Info info = new Info()
                .title("Order Service API")
                .description("REST API for managing orders in the eCommerce platform")
                .version("1.0.0")
                .contact(contact);

        Server server = new Server()
                .url("http://localhost:8081")
                .description("Local server");

        OpenAPI openAPI = new OpenAPI()
                .info(info)
                .addServersItem(server);

        return openAPI;
    }
}