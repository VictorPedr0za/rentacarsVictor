package com.rentacars.service;

import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;

/**
 * Interfaz Service del dominio Auto.
 * Cada HU agrega UNA linea aqui. No borren ni reescriban las de los demas.
 *   HU-09 (Suarez) -> buscarAutos
 *   HU-10 (Suarez) -> actualizarDetalles
 *   HU-11 (Suarez) -> actualizarDisponibilidad
 */
public interface AutoService {

    // HU-11 (Suarez)
    CreateAutoResponse actualizarDisponibilidad (Long id, UpdateAutoRequest request);

    // HU-09 (Suarez)
    java.util.List<CreateAutoResponse> buscarAutos (String ciudad, Long idCategoria);
}
