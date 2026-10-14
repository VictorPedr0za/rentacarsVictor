package com.rentacars.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.math.BigDecimal;

/**
 * Ficha comercial del vehiculo: marca, modelo, anio, placa, precio por dia, oferta e imagen.
 *
 * DE SQL A NOSQL (desnormalizar):
 *   Antes era la tabla "detalles_autos", enlazada a "autos" por id_auto, y cada consulta
 *   necesitaba un JOIN o dos lecturas. Ahora es un DOCUMENTO ANIDADO dentro del item Auto:
 *
 *       Auto { idAuto, disponibilidad, idTienda, idCategoria,
 *              detalles: { marca, modelo, anio, placa, precioDia, ofertaPorcentaje, imagen } }
 *
 *   Una sola lectura trae el auto completo y crearlo es una sola escritura atomica
 *   (ya no hacen falta dos INSERT ni @Transactional).
 *
 * SOBRE BigDecimal: precioDia y ofertaPorcentaje son dinero/porcentajes, asi que se usa
 * BigDecimal (no double). DynamoDB los guarda como numero.
 * ofertaPorcentaje PUEDE SER NULL (auto sin oferta): siempre revisar null antes de calcular.
 */
@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Detalle_auto {

    private String imagen;

    private String modelo;

    private String marca;

    private String anio;

    private String placa;

    private BigDecimal precioDia;

    private BigDecimal ofertaPorcentaje;
}
