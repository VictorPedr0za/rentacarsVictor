package com.rentacars.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnoreNulls;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

/**
 * Tabla DynamoDB "autos": la unidad fisica, sus relaciones y su ficha comercial.
 *
 * DE SQL A NOSQL:
 *   - Antes eran DOS tablas (autos + detalles_autos). Ahora es UN documento: la ficha
 *     comercial vive anidada en "detalles" (ver Detalle_auto).
 *   - Las llaves foraneas (idTienda, idCategoria) siguen siendo numeros simples, pero DynamoDB
 *     NO las valida: lo hace AutoServiceImpl. Cada una tiene un indice secundario global
 *     ("idTienda-index", "idCategoria-index") para responder rapido "que autos tiene esta
 *     tienda / categoria" sin recorrer toda la tabla.
 */
@DynamoDbBean
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Auto {

    @Getter(onMethod_ = @DynamoDbPartitionKey)
    private Long idAuto;

    // HU-08 lo pone en true al crear el auto
    private Boolean disponibilidad;

    @Getter(onMethod_ = @DynamoDbSecondaryPartitionKey(indexNames = "idTienda-index"))
    private Long idTienda;

    @Getter(onMethod_ = @DynamoDbSecondaryPartitionKey(indexNames = "idCategoria-index"))
    private Long idCategoria;

    // documento anidado; @DynamoDbIgnoreNulls evita guardar atributos vacios (p. ej. sin oferta)
    @Getter(onMethod_ = @DynamoDbIgnoreNulls)
    private Detalle_auto detalles;
}
