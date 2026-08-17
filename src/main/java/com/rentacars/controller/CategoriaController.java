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

    private final CategoriaService categoriaService;



    @PostMapping
    public ResponseEntity<CreateCategoriaResponse>
    crearCategoria(
            @Valid
            @RequestBody
            CreateCategoriaRequest request) {


         /* Enviamos el Request al Service.*/

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        categoriaService.crearCategoria(request)
                );
    }



    @GetMapping
    public ResponseEntity<List<CreateCategoriaResponse>>
    listarCategorias() {

        /*
         * Obtenemos las categorías
         * mediante el Service.
         */
        return ResponseEntity.ok(
                categoriaService.listarCategorias()
        );
    }
}

