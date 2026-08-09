package com.rentacars.mapper;

import com.rentacars.dto.response.CreateAutoResponse;
import com.rentacars.dto.response.CreateDetalle_autoResponse;
import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.response.UpdateAutoResponse;
import com.rentacars.model.Auto;
import com.rentacars.model.Detalle_auto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Getter
@AllArgsConstructor
public class AutoMapper {

    //asigna valores para crear objeto
    public static CreateAutoResponse entityToCreateAutoResponse(Auto auto) {
        return CreateAutoResponse.builder()
                .idAuto(auto.getIdAuto())
                .disponibilidad(auto.getDisponibilidad())
                .idTienda(auto.getIdTienda())
                .idCategoria(auto.getIdCategoria())
                .build();
    }

    //convierte lista
    public static List<CreateAutoResponse> entityToListCreateAutoResponse(List<Auto> autos) {
        return autos.stream().map(AutoMapper::entityToCreateAutoResponse).toList();
    }

    //convierte request a entidad
    public static Auto createAutoRequestToEntity(CreateAutoRequest createAutoRequest){

        //construye entidad auto desde request
        return Auto.builder()
                .disponibilidad(createAutoRequest.getDisponibilidad())
                .idTienda(createAutoRequest.getIdTienda())
                .idCategoria(createAutoRequest.getIdCategoria())
                .build();
    }

    //convierte entidad a update
    public static UpdateAutoResponse entityToUpdateAutoResponse(Auto auto) {

        //instanciar nuevo objeto
        UpdateAutoResponse response = UpdateAutoResponse.builder()
                .idAuto(auto.getIdAuto())
                .disponibilidad(auto.getDisponibilidad())
                .idTienda(auto.getIdTienda())
                .idCategoria(auto.getIdCategoria())
                .build();


        return response;
    }

    // HU-12 (cardona): combina Auto y detalle_auto en un solo response, con el precio con oferta ya calculado
    public static CreateDetalle_autoResponse entityToCreateDetalle_autoResponse(Auto auto, Detalle_auto detalle) {

        //la oferta puede ser null (auto sin oferta), se trata como 0
        BigDecimal oferta = detalle.getOfertaPorcentaje();
        if (oferta == null) {
            oferta = BigDecimal.ZERO;
        }

        //calcula el descuento y el precio con oferta
        BigDecimal descuento = detalle.getPrecioDia()
                .multiply(oferta)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal precioConOferta = detalle.getPrecioDia().subtract(descuento);

        return CreateDetalle_autoResponse.builder()
                .idAuto(auto.getIdAuto())
                .modelo(detalle.getModelo())
                .marca(detalle.getMarca())
                .anio(detalle.getAnio())
                .placa(detalle.getPlaca())
                .precioDia(detalle.getPrecioDia())
                .ofertaPorcentaje(detalle.getOfertaPorcentaje())
                .precioConOferta(precioConOferta)
                .disponibilidad(auto.getDisponibilidad())
                .build();
    }


}