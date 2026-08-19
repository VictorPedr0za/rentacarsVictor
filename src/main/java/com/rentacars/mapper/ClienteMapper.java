package com.rentacars.mapper;

import com.rentacars.dto.request.CreateClienteRequest;
import com.rentacars.dto.response.CreateClienteResponse;
import com.rentacars.model.Cliente;
import org.springframework.stereotype.Component;

/**
 * HU-14/HU-15/HU-16 (Murcia) -- implementado por Claude.
 *
 * Igual que TiendaMapper: solo traduce entre DTO y entidad, sin validar
 * ni decidir nada (eso vive en ClienteServiceImpl).
 */
@Component
public class ClienteMapper {

    /** DTO de entrada -> entidad lista para guardar en la BD (HU-14) */
    public Cliente toEntity(CreateClienteRequest request) {
        Cliente cliente = new Cliente();
        cliente.setNombre(request.getNombre());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setTarjetaCredito(request.getTarjetaCredito());
        return cliente;
    }

    /**
     * Entidad -> DTO de salida SIN la tarjeta de credito (HU-14, HU-15).
     * tarjetaCredito queda en null a proposito: Jackson la omite del JSON.
     */
    public CreateClienteResponse toResponse(Cliente cliente) {
        CreateClienteResponse response = new CreateClienteResponse();
        response.setIdCliente(cliente.getIdCliente());
        response.setNombre(cliente.getNombre());
        response.setEmail(cliente.getEmail());
        response.setTelefono(cliente.getTelefono());
        return response;
    }

    /**
     * Entidad -> DTO de salida CON la tarjeta enmascarada (HU-16).
     * Ejemplo: "4111111111111111" -> "************1111"
     */
    public CreateClienteResponse toResponseConTarjetaEnmascarada(Cliente cliente) {
        CreateClienteResponse response = toResponse(cliente);
        response.setTarjetaCredito(enmascararTarjeta(cliente.getTarjetaCredito()));
        return response;
    }

    private String enmascararTarjeta(String tarjeta) {
        if (tarjeta == null || tarjeta.length() <= 4) {
            return "****";
        }
        String ultimosCuatro = tarjeta.substring(tarjeta.length() - 4);
        return "*".repeat(tarjeta.length() - 4) + ultimosCuatro;
    }
}
