package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateClienteRequest;
import com.rentacars.dto.request.UpdateClienteRequest;
import com.rentacars.dto.response.ClienteListadoResponse;
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

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {
    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Override
    @Transactional
    public CreateClienteResponse crearCliente(CreateClienteRequest request) {
        if (clienteRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BadRequestException("El email ya existe");
        }
        Cliente guardado = clienteRepository.save(clienteMapper.toEntity(request));
        return clienteMapper.toCreateResponse(guardado);
    }

    @Override
    @Transactional
    public CreateClienteResponse actualizarCliente(Long id, UpdateClienteRequest request) {
        Cliente cliente = entidad(id);
        if (request.getTelefono() != null) cliente.setTelefono(request.getTelefono());
        if (request.getTarjetaCredito() != null) cliente.setTarjetaCredito(request.getTarjetaCredito());
        return clienteMapper.toCreateResponse(clienteRepository.save(cliente));
    }

    @Override
    public List<ClienteListadoResponse> listarClientes() {
        return clienteRepository.findAll().stream()
                .map(cliente -> ClienteListadoResponse.builder()
                        .idCliente(cliente.getIdCliente())
                        .nombre(cliente.getNombre())
                        .email(cliente.getEmail())
                        .telefono(cliente.getTelefono())
                        .tarjetaCredito(enmascararTarjeta(cliente.getTarjetaCredito()))
                        .build())
                .toList();
    }

    @Override
    public CreateClienteResponse obtenerCliente(Long id) {
        return clienteMapper.toCreateResponse(entidad(id));
    }

    private Cliente entidad(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id " + id));
    }

    private String enmascararTarjeta(String tarjeta) {
        if (tarjeta == null || tarjeta.isBlank()) return null;
        if (tarjeta.length() <= 4) return tarjeta;
        return "*".repeat(tarjeta.length() - 4) + tarjeta.substring(tarjeta.length() - 4);
    }
}
