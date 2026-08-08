package com.rentacars.controller;

import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;
import com.rentacars.service.AutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
