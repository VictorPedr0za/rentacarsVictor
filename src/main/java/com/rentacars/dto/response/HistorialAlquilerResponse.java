package com.rentacars.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class HistorialAlquilerResponse {
    private Long idAlquiler;
    private Long idAuto;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal precioTotal;
    private String ciudadRetirada;
    private String ciudadDevolucion;
}
