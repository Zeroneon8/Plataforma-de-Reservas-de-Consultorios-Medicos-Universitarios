// src/main/java/.../security/config/CorsConfig.java
package com.githubzs.plataforma_reservas_medicas.security.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permite peticiones desde el frontend de React (Vite corre en 5173)
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://localhost:3000"   // por si usas otro puerto
        ));
        
        // Métodos HTTP que se permiten
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Headers que el frontend puede enviar (incluye Authorization para el JWT)
        configuration.setAllowedHeaders(List.of("*"));
        
        // Permite enviar cookies y el header Authorization
        configuration.setAllowCredentials(true);
        
        // Aplica esta configuración a TODAS las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}