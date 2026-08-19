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

}
