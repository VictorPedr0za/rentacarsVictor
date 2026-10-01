package com.rentacars.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AutoUpdateDetailResponse {
    private Long idAuto;
    private String modelo;
    private BigDecimal precioDia;
    private BigDecimal ofertaPorcentaje;
    private String imagen;
}
