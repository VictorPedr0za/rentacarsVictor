package com.rentacars.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PUT /categorias/{id}: ambos campos son opcionales, solo se actualiza el que llegue.
 * Si llegan no pueden estar en blanco y deben caber en la columna (nombre VARCHAR(50)).
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateCategoriaRequest {

    @Size(min = 1, max = 50, message = "El nombre debe tener entre 1 y 50 caracteres")
    private String nombre;

    @Size(min = 1, message = "La descripcion no puede estar vacia")
    private String descripcion;
}
