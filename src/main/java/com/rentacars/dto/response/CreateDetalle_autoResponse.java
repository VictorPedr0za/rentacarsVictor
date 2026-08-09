package com.rentacars.dto.response;


import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HU-12 (Cardona) -> detalle completo de un auto (autos + detalles_autos combinados)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDetalle_autoResponse {

    private Long idAuto;
    private String modelo;
    private String marca;
    private String anio;
    private String placa;
    private BigDecimal precioDia;
    private BigDecimal ofertaPorcentaje;
    private BigDecimal precioConOferta;
    private Boolean disponibilidad;

}
