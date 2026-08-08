package com.rentacars.controller;

import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;
import com.rentacars.service.AutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/autos")
@RequiredArgsConstructor
public class AutoController {

    private final AutoService autoService;

    //HU-11 (SUAREZ):
    @PatchMapping ("/{id}/disponibilidad")
    public ResponseEntity<CreateAutoResponse> actualizarDisponibilidad(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAutoRequest request) {
        return ResponseEntity.ok(autoService.actualizarDisponibilidad(id, request));
    }
    // HU-09 (Suarez):
    @GetMapping
    public ResponseEntity<List<CreateAutoResponse>> buscarAutos(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false, name = "id_categoria") Long idCategoria) {
        return ResponseEntity.ok(autoService.buscarAutos(ciudad, idCategoria));
    }

}
