package com.rentacars.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HU-15 (Murcia): Actualizar datos de cliente -- implementado por Claude.
 *
 *   PUT /clientes/{id}
 *   { "telefono": "3109876543", "tarjeta_credito": "4222222222222222" }
 *
 * A proposito NO tiene campo "email": la regla del backlog dice
 * "no permitir cambiar el email", asi que ni siquiera se expone en el DTO.
 * Los campos son opcionales: se actualiza solo lo que llegue en el body.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClienteRequest {

    private String telefono;
    private String tarjetaCredito;
}
