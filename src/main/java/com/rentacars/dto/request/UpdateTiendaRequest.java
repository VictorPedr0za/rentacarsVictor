package com.rentacars.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HU-02 (Arango): actualizar tienda -- implementado por Claude.
 *
 * A diferencia de CreateTiendaRequest, aqui los campos son OPCIONALES
 * (sin @NotBlank): la regla de negocio dice "actualizar solo los campos
 * que lleguen en el body", asi que el cliente puede mandar solo uno.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTiendaRequest {

    private String nombre;
    private String ciudad;
    private String direccion;
}
