package com.rentacars.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Valores de "app.dynamodb.*" en application.properties (o variables de entorno).
 *
 *   endpoint     vacio  -> DynamoDB REAL en AWS (usa las credenciales de AWS de tu entorno)
 *                con URL -> DynamoDB LOCAL, por ejemplo http://localhost:8000
 *   region       region de AWS (us-east-1 = N. Virginia)
 *   tablePrefix  prefijo de los nombres de tabla; permite varias copias (dev, pruebas...) en una cuenta
 *   autoCreate   crea las tablas al arrancar si no existen
 *   seed         carga datos de ejemplo si las tablas estan vacias
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.dynamodb")
public class DynamoProperties {

    private String region = "us-east-1";

    private String endpoint = "";

    private String tablePrefix = "rentacars_";

    private boolean autoCreate = true;

    private boolean seed = true;

    public boolean esLocal() {
        return endpoint != null && !endpoint.isBlank();
    }
}
