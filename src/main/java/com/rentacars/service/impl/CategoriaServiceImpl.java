package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateCategoriaRequest;
import com.rentacars.dto.response.CreateCategoriaResponse;

import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;

import com.rentacars.mapper.CategoriaMapper;
import com.rentacars.model.Categoria;

import com.rentacars.repository.CategoriaRepository;
import com.rentacars.service.CategoriaService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {
    private final CategoriaRepository categoriaRepository;
    /*
     * =====================================================
     * HU-06
     * REGISTRAR CATEGORÍA
     * =====================================================
     */
    @Override
    public CreateCategoriaResponse crearCategoria(
            CreateCategoriaRequest request) {

        /*
         * Antes de guardar verificamos que
         * no exista otra categoría con el mismo nombre.
         */
        if (categoriaRepository.existsByNombre(
                request.getNombre())) {

            /*
             * Si ya existe, lanzamos un error 400.
             */
            throw new BadRequestException(
                    "Ya existe una categoria con ese nombre"
            );
        }

        /*
         * Convertimos el Request
         * en una entidad Categoria.
         */
        Categoria categoria =
                CategoriaMapper
                        .createCategoriaRequestToEntity(
                                request
                        );

        /*
         * Guardamos la categoría.
         */
        Categoria categoriaGuardada =
                categoriaRepository.save(categoria);

        /*
         * Convertimos la entidad guardada
         * en el Response.
         */
        return CategoriaMapper
                .entityToCreateCategoriaResponse(
                        categoriaGuardada
                );
    }


    /*
     * =====================================================
     * HU-07
     * LISTAR CATEGORÍAS
     * =====================================================
     */
    @Override
    public List<CreateCategoriaResponse>
    listarCategorias() {

        /*
         * Buscamos todas las categorías.
         */
        List<Categoria> categorias =
                categoriaRepository.findAll();

        /*
         * Convertimos las entidades
         * a DTOs de respuesta.
         */
        return CategoriaMapper
                .entityToListCreateCategoriaResponse(
                        categorias
                );
    }


    /*
     * =====================================================
     * HU-08
     * OBTENER CATEGORÍA
     * =====================================================
     */
    @Override
    public CreateCategoriaResponse
    obtenerCategoria(Long id) {

        /*
         * Buscamos la categoría por ID.
         *
         * Si no existe, lanzamos un error 404.
         */
        Categoria categoria =
                categoriaRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No existe la categoria con id "
                                                + id
                                )
                        );

        /*
         * Convertimos la entidad
         * encontrada en Response.
         */
        return CategoriaMapper
                .entityToCreateCategoriaResponse(
                        categoria
                );
    }
}
