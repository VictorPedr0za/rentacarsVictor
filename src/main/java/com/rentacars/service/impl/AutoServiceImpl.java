package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;

import com.rentacars.exception.ResourceNotFoundException;

import com.rentacars.mapper.AutoMapper;
import com.rentacars.mapper.Detalle_autoMapper;

import com.rentacars.model.Auto;
import com.rentacars.model.Categoria;
import com.rentacars.model.Detalle_auto;
import com.rentacars.model.Tienda;

import com.rentacars.repository.AutoRepository;
import com.rentacars.repository.Detalle_autoRepository;

import com.rentacars.service.AutoService;
import com.rentacars.service.CategoriaService;
import com.rentacars.service.TiendaService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutoServiceImpl implements AutoService {


     // Repository de Auto.

    private final AutoRepository autoRepository;



     //Repository de Detalle_auto.

    private final Detalle_autoRepository
            detalleAutoRepository;



     //Service de Tienda. Se utiliza para validar que la tienda exista antes de crear el auto.

    private final TiendaService tiendaService;



     // Service de Categoria. Se utiliza para validar que la categoría exista antes de crear el auto.

    private final CategoriaService categoriaService;



     // CREAR AUTO

    @Override
    @Transactional
    public CreateAutoResponse crearAuto(
            CreateAutoRequest request) {


         // VALIDAR TIENDA

        Tienda tienda =
                tiendaService.obtenerTienda(
                        request.getIdTienda()
                );


         //VALIDAR CATEGORIA

        Categoria categoria =
                categoriaService.obtenerCategoria(
                        request.getIdCategoria()
                );



         //CREAR AUTO

        Auto auto =
                AutoMapper.createAutoRequestToEntity(
                        request,
                        tienda,
                        categoria
                );


         // GUARDAR AUTO

        Auto autoGuardado =
                autoRepository.save(auto);


         //CREAR DETALLE

        Detalle_auto detalleAuto =
                Detalle_autoMapper
                        .createAutoRequestToDetalleEntity(
                                request,
                                autoGuardado
                        );



         // GUARDAR DETALLE

        Detalle_auto detalleGuardado =
                detalleAutoRepository.save(
                        detalleAuto
                );



         //CREAR RESPONSE

        return AutoMapper.entityToCreateAutoResponse(
                autoGuardado,
                detalleGuardado
        );
    }


     // OBTENER AUTO

    @Override
    public Auto obtenerAuto(Long id) {


         // Buscamos el auto por ID.

        return autoRepository.findById(id)
                 //Si no existe, lanzamos 404.

                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No existe el auto con id: " + id
                        )
                );
    }



     //ACTUALIZAR DISPONIBILIDAD

    @Override
    public void actualizarDisponibilidad(
            Long id,
            boolean disponibilidad) {


         // Primero verificamos que el auto exista.

        Auto auto = obtenerAuto(id);


         // Cambiamos la disponibilidad.

        auto.setDisponibilidad(disponibilidad);


         //Guardamos el cambio.

        autoRepository.save(auto);
    }

}
