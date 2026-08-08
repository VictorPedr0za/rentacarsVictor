package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateTiendaRequest;
import com.rentacars.dto.response.CreateTiendaResponse;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.mapper.TiendaMapper;
import com.rentacars.model.Tienda;
import com.rentacars.repository.TiendaRepository;
import com.rentacars.service.TiendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PLANTILLA DE IMPLEMENTACION DE SERVICE -- copien este patron para las demas HU.
 *
 * AQUI VIVE TODA LA LOGICA DE NEGOCIO. Es la clase mas importante del proyecto.
 * El controller no decide nada; el repository solo habla con la BD.
 * Las validaciones, calculos y reglas van aqui.
 *
 * ANOTACIONES QUE APARECEN:
 *
 *   @Service
 *       Le dice a Spring: "crea un objeto de esta clase y guardalo para
 *       inyectarlo donde haga falta". Sin esto, el controller no la encuentra.
 *
 *   @RequiredArgsConstructor  (de Lombok)
 *       Genera el constructor con todos los campos "private final".
 *       Eso es la INYECCION DE DEPENDENCIAS: Spring ve el constructor y
 *       nos entrega ya listos el repository y el mapper. No usamos "new".
 *
 *   @Transactional
 *       Todo lo que pase dentro del metodo es una sola operacion en la BD:
 *       o se guarda todo, o no se guarda nada. Si el metodo lanza una
 *       excepcion a la mitad, la BD se revierte sola.
 */
@Service
@RequiredArgsConstructor
public class TiendaServiceImpl implements TiendaService {

    // "final" + @RequiredArgsConstructor = Spring los inyecta automaticamente.
    private final TiendaRepository tiendaRepository;
    private final TiendaMapper tiendaMapper;

    /**
     * HU-01: Registrar tienda.
     *
     * Reglas del backlog:
     *   - Todos los campos son obligatorios.
     *   - Si falta alguno -> 400 Bad Request.
     *
     * Fijense que NO hay ningun "if (nombre == null)". Esa validacion la
     * hacen las anotaciones @NotBlank del CreateTiendaRequest, activadas por
     * el @Valid del controller. El GlobalExceptionHandler devuelve el 400.
     *
     * Este metodo tiene 3 lineas porque asi debe ser: recibir, guardar, devolver.
     */
    @Override
    @Transactional
    public CreateTiendaResponse crearTienda(CreateTiendaRequest request) {

        // 1. Traducir el DTO que llego a una entidad que la BD entienda
        Tienda tienda = tiendaMapper.toEntity(request);

        // 2. Guardar. save() devuelve la entidad ya con el id que asigno PostgreSQL
        Tienda tiendaGuardada = tiendaRepository.save(tienda);

        // 3. Traducir la entidad guardada al DTO de respuesta
        return tiendaMapper.toResponse(tiendaGuardada);
    }

    @Override
    @Transactional
    public void eliminarTienda(Long id) {
        Tienda tienda = tiendaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tienda no encontrada con ID: " + id));
        tiendaRepository.delete(tienda);
    }

    // ------------------------------------------------------------------
    // EJEMPLO DE COMO SE VE UNA HU QUE SI TIENE VALIDACION (para HU-02/HU-05):
    //
    // @Override
    // public CreateTiendaResponse obtenerTienda(Long id) {
    //     Tienda tienda = tiendaRepository.findById(id)
    //             .orElseThrow(() -> new ResourceNotFoundException(
    //                     "Tienda no encontrada con id " + id));
    //     return tiendaMapper.toResponse(tienda);
    // }
    //
    // orElseThrow: "si findById no encontro nada, lanza esta excepcion".
    // El GlobalExceptionHandler la convierte en 404 Not Found.
    // ------------------------------------------------------------------
}
