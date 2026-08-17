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
public class CreateAutoRequest {


     //Modelo del auto.

    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;



     //Marca del auto.

    @NotBlank(message = "La marca es obligatoria")
    private String marca;



     //Año del auto.

    @NotBlank(message = "El anio es obligatorio")
    private String anio;



     //Placa del auto.

    @NotBlank(message = "La placa es obligatoria")
    private String placa;



     // Precio por día.

    @NotNull(message = "El precioDia es obligatorio")
    @DecimalMin(
            value = "0.01",
            message = "El precioDia debe ser mayor a 0"
    )
    private BigDecimal precioDia;


    // Porcentaje de oferta.

    @NotNull(message = "El ofertaPorcentaje es obligatorio")
    @DecimalMin(
            value = "0.0",
            message = "La oferta no puede ser menor a 0"
    )
    @DecimalMax(
            value = "100.0",
            message = "La oferta no puede ser mayor a 100"
    )
    private BigDecimal ofertaPorcentaje;



     //Imagen del auto.

    @NotBlank(message = "La imagen es obligatoria")
    private String imagen;



     //ID de la tienda donde se registrará

    @NotNull(message = "El idTienda es obligatorio")
    private Long idTienda;



     //ID de la categoría del auto.

    @NotNull(message = "El idCategoria es obligatorio")
    private Long idCategoria;
}
