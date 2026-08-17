package com.rentacars.service;

import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;

import com.rentacars.model.Auto;

public interface AutoService {

     //Registrar un auto con sus detalles.

    CreateAutoResponse crearAuto(
            CreateAutoRequest request
    );


    void actualizarDisponibilidad(
            Long id,
            boolean disponibilidad
    );



     // Método interno que permite obtener un auto por su ID.

    Auto obtenerAuto(Long id);
}
