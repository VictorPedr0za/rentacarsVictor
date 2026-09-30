package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateCategoriaRequest;
import com.rentacars.dto.request.UpdateCategoriaRequest;
import com.rentacars.dto.response.CreateCategoriaResponse;

import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;

import com.rentacars.mapper.CategoriaMapper;
import com.rentacars.model.Categoria;

import com.rentacars.repository.AutoRepository;
import com.rentacars.repository.CategoriaRepository;
import com.rentacars.service.CategoriaService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {
    private final CategoriaRepository categoriaRepository;
    // solo para validar que no queden autos con esta categoria al eliminarla
    private final AutoRepository autoRepository;
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


    /*
     * =====================================================
     * ACTUALIZAR CATEGORÍA
     * Solo cambia los campos que lleguen en el body.
     * =====================================================
     */
    @Override
    @Transactional
    public CreateCategoriaResponse actualizarCategoria(
            Long id, UpdateCategoriaRequest request) {

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la categoria con id " + id));

        if (request.getNombre() != null) {
            // el nombre no puede coincidir con el de OTRA categoria
            if (categoriaRepository.existsByNombreAndIdCategoriaNot(request.getNombre(), id)) {
                throw new BadRequestException("Ya existe una categoria con ese nombre");
            }
            categoria.setNombre(request.getNombre());
        }
        if (request.getDescripcion() != null) {
            categoria.setDescripcion(request.getDescripcion());
        }

        return CategoriaMapper.entityToCreateCategoriaResponse(
                categoriaRepository.save(categoria));
    }


    /*
     * =====================================================
     * ELIMINAR CATEGORÍA
     * =====================================================
     */
    @Override
    @Transactional
    public void eliminarCategoria(Long id) {

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe la categoria con id " + id));

        // la llave foranea impide borrarla si hay autos; se avisa con un 400 claro
        if (autoRepository.existsByIdCategoria(id)) {
            throw new BadRequestException(
                    "La categoria tiene autos asociados, no se puede eliminar");
        }

        categoriaRepository.delete(categoria);
    }
}
