package com.rentacars.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

/**
 * Tabla DynamoDB "clientes".
 *
 * OJO (HU-14, HU-15, HU-16): la tarjeta de credito SE GUARDA aqui, pero NUNCA se devuelve
 * completa en un DTO de respuesta (en HU-16 se muestra enmascarada: ************1111).
 *
 * El email es unico: en SQL era UNIQUE; aqui se garantiza con un item "candado" en la tabla
 * "unicos" (atomico), y el indice "email-index" permite buscar por email.
 */
@DynamoDbBean
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Getter(onMethod_ = @DynamoDbPartitionKey)
    private Long idCliente;

    private String nombre;

    @Getter(onMethod_ = @DynamoDbSecondaryPartitionKey(indexNames = "email-index"))
    private String email;

    private String telefono;

    private String tarjetaCredito;
}
