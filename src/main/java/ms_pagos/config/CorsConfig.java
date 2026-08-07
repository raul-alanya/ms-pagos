package ms_pagos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SEC-IZI-008: Configuración CORS.
 * Permite solicitudes desde el frontend de onboarding y todos los dominios del proyecto.
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(
                                // Dominios Cloudflare de producción
                                "https://*.sistemas9noa.com",
                                "https://sistemas9noa.com",
                                // IPs del servidor (acceso directo)
                                "http://192.171.100.23",
                                "http://192.171.100.23:*",
                                // Desarrollo local
                                "http://localhost:*",
                                "http://127.0.0.1:*"
                        )
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Content-Type", "Cache-Control")
                        .allowCredentials(false)
                        .maxAge(3600);
            }
        };
    }
}
