package com.rentacars.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAutoRequest {

    // HU-11 (Suarez):
    @NotNull (message = "El campo disponibilidad es obligatorio")
    private Boolean disponibilidad;
}
