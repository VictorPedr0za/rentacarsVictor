package com.rentacars.dto.request;
import lombok.Data;
@Data

public class UpdateClienteRequest {
    private String nombre;
    private String telefono;
    private String tarjetaCredito;
}
