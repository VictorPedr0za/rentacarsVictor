package com.rentacars.controller;

import com.rentacars.dto.request.CreateClienteRequest;
import com.rentacars.dto.request.UpdateClienteRequest;
import com.rentacars.dto.response.CreateClienteResponse;
import com.rentacars.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dominio Cliente (Murcia) -- implementado por Claude, sin logica propia:
 * solo recibe la peticion HTTP y se la pasa al service (mismo patron que
 * TiendaController).
 */
@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    /** HU-14: Registrar cliente. POST /clientes */
    @PostMapping
    public ResponseEntity<CreateClienteResponse> crearCliente(
            @Valid @RequestBody CreateClienteRequest request) {
        CreateClienteResponse response = clienteService.crearCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** HU-15: Actualizar datos de cliente. PUT /clientes/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<CreateClienteResponse> actualizarCliente(
            @PathVariable Long id,
            @RequestBody UpdateClienteRequest request) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, request));
    }

    /** HU-16: Listar todos los clientes (tarjeta enmascarada). GET /clientes */
    @GetMapping
    public ResponseEntity<List<CreateClienteResponse>> listarClientes() {
        return ResponseEntity.ok(clienteService.listarClientes());
    }
}
