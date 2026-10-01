package com.rentacars.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AutoSearchResponse {
    private Long idAuto;
    private String modelo;
    private String marca;
    private BigDecimal precioDia;
    private BigDecimal ofertaPorcentaje;
    private Boolean disponibilidad;
}
