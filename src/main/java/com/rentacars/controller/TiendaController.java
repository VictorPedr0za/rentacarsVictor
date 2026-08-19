package com.rentacars.controller;

import com.rentacars.dto.request.CreateTiendaRequest;
import com.rentacars.dto.request.UpdateTiendaRequest;
import com.rentacars.dto.response.CreateTiendaResponse;
import com.rentacars.service.TiendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tiendas")
@RequiredArgsConstructor
public class TiendaController {

    private final TiendaService tiendaService;

    // HU-01 (Arango)
    @PostMapping
    public ResponseEntity<CreateTiendaResponse> crearTienda(
            @Valid @RequestBody CreateTiendaRequest request) {

        CreateTiendaResponse response = tiendaService.crearTienda(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // HU-02 (Arango)
    @PutMapping("/{id}")
    public ResponseEntity<CreateTiendaResponse> actualizarTienda(
            @PathVariable Long id,
            @RequestBody UpdateTiendaRequest request) {

        CreateTiendaResponse response = tiendaService.actualizarTienda(id, request);
        return ResponseEntity.ok(response);
    }

    // HU-03 (Arango)
    @GetMapping
    public ResponseEntity<List<CreateTiendaResponse>> listarTiendas(
            @RequestParam(required = false) String ciudad) {

        List<CreateTiendaResponse> tiendas = tiendaService.listarTiendas(ciudad);
        return ResponseEntity.ok(tiendas);
    }

    // HU-04 (Corrales)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarTienda(@PathVariable Long id) {
        tiendaService.eliminarTienda(id);
        return ResponseEntity.noContent().build();
    }

    // HU-05 (Corrales)
    @GetMapping("/{id}")
    public CreateTiendaResponse getTiendaById(@PathVariable Long id) {
        return tiendaService.getTiendaById(id);
    }
}