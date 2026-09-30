package com.rentacars.mapper;

import com.rentacars.dto.request.CreateClienteRequest;
import com.rentacars.dto.response.CreateClienteResponse;
import com.rentacars.model.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    // cantidad de digitos que se dejan visibles al enmascarar
    private static final int DIGITOS_VISIBLES = 4;

    public Cliente toEntity(CreateClienteRequest request) {
        return Cliente.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .tarjetaCredito(request.getTarjetaCredito())
                .build();
    }

    // HU-14 y HU-15: la tarjeta NUNCA sale en la respuesta
    public CreateClienteResponse toCreateResponse(Cliente cliente) {
        return CreateClienteResponse.builder()
                .idCliente(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .build();
    }

    // HU-16: igual que la anterior pero con la tarjeta enmascarada
    public CreateClienteResponse toListResponse(Cliente cliente) {
        CreateClienteResponse response = toCreateResponse(cliente);
        response.setTarjetaCredito(enmascararTarjeta(cliente.getTarjetaCredito()));
        return response;
    }

    /**
     * HU-16: deja visibles solo los ultimos 4 caracteres.
     *   4111111111111111 -> ************1111
     * Si no hay tarjeta devuelve null; si tiene 4 o menos caracteres la oculta completa.
     */
    public String enmascararTarjeta(String tarjeta) {
        if (tarjeta == null || tarjeta.isBlank()) {
            return null;
        }
        int largo = tarjeta.length();
        if (largo <= DIGITOS_VISIBLES) {
            return "*".repeat(largo);
        }
        return "*".repeat(largo - DIGITOS_VISIBLES) + tarjeta.substring(largo - DIGITOS_VISIBLES);
    }
}
