package com.rentacars.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * HU-08 (Cifuentes) -> idAuto, disponibilidad, idTienda, idCategoria, detalles
 * HU-09 (Suarez) -> modelo, marca, precioDia, ofertaPorcentaje
 * HU-11 (Suarez) -> idAuto, disponibilidad
 *
 * Los campos en null no salen en el JSON (non_null), asi que cada HU
 * solo devuelve los que llena.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAutoResponse {

    //HU-09 (Suarez)
    private String modelo;
    private String marca;
    private BigDecimal precioDia;
    private BigDecimal ofertaPorcentaje;

    //HU-10 (Suarez)
    private String imagen;

    //HU-11(Suarez)
    private Long idAuto;
    private Boolean disponibilidad;
    private Long idTienda;
    private Long idCategoria;

    //HU-08 (Cifuentes): ficha comercial guardada en detalles_autos
    private CreateDetalle_autoResponse detalles;

}
