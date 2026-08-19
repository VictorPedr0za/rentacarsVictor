package com.rentacars.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HU-14 (Murcia): Registrar cliente -- implementado por Claude.
 *
 *   POST /clientes
 *   { "nombre": "Juan Perez", "email": "juan@email.com",
 *     "telefono": "3001234567", "tarjeta_credito": "4111111111111111" }
 *
 * Todos los campos son obligatorios (regla del backlog). Si falta alguno,
 * el @Valid del controller + estas anotaciones responden 400 solas.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateClienteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato valido")
    private String email;

    @NotBlank(message = "El telefono es obligatorio")
    private String telefono;

    @NotBlank(message = "La tarjeta de credito es obligatoria")
    private String tarjetaCredito;
}
