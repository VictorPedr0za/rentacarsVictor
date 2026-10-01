package com.rentacars.controller;

import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.dto.request.UpdateDetalle_autoRequest;
import com.rentacars.dto.response.*;
import com.rentacars.service.AutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/autos")
@RequiredArgsConstructor
public class AutoController {
    private final AutoService autoService;

    // HU-08
    @PostMapping
    public ResponseEntity<AutoCreateResponse> crearAuto(@Valid @RequestBody CreateAutoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(autoService.crearAuto(request));
    }

    // HU-09
    @GetMapping
    public ResponseEntity<List<AutoSearchResponse>> buscarAutos(
            @RequestParam(required = false) String ciudad,
            @RequestParam(name = "id_categoria", required = false) Long idCategoria) {
        return ResponseEntity.ok(autoService.buscarAutos(ciudad, idCategoria));
    }

    // HU-10
    @PutMapping("/{id}")
    public ResponseEntity<AutoUpdateDetailResponse> actualizarDetalles(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDetalle_autoRequest request) {
        return ResponseEntity.ok(autoService.actualizarDetalles(id, request));
    }

    // HU-11
    @PatchMapping("/{id}/disponibilidad")
    public ResponseEntity<AutoAvailabilityResponse> actualizarDisponibilidad(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAutoRequest request) {
        return ResponseEntity.ok(autoService.actualizarDisponibilidad(id, request.getDisponibilidad()));
    }

    // HU-12
    @GetMapping("/{id}")
    public ResponseEntity<AutoDetailResponse> obtenerDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(autoService.obtenerDetalle(id));
    }

    // HU-13
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAuto(@PathVariable Long id) {
        autoService.eliminarAuto(id);
        return ResponseEntity.noContent().build();
    }
}
