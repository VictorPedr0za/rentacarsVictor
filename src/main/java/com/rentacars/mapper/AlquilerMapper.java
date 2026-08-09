package com.rentacars.mapper;

import com.rentacars.dto.response.CreateAlquilerResponse;
import com.rentacars.dto.request.CreateAlquilerRequest;
import com.rentacars.dto.response.UpdateAlquilerResponse;
import com.rentacars.model.Alquiler;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AlquilerMapper {

    //asigna valores para crear objeto
    public static CreateAlquilerResponse entityToCreateAlquilerResponse(Alquiler alquiler) {
        return CreateAlquilerResponse.builder()
                .idAlquiler(alquiler.getIdAlquiler())
                .idCliente(alquiler.getIdCliente())
                .idAuto(alquiler.getIdAuto())
                .fechaInicio(alquiler.getFechaInicio())
                .fechaFin(alquiler.getFechaFin())
                .precioTotal(alquiler.getPrecioTotal())
                .ciudadRetirada(alquiler.getCiudadRetirada())
                .ciudadDevolucion(alquiler.getCiudadDevolucion())
                .estado(alquiler.getEstado())
                .build();
    }

    //convierte lista
    public static List<CreateAlquilerResponse> entityToListCreateAlquilerResponse(List<Alquiler> alquileres) {
        return alquileres.stream().map(AlquilerMapper::entityToCreateAlquilerResponse).toList();
    }

    //convierte request a entidad
    public static Alquiler createAlquilerRequestToEntity(CreateAlquilerRequest createAlquilerRequest){

        //construye entidad alquiler desde request
        return Alquiler.builder()
                .idCliente(createAlquilerRequest.getIdCliente())
                .idAuto(createAlquilerRequest.getIdAuto())
                .fechaInicio(createAlquilerRequest.getFechaInicio())
                .fechaFin(createAlquilerRequest.getFechaFin())
                .precioTotal(createAlquilerRequest.getPrecioTotal())
                .ciudadRetirada(createAlquilerRequest.getCiudadRetirada())
                .ciudadDevolucion(createAlquilerRequest.getCiudadDevolucion())
                .estado(createAlquilerRequest.getEstado())
                .build();
    }

    //convierte entidad a update
    public static UpdateAlquilerResponse entityToUpdateAlquilerResponse(Alquiler alquiler) {

        //instanciar nuevo objeto
        UpdateAlquilerResponse response = UpdateAlquilerResponse.builder()
                .idAlquiler(alquiler.getIdAlquiler())
                .idCliente(alquiler.getIdCliente())
                .idAuto(alquiler.getIdAuto())
                .fechaInicio(alquiler.getFechaInicio())
                .fechaFin(alquiler.getFechaFin())
                .precioTotal(alquiler.getPrecioTotal())
                .ciudadRetirada(alquiler.getCiudadRetirada())
                .ciudadDevolucion(alquiler.getCiudadDevolucion())
                .estado(alquiler.getEstado())
                .build();


        return response;
    }


}