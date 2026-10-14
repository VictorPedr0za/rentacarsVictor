package com.rentacars.service.impl;


import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.dto.request.UpdateDetalle_autoRequest;
import com.rentacars.dto.response.CreateAutoResponse;
import com.rentacars.dto.response.CreateDetalle_autoResponse;
import com.rentacars.dto.response.UpdateAutoResponse;
import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.mapper.AutoMapper;
import com.rentacars.model.Auto;
import com.rentacars.model.Detalle_auto;
import com.rentacars.repository.AlquilerRepository;
import com.rentacars.repository.AutoRepository;
import com.rentacars.service.AutoService;
import com.rentacars.service.CategoriaService;
import com.rentacars.service.TiendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Logica de negocio de los autos.
 *
 * DE SQL A NOSQL: el detalle comercial (marca, modelo, precio...) ya no esta en otra tabla:
 * viaja DENTRO del documento Auto (auto.getDetalles()). Por eso desaparecio el
 * Detalle_autoRepository, las lecturas dobles y el @Transactional de HU-08.
 */
@Service
@RequiredArgsConstructor
public class AutoServiceImpl implements AutoService {

    private final AutoRepository autoRepository;
    // valida FK antes de borrar
    private final AlquilerRepository alquilerRepository;

    // HU-08 (v2): valida que la tienda y la categoria existan con una llamada directa (antes era HTTP)
    private final TiendaService tiendaService;
    private final CategoriaService categoriaService;


    //obtiene lista autos, cada uno con su ficha comercial
    @Override
    public List<CreateAutoResponse> getAllAutos() {
        return autoRepository.findAll().stream()
                .map(auto -> AutoMapper.entityToCreateAutoResponse(auto, auto.getDetalles()))
                .toList();
    }

    //HU-09
    @Override
    public List<CreateAutoResponse> buscarAutos(String ciudad, Long idCategoria) {

        // un string en blanco se trata como "sin filtro"
        String ciudadFiltro = (ciudad == null || ciudad.isBlank()) ? null : ciudad.trim();

        return autoRepository.buscarDisponibles(ciudadFiltro, idCategoria).stream()
                .map(auto -> AutoMapper.entityToCreateAutoResponse(auto, auto.getDetalles()))
                .toList();
    }

    //HU-10
    @Override
    public CreateAutoResponse actualizarDetalles(Long id, UpdateDetalle_autoRequest request) {
        Auto auto = autoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auto no encontrado con ID: " + id));

        Detalle_auto detalle = auto.getDetalles();
        if (detalle == null) {
            throw new ResourceNotFoundException("Detalle no encontrado para auto ID: " + id);
        }

        if (request.getPrecioDia() != null) {
            detalle.setPrecioDia(request.getPrecioDia());
        }
        if (request.getOfertaPorcentaje() != null) {
            detalle.setOfertaPorcentaje(request.getOfertaPorcentaje());
        }
        if (request.getImagen() != null) {
            detalle.setImagen(request.getImagen());
        }

        // update parcial: solo reemplaza "detalles"; no pisa la disponibilidad si cambio justo ahora
        Auto actualizado = autoRepository.actualizarParcial(Auto.builder().idAuto(id).detalles(detalle).build());
        Detalle_auto d = actualizado.getDetalles();

        CreateAutoResponse response = new CreateAutoResponse();
        response.setIdAuto(actualizado.getIdAuto());
        response.setDisponibilidad(actualizado.getDisponibilidad());
        response.setModelo(d.getModelo());
        response.setMarca(d.getMarca());
        response.setPrecioDia(d.getPrecioDia());
        response.setOfertaPorcentaje(d.getOfertaPorcentaje());
        response.setImagen(d.getImagen());
        return response;
    }

    //HU-11
    @Override
    public CreateAutoResponse actualizarDisponibilidad(Long id, UpdateAutoRequest request) {
        // update parcial: solo el campo disponibilidad; 404 si el auto no existe
        Auto autoGuardado = autoRepository.actualizarParcial(
                Auto.builder().idAuto(id).disponibilidad(request.getDisponibilidad()).build());

        CreateAutoResponse response = new CreateAutoResponse();
        response.setIdAuto(autoGuardado.getIdAuto());
        response.setDisponibilidad(autoGuardado.getDisponibilidad());
        return response;
    }

    // HU-12 (Cardona): obtiene el detalle completo del auto, con precio calculado
    @Override
    public CreateDetalle_autoResponse getAutoById(Long id) {

        Auto auto = autoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auto no encontrado con id " + id));

        if (auto.getDetalles() == null) {
            throw new ResourceNotFoundException("No se encontraron detalles para el auto con id " + id);
        }

        return AutoMapper.entityToCreateDetalle_autoResponse(auto, auto.getDetalles());
    }

    // HU-08 (Cifuentes): registra el auto con su ficha comercial. Es UN solo documento, asi que se
    // guarda con una sola escritura atomica (la placa unica se reserva en la misma transaccion).
    @Override
    public CreateAutoResponse createAuto(CreateAutoRequest request) {

        // la tienda y la categoria deben existir (404 si no)
        tiendaService.getTiendaById(request.getIdTienda());
        categoriaService.obtenerCategoria(request.getIdCategoria());

        // la placa es unica (consulta amistosa; la garantia real es el candado atomico del repository)
        if (autoRepository.existsByPlaca(request.getPlaca())) {
            throw new BadRequestException("La placa ya esta registrada");
        }

        // nace disponible, con su detalle anidado
        Auto auto = autoRepository.save(AutoMapper.createAutoRequestToEntity(request));

        return AutoMapper.entityToCreateAutoResponseWithDetalles(auto, auto.getDetalles());
    }

    //metodo para actualizar atributos
    @Override
    public UpdateAutoResponse updateAuto(Long id, UpdateAutoRequest updateAutoRequest) {

        //busca auto por id, 404 si no existe
        autoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auto no encontrado con id " + id));

        //la tienda y la categoria nuevas, si llegan, deben existir
        if (updateAutoRequest.getIdTienda() != null) {
            tiendaService.getTiendaById(updateAutoRequest.getIdTienda());
        }
        if (updateAutoRequest.getIdCategoria() != null) {
            categoriaService.obtenerCategoria(updateAutoRequest.getIdCategoria());
        }

        //update parcial: solo cambian los campos que llegan con valor
        Auto auto = autoRepository.actualizarParcial(Auto.builder()
                .idAuto(id)
                .disponibilidad(updateAutoRequest.getDisponibilidad())
                .idTienda(updateAutoRequest.getIdTienda())
                .idCategoria(updateAutoRequest.getIdCategoria())
                .build());

        return AutoMapper.entityToUpdateAutoResponse(auto);
    }


    //metodo para eliminar auto
    // HU-13 (Cardona)
    @Override
    public void deleteAuto(Long id) {

        //busca auto por id, 404 si no existe
        Auto auto = autoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auto no encontrado con id " + id));

        //bloquea borrado si esta alquilado
        if (Boolean.FALSE.equals(auto.getDisponibilidad())) {
            throw new BadRequestException("El auto esta alquilado, no se puede eliminar");
        }

        //bloquea si tiene alquileres asociados (DynamoDB no tiene llaves foraneas: se valida aqui)
        if (alquilerRepository.existsByIdAuto(id)) {
            throw new BadRequestException("El auto tiene alquileres registrados, no se puede eliminar");
        }

        //borra el auto (y libera su placa) en una sola transaccion
        autoRepository.delete(auto);
    }

}
