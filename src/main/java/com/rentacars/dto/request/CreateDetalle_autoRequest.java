package com.rentacars.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class CreateDetalle_autoRequest {


     //Imagen del vehículo.

    @NotBlank(message = "La imagen es obligatoria")
    private String imagen;



     //Modelo del vehículo.

    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;



     //Marca del vehículo.

    @NotBlank(message = "La marca es obligatoria")
    private String marca;



     //Año del vehículo.

    @NotBlank(message = "El anio es obligatorio")
    private String anio;



     //Placa del vehículo.

    @NotBlank(message = "La placa es obligatoria")
    private String placa;



     //Precio por día.

    @NotNull(message = "El precioDia es obligatorio")
    @DecimalMin("0.01")
    private BigDecimal precioDia;



     // Porcentaje de oferta.

    @NotNull(message = "El ofertaPorcentaje es obligatorio")
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal ofertaPorcentaje;
}
