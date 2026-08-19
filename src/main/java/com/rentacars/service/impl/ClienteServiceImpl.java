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

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Override
    public CreateClienteResponse crearCliente(CreateClienteRequest request) {
        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya existe");
        }

        Cliente cliente = clienteMapper.toEntity(request);
        Cliente clienteGuardado = clienteRepository.save(cliente);

        return clienteMapper.toCreateResponse(clienteGuardado);
    }

    @Override
    public CreateClienteResponse actualizarCliente(Long id, UpdateClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id " + id));

        if (request.getNombre() != null) {
            cliente.setNombre(request.getNombre());
        }
        if (request.getTelefono() != null) {
            cliente.setTelefono(request.getTelefono());
        }
        if (request.getTarjetaCredito() != null) {
            cliente.setTarjetaCredito(request.getTarjetaCredito());
        }

        Cliente clienteActualizado = clienteRepository.save(cliente);
        return clienteMapper.toCreateResponse(clienteActualizado);
    }
}