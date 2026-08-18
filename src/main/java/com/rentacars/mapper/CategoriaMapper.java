package com.rentacars.mapper;

import com.rentacars.dto.request.CreateCategoriaRequest;
import com.rentacars.dto.response.CreateCategoriaResponse;
import com.rentacars.model.Categoria;

import java.util.List;

public class CategoriaMapper {

     // Convierte el Request recibido desde el Controller en una entidad Categoria.

    public static Categoria createCategoriaRequestToEntity(
            CreateCategoriaRequest request) {

        return Categoria.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .build();
    }


     // Convierte una entidad Categoria en un CreateCategoriaResponse.

    public static CreateCategoriaResponse
    entityToCreateCategoriaResponse(
            Categoria categoria) {

        if (categoria == null) {
            return null;
        }

        return CreateCategoriaResponse.builder()
                .idCategoria(categoria.getIdCategoria())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .build();
    }


     //Convierte una lista de categorías en una lista de respuestas.

    public static List<CreateCategoriaResponse>
    entityToListCreateCategoriaResponse(
            List<Categoria> categorias) {


        if (categorias == null || categorias.isEmpty()) {
            return List.of();
        }

         // Convertimos cada entidad utilizando el método anterior.

        return categorias.stream()
                .map(CategoriaMapper::entityToCreateCategoriaResponse)
                .toList();
    }
}
