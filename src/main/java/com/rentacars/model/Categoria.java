package com.rentacars.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * Tabla DynamoDB "categorias". Clasifica los autos (SUV, Sedan, Camioneta...).
 *
 * "No se pueden registrar dos categorias con el mismo nombre" (HU-06): en SQL era un UNIQUE.
 * DynamoDB no tiene UNIQUE; se logra con un item "candado" en la tabla "unicos"
 * (ver repository/CategoriaRepository.java).
 */
@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Getter(onMethod_ = @DynamoDbPartitionKey)
    private Long idCategoria;

    private String nombre;

    private String descripcion;
}
