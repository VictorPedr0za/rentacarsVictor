package com.rentacars.mapper;

import com.rentacars.dto.request.CreateDetalle_autoRequest;
import com.rentacars.model.Detalle_auto;

/**
 * Traduce la ficha comercial del auto (request) a la entidad detalles_autos.
 * Solo traduce: las validaciones viven en el DTO y en el service.
 */
public class Detalle_autoMapper {

    private Detalle_autoMapper() {
    }

    // HU-08: el id_auto se asigna despues de guardar el auto (primer insert)
    public static Detalle_auto createDetalle_autoRequestToEntity(CreateDetalle_autoRequest request, Long idAuto) {
        Detalle_auto detalle = new Detalle_auto();
        detalle.setModelo(request.getModelo());
        detalle.setMarca(request.getMarca());
        detalle.setAnio(request.getAnio());
        detalle.setPlaca(request.getPlaca());
        detalle.setPrecioDia(request.getPrecioDia());
        detalle.setOfertaPorcentaje(request.getOfertaPorcentaje());
        detalle.setImagen(request.getImagen());
        detalle.setIdAuto(idAuto);
        return detalle;
    }
}
