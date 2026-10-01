package com.rentacars.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateDetalle_autoRequest {
    @Positive(message = "El precio por dia debe ser mayor a 0")
    private BigDecimal precioDia;

    @DecimalMin(value = "0.0", message = "La oferta no puede ser negativa")
    @DecimalMax(value = "100.0", message = "La oferta no puede ser mayor a 100")
    private BigDecimal ofertaPorcentaje;

    private String imagen;
}
