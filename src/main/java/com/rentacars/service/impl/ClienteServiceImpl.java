package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateClienteRequest;
import com.rentacars.dto.response.CreateClienteResponse;
import com.rentacars.exception.BadRequestException;
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
}
