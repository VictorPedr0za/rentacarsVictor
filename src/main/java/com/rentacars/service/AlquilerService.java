package com.rentacars.service;

import com.rentacars.dto.request.CreateAlquilerRequest;
import com.rentacars.dto.response.CreateAlquilerResponse;
import com.rentacars.dto.request.UpdateAlquilerRequest;
import com.rentacars.dto.response.UpdateAlquilerResponse;

import java.util.List;

/**
 * Interfaz Service del dominio Alquiler.
 *   HU-18 (Pedroza) -> createAlquiler ahora valida cliente/auto y calcula el precio -- implementado por Claude
 *   HU-20 (Pedroza) -> historialPorCliente -- implementado por Claude
 *   HU-21 (Pedroza) -> listarActivos -- implementado por Claude
 *   HU-22 (Cardona) -> deleteAlquiler ahora valida fecha y libera el auto
 *   HU-24 (Corrales) -> registrarDevolucion -- implementado por Claude
 */

public interface AlquilerService {

    CreateAlquilerResponse createAlquiler(CreateAlquilerRequest createAlquilerRequest) throws Exception;

    // HU-20 (Pedroza): historial de alquileres de un cliente -- implementado por Claude
    List<CreateAlquilerResponse> historialPorCliente(Long idCliente);

    // HU-21 (Pedroza): alquileres con estado ACTIVO -- implementado por Claude
    List<CreateAlquilerResponse> listarActivos();

    // HU-24 (Corrales): cierra el alquiler y libera el auto -- implementado por Claude
    CreateAlquilerResponse registrarDevolucion(Long id);

    //get all
    List<CreateAlquilerResponse> getAllAlquileres();

    //get by id
    CreateAlquilerResponse getAlquilerById(Long id);

    //put
    UpdateAlquilerResponse updateAlquiler(Long id, UpdateAlquilerRequest updateAlquilerRequest) throws Exception;

    /*
    //delete
    void deleteAlquiler(Long id) throws Exception;
    */

    //delete
    // HU-22 (Cardona): cancela alquiler y libera el auto
    void deleteAlquiler(Long id);

}
