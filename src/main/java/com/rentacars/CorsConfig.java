package com.rentacars;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE); // se ejecuta antes que Security
        return bean;
    }
}
//package com.rentacars;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
///**
// * Habilita CORS para que el frontend de React (que corre en otro puerto,
// * ej. http://localhost:5173 con "npm run dev") pueda llamar a esta API.
// *
// * Sin esta clase, el navegador BLOQUEA las peticiones del frontend con un
// * error de "CORS policy" en la consola, aunque el backend este funcionando
// * perfectamente (Postman/curl si funcionarian, porque esta politica solo
// * la aplica el navegador).
// *
// * COMO USARLA:
// * 1. Copia este archivo a: src/main/java/com/rentacars/config/CorsConfig.java
// *    (crea la carpeta "config" si no existe).
// * 2. Si el frontend corre en otro puerto o dominio, agregalo en allowedOrigins.
// * 3. Vuelve a arrancar el backend (o reconstruye la imagen Docker si lo corres
// *    en contenedor) para que el cambio tome efecto.
// */

//@Configuration
//public class CorsConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins(
//                        "http://localhost:5173",  // npm run dev (Vite)
//                        "http://localhost:4173"   // npm run preview (build local)
//                )
//                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
//                .allowedHeaders("*");
//    }
//}
