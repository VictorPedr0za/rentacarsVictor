package com.rentacars.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateClienteRequest {
    @Size(min = 1, max = 20, message = "El telefono debe tener entre 1 y 20 caracteres")
    private String telefono;

    @Size(min = 4, max = 20, message = "La tarjeta de credito debe tener entre 4 y 20 caracteres")
    private String tarjetaCredito;
}
