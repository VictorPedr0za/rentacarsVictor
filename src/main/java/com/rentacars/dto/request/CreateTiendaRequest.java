package com.rentacars.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PLANTILLA DE DTO DE ENTRADA -- copien este patron para las demas HU.
 *
 * Un "Request" representa el JSON que ENTRA a la API:
 *
 *     POST /tiendas
 *     { "nombre": "Tienda Norte", "ciudad": "Bogota", "direccion": "Calle 100 #15-20" }
 *
 * Las anotaciones @NotBlank hacen la validacion solas. Si alguien manda
 * el JSON sin "nombre", el GlobalExceptionHandler responde 400 Bad Request
 * sin que nosotros escribamos un solo if. Para que funcionen, el controller
 * debe marcar el parametro con @Valid.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTiendaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La ciudad es obligatoria")
    private String ciudad;

    @NotBlank(message = "La direccion es obligatoria")
    private String direccion;
}
