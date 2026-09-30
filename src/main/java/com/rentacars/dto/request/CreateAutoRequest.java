package com.rentacars.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HU-08: body de POST /autos.
 *
 * Hereda de CreateDetalle_autoRequest los campos de la ficha (modelo, marca, anio,
 * placa, precio_dia, oferta_porcentaje, imagen). Aqui solo van las llaves foraneas.
 *
 * "disponibilidad" ya NO llega del cliente: todo auto nuevo inicia en true.
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateAutoRequest extends CreateDetalle_autoRequest {

    //valida id de tienda requerido
    @NotNull(message = "El id de la tienda es requerido")
    private Long idTienda;

    //valida id de categoria requerido
    @NotNull(message = "El id de la categoria es requerido")
    private Long idCategoria;

}
