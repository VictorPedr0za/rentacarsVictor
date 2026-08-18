package com.rentacars.service;

import com.rentacars.dto.request.CreateCategoriaRequest;
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
}
