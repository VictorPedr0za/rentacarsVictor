package com.rentacars.service;

import com.rentacars.dto.request.CreateClienteRequest;
import com.rentacars.dto.request.UpdateClienteRequest;
import com.rentacars.dto.response.ClienteListadoResponse;
import com.rentacars.dto.response.CreateClienteResponse;

import java.util.List;

public interface ClienteService {
    CreateClienteResponse crearCliente(CreateClienteRequest request);
    CreateClienteResponse actualizarCliente(Long id, UpdateClienteRequest request);
    List<ClienteListadoResponse> listarClientes();
    CreateClienteResponse obtenerCliente(Long id);
}
