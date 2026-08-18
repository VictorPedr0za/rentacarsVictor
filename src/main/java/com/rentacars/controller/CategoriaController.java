package com.rentacars.controller;

import com.rentacars.dto.request.CreateCategoriaRequest;
import com.rentacars.dto.response.CreateCategoriaResponse;

import com.rentacars.service.CategoriaService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor

public class CategoriaController {

     // Service que contiene la lógica de negocio.

    private final CategoriaService categoriaService;


    /*
     * =====================================================
     * HU-06
     *
     * POST /categorias
     * =====================================================
     */
    @PostMapping
    public ResponseEntity<CreateCategoriaResponse>
    crearCategoria(
            @Valid @RequestBody CreateCategoriaRequest request) {

        /*
         * Enviamos el Request al Service.
         */
        CreateCategoriaResponse response =
                categoriaService.crearCategoria(request);

        /*
         * 201 significa que el recurso
         * fue creado correctamente.
         */
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    /*
     * =====================================================
     * HU-07
     *
     * GET /categorias
     * =====================================================
     */
    @GetMapping
    public ResponseEntity<List<CreateCategoriaResponse>>
    listarCategorias() {

        /*
         * Solicitamos al Service
         * todas las categorías.
         */
        return ResponseEntity.ok(
                categoriaService.listarCategorias()
        );
    }
}
