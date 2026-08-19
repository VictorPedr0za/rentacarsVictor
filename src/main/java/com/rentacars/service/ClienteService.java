package com.rentacars.service;

import com.rentacars.dto.request.CreateClienteRequest;
import com.rentacars.dto.response.CreateClienteResponse;
import com.rentacars.dto.request.UpdateClienteRequest;
import java.util.List;
import com.rentacars.dto.response.ClienteResponse;

public interface ClienteService {
    CreateClienteResponse crearCliente(CreateClienteRequest request);
    CreateClienteResponse actualizarCliente(Long id, UpdateClienteRequest request);
    List<ClienteResponse> listarClientes();
}
