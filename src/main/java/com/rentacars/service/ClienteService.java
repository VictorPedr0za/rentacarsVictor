package com.rentacars.service;

import com.rentacars.dto.request.CreateClienteRequest;
import com.rentacars.dto.request.UpdateClienteRequest;
import com.rentacars.dto.response.CreateClienteResponse;

import java.util.List;

/**
 *   HU-14 (Murcia) -> crearCliente
 *   HU-15 (Murcia) -> actualizarCliente
 *   HU-16 (Murcia) -> listarClientes
 *   HU-18 (Pedroza) -> obtenerCliente (valida que el cliente exista antes de alquilar)
 */
public interface ClienteService {

    // HU-14
    CreateClienteResponse crearCliente(CreateClienteRequest request);

    // HU-15: actualiza telefono y/o tarjeta. 404 si no existe. El email no se puede cambiar.
    CreateClienteResponse actualizarCliente(Long id, UpdateClienteRequest request);

    // HU-16: lista todos los clientes con la tarjeta enmascarada. Lista vacia si no hay.
    List<CreateClienteResponse> listarClientes();

    // HU-18: devuelve el cliente o lanza 404
    CreateClienteResponse obtenerCliente(Long id);

    // elimina el cliente. 404 si no existe, 400 si tiene alquileres registrados
    void eliminarCliente(Long id);
}
