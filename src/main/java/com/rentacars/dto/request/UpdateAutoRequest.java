package com.rentacars.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAutoRequest {
    @NotNull(message = "El campo disponibilidad es obligatorio")
    private Boolean disponibilidad;
}
