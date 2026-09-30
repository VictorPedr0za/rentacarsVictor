package com.rentacars.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * HU-15: solo se pueden cambiar los datos de contacto y la tarjeta.
 * NO lleva email (no se permite cambiarlo) ni nombre: si llegan en el JSON se ignoran.
 *
 * Ambos campos son opcionales: solo se actualiza el que llegue.
 * Si llegan, no pueden estar en blanco y deben caber en la columna (VARCHAR(20)).
 */
@Data
public class UpdateClienteRequest {

    @Size(min = 1, max = 20, message = "El telefono debe tener entre 1 y 20 caracteres")
    private String telefono;

    @Size(min = 1, max = 20, message = "La tarjeta de credito debe tener entre 1 y 20 caracteres")
    private String tarjetaCredito;

}
