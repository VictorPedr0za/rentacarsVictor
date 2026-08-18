package com.rentacars.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoriaResponse {
     // ID generado automáticamente por la base de datos.

    private Long idCategoria;

    //Nombre de la categoría.

    private String nombre;

     // Descripción de la categoría.

    private String descripcion;
}
