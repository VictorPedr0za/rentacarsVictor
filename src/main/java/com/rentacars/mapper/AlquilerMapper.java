package com.rentacars.mapper;

import com.rentacars.dto.response.CreateAlquilerResponse;
import com.rentacars.dto.request.CreateAlquilerRequest;
import com.rentacars.dto.response.UpdateAlquilerResponse;
import com.rentacars.model.Alquiler;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
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

    // HU-18 (Pedroza): construye la entidad Alquiler -- implementado por Claude.
    // precioTotal ya viene calculado desde AlquilerServiceImpl (dias * precio_dia * (1 - oferta/100));
    // el cliente no lo manda. El estado siempre nace en "ACTIVO".
    public static Alquiler createAlquilerRequestToEntity(CreateAlquilerRequest createAlquilerRequest, BigDecimal precioTotal){
        return Alquiler.builder()
                .idCliente(createAlquilerRequest.getIdCliente())
                .idAuto(createAlquilerRequest.getIdAuto())
                .fechaInicio(createAlquilerRequest.getFechaInicio())
                .fechaFin(createAlquilerRequest.getFechaFin())
                .precioTotal(precioTotal)
                .ciudadRetirada(createAlquilerRequest.getCiudadRetirada())
                .ciudadDevolucion(createAlquilerRequest.getCiudadDevolucion())
                .estado("ACTIVO")
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