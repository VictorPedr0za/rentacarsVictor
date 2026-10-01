package com.rentacars.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AutoDetailResponse {
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
