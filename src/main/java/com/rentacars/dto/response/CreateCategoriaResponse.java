package com.rentacars.dto.response;

import lombok.*;
import lombok.Builder;
@Setter
@Getter
@Builder

public class CreateCategoriaResponse {
    private Integer idCategoria;
    private String nombre;
    private String descripcion;
}
