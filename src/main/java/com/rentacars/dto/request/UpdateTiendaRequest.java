package com.rentacars.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTiendaRequest {
    private String nombre;
    private String ciudad;
    private String direccion;
}