package com.rentacars.service;

import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.request.UpdateDetalle_autoRequest;
import com.rentacars.dto.response.*;

import java.util.List;

public interface AutoService {
    AutoCreateResponse crearAuto(CreateAutoRequest request);
    List<AutoSearchResponse> buscarAutos(String ciudad, Long idCategoria);
    AutoUpdateDetailResponse actualizarDetalles(Long id, UpdateDetalle_autoRequest request);
    AutoAvailabilityResponse actualizarDisponibilidad(Long id, boolean disponibilidad);
    AutoDetailResponse obtenerDetalle(Long id);
    void eliminarAuto(Long id);
}
