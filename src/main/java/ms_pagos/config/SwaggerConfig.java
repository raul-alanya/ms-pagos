package ms_pagos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Microservicio de Pagos Online - IZIPAY")
                        .description("API REST para la gestión de pagos online mediante la pasarela IZIPAY. " +
                                "Permite registrar configuraciones, generar tokens de comunicación, " +
                                "procesar pagos, registrar respuestas y gestionar rechazos.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo")
                                .email("desarrollo@pagos.com"))
                        .license(new License()
                                .name("MIT License")));
    }
}
