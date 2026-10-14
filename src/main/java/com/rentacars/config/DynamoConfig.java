package com.rentacars.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;

/**
 * Crea los dos clientes de DynamoDB que usa toda la aplicacion:
 *   - DynamoDbClient          : el cliente "de bajo nivel" (operaciones sueltas: contadores, transacciones).
 *   - DynamoDbEnhancedClient  : el cliente "mejorado" que convierte objetos Java <-> items automaticamente.
 *
 * CREDENCIALES:
 *   - DynamoDB local: cualquier valor sirve, se usan unos de relleno.
 *   - AWS real: el SDK las busca solo, en este orden: variables de entorno AWS_ACCESS_KEY_ID /
 *     AWS_SECRET_ACCESS_KEY, archivo ~/.aws/credentials, o el rol de la maquina (EC2, App Runner...).
 *     Nunca se escriben en el codigo.
 */
@Configuration
@EnableConfigurationProperties(DynamoProperties.class)
public class DynamoConfig {

    @Bean(destroyMethod = "close")
    public DynamoDbClient dynamoDbClient(DynamoProperties props) {
        DynamoDbClientBuilder builder = DynamoDbClient.builder()
                .region(Region.of(props.getRegion()))
                // se fija el cliente HTTP a mano para que no haya ambiguedad si hay mas de uno en el classpath
                .httpClientBuilder(UrlConnectionHttpClient.builder());

        if (props.esLocal()) {
            builder.endpointOverride(URI.create(props.getEndpoint()))
                    .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("local", "local")));
        }
        return builder.build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient cliente) {
        return DynamoDbEnhancedClient.builder().dynamoDbClient(cliente).build();
    }
}
