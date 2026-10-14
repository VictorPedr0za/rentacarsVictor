package com.rentacars.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

/**
 * Tabla DynamoDB "tiendas".
 *
 * DE SQL A NOSQL:
 *   - Antes: @Entity + @Table (una fila de PostgreSQL). Ahora: @DynamoDbBean (un "ítem" / documento).
 *   - La llave primaria (partition key) es idTienda. DynamoDB NO la genera sola: la asigna
 *     IdGenerator (ver repository/IdGenerator.java).
 *   - ciudadNormalizada es una COPIA de "ciudad" en minusculas. Existe solo para poder buscar
 *     "bogota" y encontrar "Bogota" sin importar mayusculas: en DynamoDB no hay ILIKE ni
 *     LOWER() en la consulta, asi que el dato se guarda ya preparado para la consulta
 *     (indice secundario global "ciudad-index").
 */
@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tienda {

    @Getter(onMethod_ = @DynamoDbPartitionKey)
    private Long idTienda;

    private String nombre;

    private String ciudad;

    private String direccion;

    @Getter(onMethod_ = @DynamoDbSecondaryPartitionKey(indexNames = "ciudad-index"))
    private String ciudadNormalizada;
}
