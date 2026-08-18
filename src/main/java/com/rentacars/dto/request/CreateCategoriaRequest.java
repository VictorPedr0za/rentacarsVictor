package com.rentacars.dto.request;

import jakarta.validation.constraints.NotBlank;

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
public class CreateCategoriaRequest {
     // Nombre de la categoría.

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

     //Descripción de la categoría.

    @NotBlank(message = "La descripcion es obligatoria")
    private String descripcion;
}
