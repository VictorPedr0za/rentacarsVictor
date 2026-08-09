package com.rentacars.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//valid
import jakarta.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateAutoRequest {

    //valida disponibilidad requerida
    @NotNull(message = "La disponibilidad es requerida")
    private Boolean disponibilidad;

    //valida id de tienda requerido
    @NotNull(message = "El id de la tienda es requerido")
    private Long idTienda;

    //valida id de categoria requerido
    @NotNull(message = "El id de la categoria es requerido")
    private Long idCategoria;

}