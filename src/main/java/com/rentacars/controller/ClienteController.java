package com.rentacars.controller;

import com.rentacars.dto.request.CreateClienteRequest;
import com.rentacars.dto.request.UpdateClienteRequest;
import com.rentacars.dto.response.ClienteListadoResponse;
import com.rentacars.dto.response.CreateClienteResponse;
import com.rentacars.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService clienteService;

    // HU-14
    @PostMapping
    public ResponseEntity<CreateClienteResponse> crearCliente(@Valid @RequestBody CreateClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crearCliente(request));
    }

    // HU-15
    @PutMapping("/{id}")
    public ResponseEntity<CreateClienteResponse> actualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClienteRequest request) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, request));
    }

    // HU-16
    @GetMapping
    public ResponseEntity<List<ClienteListadoResponse>> listarClientes() {
        return ResponseEntity.ok(clienteService.listarClientes());
    }
}
