package com.rentacars.service;

import com.rentacars.dto.request.CreateAlquilerRequest;
import com.rentacars.dto.response.CreateAlquilerResponse;
import com.rentacars.dto.request.UpdateAlquilerRequest;
import com.rentacars.dto.response.UpdateAlquilerResponse;

import java.util.List;

/**
 * Interfaz Service del dominio Alquiler.
 *   HU-22 (Cardona) -> deleteAlquiler ahora valida fecha y libera el auto
 */

public interface AlquilerService {

    CreateAlquilerResponse createAlquiler(CreateAlquilerRequest createAlquilerRequest) throws Exception;

    //get all
    List<CreateAlquilerResponse> getAllAlquileres();

    //get by id
    CreateAlquilerResponse getAlquilerById(Long id);

    //put
    UpdateAlquilerResponse updateAlquiler(Long id, UpdateAlquilerRequest updateAlquilerRequest) throws Exception;

    //HU-24
    CreateAlquilerResponse registrarDevolucion(Long id);
    /*
    //delete
    void deleteAlquiler(Long id) throws Exception;
    */

    //delete
    // HU-22 (Cardona): cancela alquiler y libera el auto
    void deleteAlquiler(Long id);

}
