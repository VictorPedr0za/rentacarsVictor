package com.rentacars.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AutoCreateResponse {
    private Long idAuto;
    private Boolean disponibilidad;
    private Long idTienda;
    private Long idCategoria;
    private Detalles detalles;

    @Getter
    @Builder
    public static class Detalles {
        private String modelo;
        private BigDecimal precioDia;
        private BigDecimal ofertaPorcentaje;
    }
}
