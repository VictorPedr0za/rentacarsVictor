package com.rentacars.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateClienteResponse {

    private Long idCliente;
    private String nombre;
    private String email;
    private String telefono;

    /**
     * SOLO lo llena HU-16 (listar clientes) y siempre ENMASCARADA (************1111).
     * En HU-14 y HU-15 queda en null y, por la config non_null, ni siquiera sale en el JSON.
     * Quien garantiza eso es ClienteMapper: nunca asignar aqui la tarjeta completa.
     */
    private String tarjetaCredito;

}
