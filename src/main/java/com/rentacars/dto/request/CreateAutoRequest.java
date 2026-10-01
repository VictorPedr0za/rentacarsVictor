package com.rentacars.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CreateAutoRequest {
    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotBlank(message = "El anio es obligatorio")
    @Size(min = 4, max = 4, message = "El anio debe tener 4 caracteres")
    private String anio;

    @NotBlank(message = "La placa es obligatoria")
    private String placa;

    @NotNull(message = "El precio por dia es obligatorio")
    @Positive(message = "El precio por dia debe ser mayor a 0")
    private BigDecimal precioDia;

    @NotNull(message = "La oferta es obligatoria")
    @DecimalMin(value = "0.0", message = "La oferta no puede ser negativa")
    @DecimalMax(value = "100.0", message = "La oferta no puede ser mayor a 100")
    private BigDecimal ofertaPorcentaje;

    @NotBlank(message = "La imagen es obligatoria")
    private String imagen;

    @NotNull(message = "El id de la tienda es obligatorio")
    @Positive(message = "El id de la tienda debe ser mayor a 0")
    private Long idTienda;

    @NotNull(message = "El id de la categoria es obligatorio")
    @Positive(message = "El id de la categoria debe ser mayor a 0")
    private Long idCategoria;
}
