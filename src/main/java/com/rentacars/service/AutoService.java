package com.rentacars.service;

import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;
import com.rentacars.dto.response.CreateDetalle_autoResponse;
import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.dto.response.UpdateAutoResponse;
import com.rentacars.dto.request.UpdateDetalle_autoRequest;

import java.util.List;

/**
 * Interfaz Service del dominio Auto.
 * Cada HU agrega UNA linea aqui. No borren ni reescriban las de los demas.
 *   HU-09 (Suarez) -> buscarAutos
 *   HU-10 (Suarez) -> actualizarDetalles
 *   HU-11 (Suarez) -> actualizarDisponibilidad
 *   HU-12 (Cardona) -> getAutoById ahora retorna el detalle completo
 *   HU-13 (Cardona) -> deleteAuto valida disponibilidad y borra en cascada
 */


public interface AutoService {
  
    // HU-09 (Suarez)
    java.util.List<CreateAutoResponse> buscarAutos (String ciudad, Long idCategoria);

    // HU-11 (Suarez)
    CreateAutoResponse actualizarDisponibilidad (Long id, UpdateAutoRequest updateAutoRequest);

    // HU-10 (Suarez)
    CreateAutoResponse actualizarDetalles (Long id, UpdateDetalle_autoRequest updateDetalle_autoRequest);


    CreateAutoResponse createAuto(CreateAutoRequest createAutoRequest) throws Exception;

    //get all
    List<CreateAutoResponse> getAllAutos();

    //get by id
    //CreateAutoResponse getAutoById(Long id);

    //get by id
    // HU-12 (Cardona): detalle completo del auto (autos + detalles_autos) con precio calculado
    CreateDetalle_autoResponse getAutoById(Long id);

    //put
    UpdateAutoResponse updateAuto(Long id, UpdateAutoRequest updateAutoRequest) throws Exception;

    //delete
    // HU-13 (Cardona): borra detalle y auto, valida disponibilidad
    void deleteAuto(Long id);

}
