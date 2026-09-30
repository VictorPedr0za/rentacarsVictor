package com.rentacars.service;

import com.rentacars.dto.request.CreateCategoriaRequest;
import com.rentacars.dto.request.UpdateCategoriaRequest;
import com.rentacars.dto.response.CreateCategoriaResponse;

import java.util.List;

public interface CategoriaService {

    /*
     * HU-06
     *
     * Registra una nueva categoría.
     */
    CreateCategoriaResponse crearCategoria(
            CreateCategoriaRequest request);

    /*
     * HU-07
     *
     * Lista todas las categorías.
     */
    List<CreateCategoriaResponse> listarCategorias();

    /*
     * HU-08
     *
     * Permite comprobar que una categoría
     * existe antes de registrar un auto.
     */
    CreateCategoriaResponse obtenerCategoria(Long id);

    /*
     * Actualiza nombre y/o descripción. 404 si no existe,
     * 400 si el nombre ya lo usa otra categoría.
     */
    CreateCategoriaResponse actualizarCategoria(Long id, UpdateCategoriaRequest request);

    /*
     * Elimina la categoría. 404 si no existe,
     * 400 si todavía tiene autos asociados.
     */
    void eliminarCategoria(Long id);
}
