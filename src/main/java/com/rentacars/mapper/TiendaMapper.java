package com.rentacars.mapper;

import com.rentacars.dto.request.CreateTiendaRequest;
import com.rentacars.dto.response.CreateTiendaResponse;
import com.rentacars.model.Tienda;
import org.springframework.stereotype.Component;

/**
 * PLANTILLA DE MAPPER -- copien este patron para las demas entidades.
 *
 * Un mapper solo traduce entre dos formas del mismo dato:
 *
 *   DTO  = lo que viaja por internet (JSON que entra o sale de la API)
 *   Model = lo que se guarda en la base de datos (@Entity)
 *
 * Son distintos a proposito. Ejemplo: CreateClienteResponse NO lleva
 * la tarjeta de credito aunque la entidad Cliente si la tenga.
 *
 * El mapper NO valida ni decide nada. Eso es trabajo del ServiceImpl.
 */
@Component   // @Component hace que Spring cree el objeto y lo pueda inyectar
public class TiendaMapper {

    /** DTO de entrada -> entidad lista para guardar en la BD */
    public Tienda toEntity(CreateTiendaRequest request) {
        Tienda tienda = new Tienda();
        tienda.setNombre(request.getNombre());
        tienda.setCiudad(request.getCiudad());
        tienda.setDireccion(request.getDireccion());
        return tienda;
    }

    /** Entidad guardada -> DTO de salida que se devuelve al cliente */
    public CreateTiendaResponse toResponse(Tienda tienda) {
        CreateTiendaResponse response = new CreateTiendaResponse();
        response.setIdTienda(tienda.getIdTienda());
        response.setNombre(tienda.getNombre());
        response.setCiudad(tienda.getCiudad());
        response.setDireccion(tienda.getDireccion());
        return response;
    }
}
