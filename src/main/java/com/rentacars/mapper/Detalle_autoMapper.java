package com.rentacars.mapper;

import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.model.Detalle_auto;

/** Mapper de la tabla detalles_autos usado por HU-08. */
public final class Detalle_autoMapper {
    private Detalle_autoMapper() {}

    public static Detalle_auto fromCreateAutoRequest(CreateAutoRequest request, Long idAuto) {
        Detalle_auto detalle = new Detalle_auto();
        detalle.setIdAuto(idAuto);
        detalle.setModelo(request.getModelo());
        detalle.setMarca(request.getMarca());
        detalle.setAnio(request.getAnio());
        detalle.setPlaca(request.getPlaca());
        detalle.setPrecioDia(request.getPrecioDia());
        detalle.setOfertaPorcentaje(request.getOfertaPorcentaje());
        detalle.setImagen(request.getImagen());
        return detalle;
    }
}
