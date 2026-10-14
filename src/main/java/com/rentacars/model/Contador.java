package com.rentacars.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * Tabla DynamoDB "contadores": reemplaza al BIGSERIAL (id autoincremental) de PostgreSQL.
 *
 * DynamoDB no genera ids. Para mantener ids numericos (1, 2, 3...) como pide la API,
 * hay un item por entidad ("tiendas", "autos"...) con el ultimo id entregado, que se
 * incrementa con una operacion ATOMICA (ADD valor 1): aunque dos peticiones lleguen a la
 * vez, nunca reciben el mismo numero. Ver repository/IdGenerator.java.
 */
@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Contador {

    @Getter(onMethod_ = @DynamoDbPartitionKey)
    private String nombre;

    private Long valor;
}
