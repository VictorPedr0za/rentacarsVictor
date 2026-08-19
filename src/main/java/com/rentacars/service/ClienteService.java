package com.rentacars.service;

import com.rentacars.dto.request.CreateClienteRequest;
import com.rentacars.dto.response.CreateClienteResponse;
import com.rentacars.dto.request.UpdateClienteRequest;

public interface ClienteService {
    CreateClienteResponse crearCliente(CreateClienteRequest request);
    CreateClienteResponse actualizarCliente(Long id, UpdateClienteRequest request);
}
