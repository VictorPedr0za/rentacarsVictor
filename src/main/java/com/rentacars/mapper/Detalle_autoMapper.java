package com.rentacars.mapper;

import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.model.Auto;
import com.rentacars.model.Detalle_auto;

public class Detalle_autoMapper {


     // REQUEST -> DETALLE_AUTO

    public static Detalle_auto
    createAutoRequestToDetalleEntity(
            CreateAutoRequest createAutoRequest,
            Auto auto) {

        if (createAutoRequest == null) {
            return null;
        }

        return Detalle_auto.builder()


                .imagen(
                        createAutoRequest.getImagen()
                )


                .modelo(
                        createAutoRequest.getModelo()
                )


                .marca(
                        createAutoRequest.getMarca()
                )


                .anio(
                        createAutoRequest.getAnio()
                )


                .placa(
                        createAutoRequest.getPlaca()
                )

                .precioDia(
                        createAutoRequest.getPrecioDia()
                )

                .ofertaPorcentaje(
                        createAutoRequest.getOfertaPorcentaje()
                )


                .auto(auto)

                .build();
    }
}
