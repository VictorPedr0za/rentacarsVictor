package com.rentacars.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PLANTILLA DE DTO DE SALIDA -- copien este patron para las demas HU.
 *
 * Un "Response" representa el JSON que SALE de la API:
 *
 *     { "id_tienda": 1, "nombre": "Tienda Norte", "ciudad": "Bogota",
 *       "direccion": "Calle 100 #15-20" }
 *
 * Fijense que el campo en Java se llama idTienda (camelCase) y en el JSON
 * sale como "id_tienda". Eso lo hace solo la configuracion SNAKE_CASE
 * de application.properties.
 *
 * Los Response NO llevan validaciones (@NotBlank): esas son solo de entrada.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTiendaResponse {

    private Long idTienda;
    private String nombre;
    private String ciudad;
    private String direccion;
}
