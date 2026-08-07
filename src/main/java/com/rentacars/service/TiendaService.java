package com.rentacars.service;

import com.rentacars.dto.response.CreateTiendaResponse;
import com.rentacars.dto.request.CreateTiendaRequest;

/**
 * PLANTILLA DE INTERFAZ DE SERVICE -- copien este patron para las demas entidades.
 *
 * QUE ES UNA INTERFAZ:
 * Es un contrato. Dice QUE se puede hacer, pero no COMO se hace.
 * Aqui solo van las firmas de los metodos, sin cuerpo y sin logica.
 * El COMO va en TiendaServiceImpl.
 *
 * POR QUE SE HACE ASI:
 * El TiendaController depende de esta interfaz, no de la implementacion.
 * Eso es el principio D de SOLID (inversion de dependencias).
 *
 * COMO CRECE ESTE ARCHIVO:
 * Cada HU del dominio Tiendas agrega UNA linea aqui.
 *   HU-01 (Arango)   -> crearTienda        <-- ya esta hecha, es el ejemplo
 *   HU-02 (Arango)   -> actualizarTienda
 *   HU-03 (Arango)   -> listarTiendas
 *   HU-04 (Corrales) -> eliminarTienda
 *   HU-05 (Corrales) -> obtenerTienda
 *
 * IMPORTANTE: varias personas van a editar este mismo archivo.
 * Agreguen SOLO su metodo, no borren ni reescriban los de los demas.
 */
public interface TiendaService {

    /** HU-01: registra una tienda nueva y devuelve la tienda creada con su id. */
    CreateTiendaResponse crearTienda(CreateTiendaRequest request);

    // HU-02 (Arango):
    // CreateTiendaResponse actualizarTienda(Long id, UpdateTiendaRequest request);

    // HU-03 (Arango):
    // List<CreateTiendaResponse> listarTiendas(String ciudad);

    // HU-04 (Corrales):
    // void eliminarTienda(Long id);

    // HU-05 (Corrales):
    // CreateTiendaResponse obtenerTienda(Long id);
}
