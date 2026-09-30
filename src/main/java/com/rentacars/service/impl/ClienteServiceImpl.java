package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateClienteRequest;
import com.rentacars.dto.request.UpdateClienteRequest;
import com.rentacars.dto.response.CreateClienteResponse;
import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.mapper.ClienteMapper;
import com.rentacars.model.Cliente;
import com.rentacars.repository.AlquilerRepository;
import com.rentacars.repository.ClienteRepository;
import com.rentacars.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    // solo para validar que el cliente no tenga alquileres antes de borrarlo
    private final AlquilerRepository alquilerRepository;

    @Override
    @Transactional
    public CreateClienteResponse crearCliente(CreateClienteRequest request) {
        if (clienteRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya existe");
        }

        Cliente cliente = clienteMapper.toEntity(request);
        Cliente clienteGuardado = clienteRepository.save(cliente);

        return clienteMapper.toCreateResponse(clienteGuardado);
    }

    // HU-15 (Murcia): actualiza solo los campos que lleguen; el email no se toca nunca
    @Override
    @Transactional
    public CreateClienteResponse actualizarCliente(Long id, UpdateClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id " + id));

        if (request.getTelefono() != null) {
            cliente.setTelefono(request.getTelefono());
        }
        if (request.getTarjetaCredito() != null) {
            cliente.setTarjetaCredito(request.getTarjetaCredito());
        }

        Cliente clienteActualizado = clienteRepository.save(cliente);

        // la respuesta nunca lleva la tarjeta
        return clienteMapper.toCreateResponse(clienteActualizado);
    }

    // HU-16 (Murcia): lista todos; la tarjeta sale enmascarada
    @Override
    @Transactional(readOnly = true)
    public List<CreateClienteResponse> listarClientes() {
        return clienteRepository.findAll().stream()
                .map(clienteMapper::toListResponse)
                .toList();
    }

    // HU-18: el cliente debe existir (404)
    @Override
    @Transactional(readOnly = true)
    public CreateClienteResponse obtenerCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id " + id));
        return clienteMapper.toCreateResponse(cliente);
    }

    // un cliente con alquileres no se borra: la llave foranea lo impide y se avisa con un 400 claro
    @Override
    @Transactional
    public void eliminarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id " + id));

        if (alquilerRepository.existsByIdCliente(id)) {
            throw new BadRequestException("El cliente tiene alquileres registrados, no se puede eliminar");
        }

        clienteRepository.delete(cliente);
    }
}
