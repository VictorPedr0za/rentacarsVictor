package com.rentacars.service;

import com.rentacars.dto.request.CreateClienteRequest;
import com.rentacars.dto.request.UpdateClienteRequest;
import com.rentacars.dto.response.CreateClienteResponse;

import java.util.List;

/**
 * Interfaz Service del dominio Cliente -- implementado por Claude.
 *   HU-14 (Murcia) -> crearCliente
 *   HU-15 (Murcia) -> actualizarCliente
 *   HU-16 (Murcia) -> listarClientes
 *
 * obtenerCliente() no es una HU de este dominio, pero HU-18 (Pedroza) la
 * necesita para validar que el cliente exista antes de crear un alquiler
 * (ver AlquilerServiceImpl.createAlquiler, que la inyecta en vez de un
 * ClienteFeignClient -- ver el "Cambio v2" del backlog).
 */
public interface ClienteService {

    CreateClienteResponse crearCliente(CreateClienteRequest request);

    CreateClienteResponse actualizarCliente(Long id, UpdateClienteRequest request);

    List<CreateClienteResponse> listarClientes();

    CreateClienteResponse obtenerCliente(Long id);
}
