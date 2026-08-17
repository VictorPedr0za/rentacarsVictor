package com.rentacars.controller;

import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;

import com.rentacars.service.AutoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/autos")
@RequiredArgsConstructor

public class AutoController {

     // Inyectamos la interfaz AutoService.

    private final AutoService autoService;

     // POST /autos

    @PostMapping
    public ResponseEntity<CreateAutoResponse>
    crearAuto(
            @Valid
            @RequestBody
            CreateAutoRequest request) {


         // Enviamos el Request al Service.

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        autoService.crearAuto(request)
                );
    }
}
