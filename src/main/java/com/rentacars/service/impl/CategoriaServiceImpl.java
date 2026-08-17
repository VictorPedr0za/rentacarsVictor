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

        @Override
        public CreateCategoriaResponse crearCategoria(
                CreateCategoriaRequest request) {

            if (categoriaRepository.existsByNombre(
                    request.getNombre())) {

                throw new BadRequestException(
                        "Ya existe una categoria con ese nombre"
                );
            }

            Categoria categoria =
                    CategoriaMapper
                            .createCategoriaRequestToEntity(request);

            Categoria categoriaGuardada =
                    categoriaRepository.save(categoria);

            return CategoriaMapper
                    .entityToCreateCategoriaResponse(
                            categoriaGuardada
                    );
        }


        @Override
        public List<CreateCategoriaResponse> listarCategorias() {


            List<Categoria> categorias =
                    categoriaRepository.findAll();


            return CategoriaMapper
                    .entityToListCreateCategoriaResponse(
                            categorias
                    );
        }

        @Override
        public Categoria obtenerCategoria(Long id) {


            return categoriaRepository.findById(id)

                    /*
                     * Si no existe, lanzamos 404.
                     */
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "No existe la categoria con id: " + id
                            )
                    );
        }
    }


