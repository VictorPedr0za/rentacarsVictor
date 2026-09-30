package com.rentacars.dto.request;


import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * HU-10: todos los campos son opcionales (solo se actualizan los que lleguen),
 * pero si llegan deben respetar los CHECK de la BD (precio_dia > 0, oferta entre 0 y 100)
 * para responder 400 y no un error de PostgreSQL.
 */
@Getter
@Setter
public class UpdateDetalle_autoRequest {

    @DecimalMin(value = "0.0", inclusive = false, message = "El precio por dia debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2, message = "El precio por dia admite hasta 8 enteros y 2 decimales")
    private BigDecimal precioDia;

    @DecimalMin(value = "0.0", message = "La oferta no puede ser menor a 0")
    @DecimalMax(value = "100.0", message = "La oferta no puede ser mayor a 100")
    @Digits(integer = 3, fraction = 2, message = "La oferta admite hasta 2 decimales")
    private BigDecimal ofertaPorcentaje;

    @Size(max = 2000, message = "La imagen soporta hasta 2000 caracteres")
    private String imagen;


}
