package com.rentacars.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {
    private Long idCliente;
    private String nombre;
    private String email;
    private String telefono;
    private String tarjetaCredito;
}
