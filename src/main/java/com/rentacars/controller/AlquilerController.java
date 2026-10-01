package com.rentacars.controller;

import com.rentacars.dto.request.CreateAlquilerRequest;
import com.rentacars.dto.response.*;
import com.rentacars.service.AlquilerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alquileres")
@RequiredArgsConstructor
public class AlquilerController {
    private final AlquilerService alquilerService;

    // HU-18
    @PostMapping
    public ResponseEntity<CreateAlquilerResponse> crearAlquiler(@Valid @RequestBody CreateAlquilerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alquilerService.crearAlquiler(request));
    }

    // HU-20
    @GetMapping
    public ResponseEntity<List<HistorialAlquilerResponse>> historialPorCliente(
            @RequestParam("id_cliente") Long idCliente) {
        return ResponseEntity.ok(alquilerService.historialPorCliente(idCliente));
    }

    // HU-21
    @GetMapping("/activos")
    public ResponseEntity<List<AlquilerActivoResponse>> listarActivos() {
        return ResponseEntity.ok(alquilerService.listarActivos());
    }

    // HU-22
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarAlquiler(@PathVariable Long id) {
        alquilerService.cancelarAlquiler(id);
        return ResponseEntity.noContent().build();
    }

    // HU-24
    @PutMapping("/{id}/devolucion")
    public ResponseEntity<DevolucionAlquilerResponse> registrarDevolucion(@PathVariable Long id) {
        return ResponseEntity.ok(alquilerService.registrarDevolucion(id));
    }
}
