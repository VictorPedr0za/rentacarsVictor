package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateClienteRequest;
import com.rentacars.dto.request.UpdateClienteRequest;
import com.rentacars.dto.response.CreateClienteResponse;
import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.mapper.ClienteMapper;
import com.rentacars.model.Cliente;
import com.rentacars.repository.ClienteRepository;
import com.rentacars.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Dominio Cliente (Murcia) -- implementado por Claude, siguiendo el patron
 * de TiendaServiceImpl que describe EMPEZAR_AQUI.md.
 */
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    /**
     * HU-14: Registrar cliente.
     * Reglas del backlog:
     *   - Todos los campos obligatorios -> los cubre @Valid + @NotBlank en el DTO.
     *   - Si el email ya existe -> 400 con "El email ya existe".
     *   - La tarjeta nunca se devuelve en la respuesta.
     */
    @Override
    @Transactional
    public CreateClienteResponse crearCliente(CreateClienteRequest request) {
        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya existe");
        }

        Cliente cliente = clienteMapper.toEntity(request);
        Cliente clienteGuardado = clienteRepository.save(cliente);
        return clienteMapper.toResponse(clienteGuardado);
    }

    /**
     * HU-15: Actualizar datos de cliente.
     * Reglas del backlog:
     *   - Si no existe -> 404 Not Found.
     *   - No se puede cambiar el email (el DTO ni siquiera trae ese campo).
     *   - La tarjeta nunca se devuelve en la respuesta.
     */
    @Override
    @Transactional
    public CreateClienteResponse actualizarCliente(Long id, UpdateClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        if (request.getTelefono() != null) {
            cliente.setTelefono(request.getTelefono());
        }
        if (request.getTarjetaCredito() != null) {
            cliente.setTarjetaCredito(request.getTarjetaCredito());
        }

        Cliente clienteActualizado = clienteRepository.save(cliente);
        return clienteMapper.toResponse(clienteActualizado);
    }

    /**
     * HU-16: Listar todos los clientes, con la tarjeta enmascarada.
     * Regla del backlog: si no hay clientes, retornar lista vacia (no error).
     */
    @Override
    public List<CreateClienteResponse> listarClientes() {
        return clienteRepository.findAll().stream()
                .map(clienteMapper::toResponseConTarjetaEnmascarada)
                .toList();
    }

    /**
     * Usada por HU-18 (Pedroza) para validar que el cliente exista antes
     * de crear un alquiler. No es una HU propia del dominio Cliente.
     */
    @Override
    public CreateClienteResponse obtenerCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
        return clienteMapper.toResponse(cliente);
    }
}
