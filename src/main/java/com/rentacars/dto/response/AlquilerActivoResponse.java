package com.rentacars.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class AlquilerActivoResponse {
    private Long idAlquiler;
    private Long idCliente;
    private Long idAuto;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String ciudadRetirada;
    private BigDecimal precioTotal;
}
