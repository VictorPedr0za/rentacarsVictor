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

    // HU-18 (Pedroza): crea el alquiler, calcula el precio y marca el auto ocupado
    CreateAlquilerResponse createAlquiler(CreateAlquilerRequest createAlquilerRequest);

    //get all
    List<CreateAlquilerResponse> getAllAlquileres();

    //get by id
    CreateAlquilerResponse getAlquilerById(Long id);

    //put
    UpdateAlquilerResponse updateAlquiler(Long id, UpdateAlquilerRequest updateAlquilerRequest);

    //HU-24
    CreateAlquilerResponse registrarDevolucion(Long id);

    //delete
    // HU-22 (Cardona): cancela alquiler y libera el auto
    void deleteAlquiler(Long id);


    // HU-20 (Pedroza): historial de alquileres de un cliente
    List<CreateAlquilerResponse> historialPorCliente(Long idCliente);

    // HU-21 (Pedroza): lista los alquileres activos
    List<CreateAlquilerResponse> listarActivos();

}
