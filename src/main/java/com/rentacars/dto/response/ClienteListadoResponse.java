package com.rentacars.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClienteListadoResponse {
    private Long idCliente;
    private String nombre;
    private String email;
    private String telefono;
    private String tarjetaCredito;
}
