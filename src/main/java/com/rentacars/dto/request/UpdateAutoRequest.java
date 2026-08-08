package com.rentacars.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//valids
import jakarta.validation.constraints.Positive;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAutoRequest {

    //atributos que se van a actualizar

    //disponibilidad no requiere validacion, solo true o false
    private Boolean disponibilidad;

    //valida id de tienda positivo
    @Positive(message = "El idTienda debe ser mayor a 0")
    private Long idTienda;

    //valida id de categoria positivo
    @Positive(message = "El idCategoria debe ser mayor a 0")
    private Long idCategoria;

}