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
     * No es una HU propia del dominio Categoria: la usa AutoServiceImpl
     * para validar que la categoria exista antes de registrar un auto
     * (HU-08, Cifuentes) -- comentario corregido por Claude, decia "HU-08"
     * por error ya que esa HU es de Auto, no de Categoria.
     */
    CreateCategoriaResponse obtenerCategoria(Long id);
}
