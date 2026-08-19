package com.rentacars.mapper;

import com.rentacars.dto.request.CreateClienteRequest;
import com.rentacars.dto.response.CreateClienteResponse;
import com.rentacars.model.Cliente;
import org.springframework.stereotype.Component;
import com.rentacars.dto.response.ClienteResponse;

@Component
public class ClienteMapper {

    public Cliente toEntity(CreateClienteRequest request) {
        return Cliente.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .tarjetaCredito(request.getTarjetaCredito())
                .build();
    }

    public CreateClienteResponse toCreateResponse(Cliente cliente) {
        return CreateClienteResponse.builder()
                .idCliente(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .build();
    }

    // HU-16 LISTAR CLIENTES
    public String enmascararTarjeta(String tarjeta) {
        if (tarjeta == null || tarjeta.length() < 4) {
            return tarjeta;
        }
        String ultimosDigitos = tarjeta.substring(tarjeta.length() - 4);
        String asteriscos = "*".repeat(tarjeta.length() - 4);
        return asteriscos + ultimosDigitos;
    }

    public ClienteResponse toResponse(Cliente cliente) {
        return ClienteResponse.builder()
                .idCliente(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .tarjetaCredito(enmascararTarjeta(cliente.getTarjetaCredito()))
                .build();
    }
}