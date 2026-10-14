package com.rentacars.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tabla DynamoDB "alquileres".
 *
 * SOBRE EL CAMPO estado: solo "ACTIVO" o "CERRADO" (antes lo obligaba un CHECK de PostgreSQL;
 * ahora lo valida la aplicacion).
 *   - HU-18 crea el alquiler con estado = "ACTIVO"
 *   - HU-24 lo pasa a "CERRADO" al registrar la devolucion
 *   - HU-21 lista solo los que estan en "ACTIVO" y no vencidos
 *
 * INDICES (cada uno responde una consulta de la API sin recorrer toda la tabla):
 *   - idCliente-index        -> HU-20 historial de un cliente
 *   - idAuto-index           -> "este auto tiene alquileres?" (al borrar un auto)
 *   - estado-fechaFin-index  -> HU-21 activos: estado = ACTIVO y fechaFin >= hoy
 *                               (estado es la llave de particion y fechaFin la de orden,
 *                                asi DynamoDB filtra por rango de fechas directamente)
 *
 * precioTotal es BigDecimal (dinero). Las fechas se guardan como texto ISO (2026-08-10),
 * que ademas se ordena correctamente.
 */
@DynamoDbBean
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alquiler {

    @Getter(onMethod_ = @DynamoDbPartitionKey)
    private Long idAlquiler;

    @Getter(onMethod_ = @DynamoDbSecondaryPartitionKey(indexNames = "idCliente-index"))
    private Long idCliente;

    @Getter(onMethod_ = @DynamoDbSecondaryPartitionKey(indexNames = "idAuto-index"))
    private Long idAuto;

    private LocalDate fechaInicio;

    @Getter(onMethod_ = @DynamoDbSecondarySortKey(indexNames = "estado-fechaFin-index"))
    private LocalDate fechaFin;

    private BigDecimal precioTotal;

    private String ciudadRetirada;

    private String ciudadDevolucion;

    /** Solo "ACTIVO" o "CERRADO" */
    @Getter(onMethod_ = @DynamoDbSecondaryPartitionKey(indexNames = "estado-fechaFin-index"))
    private String estado;
}
