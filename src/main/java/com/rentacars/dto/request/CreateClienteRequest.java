package com.rentacars.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateClienteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre soporta hasta 100 caracteres")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 150, message = "El email soporta hasta 150 caracteres")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20, message = "El teléfono soporta hasta 20 caracteres")
    private String telefono;

    @NotBlank(message = "La tarjeta de crédito es obligatoria")
    @Size(max = 20, message = "La tarjeta de crédito soporta hasta 20 caracteres")
    private String tarjetaCredito;

}
