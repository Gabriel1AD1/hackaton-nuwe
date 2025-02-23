package com.hackathon.blockchain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Especifica los orígenes permitidos
        config.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000", "http://localhost:8080"));
        // Permite solo los métodos necesarios
        config.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));
        // Limita los encabezados permitidos
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        // Revisa si necesitas compartir credenciales
        config.setAllowCredentials(true);
        // Exponer solo los encabezados necesarios
        config.setExposedHeaders(Collections.singletonList("userSession"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
