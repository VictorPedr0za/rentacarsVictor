package com.rentacars.service;

import com.rentacars.dto.request.CreateClienteRequest;
import com.rentacars.dto.response.CreateClienteResponse;

public interface ClienteService {
    CreateClienteResponse crearCliente(CreateClienteRequest request);
}
