package com.rentacars.mapper;

import com.rentacars.dto.response.CreateAutoResponse;
import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.response.UpdateAutoResponse;
import com.rentacars.model.Auto;
import lombok.AllArgsConstructor;
import lombok.Getter;

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


}