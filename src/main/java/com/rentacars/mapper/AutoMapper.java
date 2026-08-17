package com.rentacars.mapper;

import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;

import com.rentacars.model.Auto;
import com.rentacars.model.Categoria;
import com.rentacars.model.Detalle_auto;
import com.rentacars.model.Tienda;

public class AutoMapper {

    public static Auto createAutoRequestToEntity(
            CreateAutoRequest createAutoRequest,
            Tienda tienda,
            Categoria categoria) {


        if (createAutoRequest == null) {
            return null;
        }

        return Auto.builder()


                .disponibilidad(true)

                .tienda(tienda)

                .categoria(categoria)

                .build();
    }



    public static CreateAutoResponse
    entityToCreateAutoResponse(
            Auto auto,
            Detalle_auto detalleAuto) {

        if (auto == null || detalleAuto == null) {
            return null;
        }

        return CreateAutoResponse.builder()

                /*
                 * Datos del auto.
                 */
                .idAuto(auto.getIdAuto())

                .disponibilidad(
                        auto.getDisponibilidad()
                )

                .idTienda(
                        auto.getTienda().getIdTienda()
                )

                .idCategoria(
                        auto.getCategoria().getIdCategoria()
                )

                /*
                 * Datos del detalle.
                 */
                .imagen(
                        detalleAuto.getImagen()
                )

                .modelo(
                        detalleAuto.getModelo()
                )

                .marca(
                        detalleAuto.getMarca()
                )

                .anio(
                        detalleAuto.getAnio()
                )

                .placa(
                        detalleAuto.getPlaca()
                )

                .precioDia(
                        detalleAuto.getPrecioDia()
                )

                .ofertaPorcentaje(
                        detalleAuto.getOfertaPorcentaje()
                )

                .build();
    }
}
