package com.rentacars.service;

import com.rentacars.dto.request.CreateAlquilerRequest;
import com.rentacars.dto.response.*;

import java.util.List;

public interface AlquilerService {
    CreateAlquilerResponse crearAlquiler(CreateAlquilerRequest request);
    List<HistorialAlquilerResponse> historialPorCliente(Long idCliente);
    List<AlquilerActivoResponse> listarActivos();
    void cancelarAlquiler(Long id);
    DevolucionAlquilerResponse registrarDevolucion(Long id);
}
