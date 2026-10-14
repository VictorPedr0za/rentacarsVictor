package com.rentacars.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS: que paginas (origenes) pueden llamar a la API desde el navegador.
 *
 * Si el frontend lo sirve el propio backend (http://localhost:8080/) no hay CORS porque es el mismo
 * origen. CORS solo importa cuando el frontend corre aparte (nginx en :8081, servidor de pruebas...).
 *
 * Por defecto se aceptan localhost y 127.0.0.1 en CUALQUIER puerto (antes solo el 8081, y abrir el
 * frontend con otro puerto o con 127.0.0.1 hacia que el navegador bloqueara las llamadas).
 * Para una pagina publicada (por ejemplo en AWS) agrega su direccion en app.cors.allowed-origins
 * o con la variable APP_CORS_ALLOWED_ORIGINS, separadas por coma.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:}")
    private String[] origenesExtra;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedOrigins(origenesExtra)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
