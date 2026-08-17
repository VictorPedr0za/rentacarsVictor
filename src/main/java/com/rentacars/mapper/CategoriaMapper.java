package com.rentacars.mapper;

import com.rentacars.dto.request.CreateCategoriaRequest;
import com.rentacars.dto.response.CreateCategoriaResponse;
import com.rentacars.model.Categoria;

import java.util.List;

public class CategoriaMapper {

    public static Categoria createCategoriaRequestToEntity(
            CreateCategoriaRequest createCategoriaRequest) {
        if (createCategoriaRequest == null) {
            return null;
        }
        return Categoria.builder()
                .nombre(createCategoriaRequest.getNombre())
                .descripcion(createCategoriaRequest.getDescripcion())
                .build();
    }

    public static CreateCategoriaResponse
    entityToCreateCategoriaResponse(
            Categoria categoria) {
        if (categoria == null) {
            return null;
        }
        return CreateCategoriaResponse.builder()
                .idCategoria(Math.toIntExact(categoria.getIdCategoria()))
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .build();
    }

    public static List<CreateCategoriaResponse>
    entityToListCreateCategoriaResponse(
            List<Categoria> categorias) {
        if (categorias == null || categorias.isEmpty()) {
            return List.of();
        }
        return categorias.stream()
                .map(
                        CategoriaMapper::
                                entityToCreateCategoriaResponse
                )
                .toList();
    }

}
