package com.rentacars.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * HU-08: ficha comercial del auto (tabla detalles_autos).
 *
 * CreateAutoRequest la extiende para que el JSON de POST /autos sea plano,
 * como pide el backlog. Las validaciones se heredan: basta con @Valid en el controller.
 *
 * oferta_porcentaje e imagen son opcionales: las columnas admiten null
 * (un auto puede no tener oferta ni foto).
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateDetalle_autoRequest {

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 50, message = "El modelo soporta hasta 50 caracteres")
    private String modelo;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50, message = "La marca soporta hasta 50 caracteres")
    private String marca;

    @NotBlank(message = "El anio es obligatorio")
    @Pattern(regexp = "\\d{4}", message = "El anio debe tener 4 digitos")
    private String anio;

    @NotBlank(message = "La placa es obligatoria")
    @Size(max = 20, message = "La placa soporta hasta 20 caracteres")
    private String placa;

    @NotNull(message = "El precio por dia es obligatorio")
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
