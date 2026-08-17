package com.rentacars.service;


import com.rentacars.dto.request.CreateCategoriaRequest;
import com.rentacars.dto.response.CreateCategoriaResponse;
import com.rentacars.model.Categoria;

import java.util.List;

public interface CategoriaService {
    CreateCategoriaResponse crearCategoria(
            CreateCategoriaRequest request
    );

    List<CreateCategoriaResponse> listarCategorias();

    Categoria obtenerCategoria(Long id);
}
