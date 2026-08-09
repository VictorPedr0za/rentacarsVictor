package com.rentacars.dto.request;


import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//valids
import jakarta.validation.constraints.Positive;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAutoRequest {

    //atributos que se van a actualizar

    //disponibilidad no requiere validacion, solo true o false
    // HU-11 (Suarez):
    @NotNull (message = "El campo disponibilidad es obligatorio")
    private Boolean disponibilidad;

    //valida id de tienda positivo
    @Positive(message = "El idTienda debe ser mayor a 0")
    private Long idTienda;

    //valida id de categoria positivo
    @Positive(message = "El idCategoria debe ser mayor a 0")
    private Long idCategoria;

}

