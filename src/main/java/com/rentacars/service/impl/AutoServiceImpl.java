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
import com.rentacars.mapper.Detalle_autoMapper;
import com.rentacars.model.Auto;
import com.rentacars.model.Detalle_auto;
import com.rentacars.repository.AlquilerRepository;
import com.rentacars.repository.AutoRepository;
import com.rentacars.repository.Detalle_autoRepository;
import com.rentacars.service.AutoService;
import com.rentacars.service.CategoriaService;
import com.rentacars.service.TiendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AutoServiceImpl implements AutoService {

    private final AutoRepository autoRepository;
    private final Detalle_autoRepository detalleAutoRepository;
    // valida FK antes de borrar
    private final AlquilerRepository alquilerRepository;

    // HU-08 (v2): valida que la tienda y la categoria existan con una llamada directa (antes era HTTP)
    private final TiendaService tiendaService;
    private final CategoriaService categoriaService;


    //obtiene lista autos, cada uno con su ficha comercial
    @Override
    @Transactional(readOnly = true)
    public List<CreateAutoResponse> getAllAutos() {

        List<Auto> autos = autoRepository.findAll();

        // una sola consulta para todas las fichas
        Map<Long, Detalle_auto> detallesPorAuto = detalleAutoRepository.findAll().stream()
                .collect(Collectors.toMap(Detalle_auto::getIdAuto, Function.identity(), (a, b) -> a));

        return autos.stream()
                .map(auto -> AutoMapper.entityToCreateAutoResponse(auto, detallesPorAuto.get(auto.getIdAuto())))
                .toList();
    }

    //HU-09
    @Override
    @Transactional(readOnly = true)
    public List<CreateAutoResponse> buscarAutos(String ciudad, Long idCategoria) {

        // un string en blanco se trata como "sin filtro"
        String ciudadFiltro = (ciudad == null || ciudad.isBlank()) ? null : ciudad.trim();

        List<Auto> autos = autoRepository.buscarDisponibles(ciudadFiltro, idCategoria);
        if (autos.isEmpty()) {
            return List.of();
        }

        // una sola consulta para las fichas de todos los autos encontrados
        List<Long> ids = autos.stream().map(Auto::getIdAuto).toList();
        Map<Long, Detalle_auto> detallesPorAuto = detalleAutoRepository.findByIdAutoIn(ids).stream()
                .collect(Collectors.toMap(Detalle_auto::getIdAuto, Function.identity(), (a, b) -> a));

        return autos.stream()
                .map(auto -> AutoMapper.entityToCreateAutoResponse(auto, detallesPorAuto.get(auto.getIdAuto())))
                .toList();
    }

    //HU-10
    @Override
    @Transactional
    public CreateAutoResponse actualizarDetalles(Long id, UpdateDetalle_autoRequest request) {
        Auto auto = autoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auto no encontrado con ID: " + id));

        Detalle_auto detalle = detalleAutoRepository.findByIdAuto(id)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle no encontrado para auto ID: " + id));

        if (request.getPrecioDia() != null) {
            detalle.setPrecioDia(request.getPrecioDia());
        }
        if (request.getOfertaPorcentaje() != null) {
            detalle.setOfertaPorcentaje(request.getOfertaPorcentaje());
        }
        if (request.getImagen() != null) {
            detalle.setImagen(request.getImagen());
        }

        Detalle_auto detalleGuardado = detalleAutoRepository.save(detalle);

        CreateAutoResponse response = new CreateAutoResponse();
        response.setIdAuto(auto.getIdAuto());
        response.setDisponibilidad(auto.getDisponibilidad());
        response.setModelo(detalleGuardado.getModelo());
        response.setMarca(detalleGuardado.getMarca());
        response.setPrecioDia(detalleGuardado.getPrecioDia());
        response.setOfertaPorcentaje(detalleGuardado.getOfertaPorcentaje());
        response.setImagen(detalleGuardado.getImagen());
        return response;
    }

    //HU-11
    @Override
    @Transactional
    public CreateAutoResponse actualizarDisponibilidad(Long id, UpdateAutoRequest request) {
        Auto auto = autoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auto no encontrado con ID: " + id));
        auto.setDisponibilidad(request.getDisponibilidad());
        Auto autoGuardado = autoRepository.save(auto);

        CreateAutoResponse response = new CreateAutoResponse();
        response.setIdAuto(autoGuardado.getIdAuto());
        response.setDisponibilidad(autoGuardado.getDisponibilidad());
        return response;
    }

    // HU-12 (Cardona): obtiene el detalle completo del auto (autos + detalles_autos), con precio calculado
    @Override
    @Transactional(readOnly = true)
    public CreateDetalle_autoResponse getAutoById(Long id) {

        Auto auto = autoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auto no encontrado con id " + id));

        Detalle_auto detalle = detalleAutoRepository.findByIdAuto(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontraron detalles para el auto con id " + id));

        return AutoMapper.entityToCreateDetalle_autoResponse(auto, detalle);
    }

    // HU-08 (Cifuentes): registra el auto y su ficha comercial en dos inserts dentro de una sola transaccion
    @Override
    @Transactional
    public CreateAutoResponse createAuto(CreateAutoRequest request) {

        // la tienda y la categoria deben existir (404 si no)
        tiendaService.getTiendaById(request.getIdTienda());
        categoriaService.obtenerCategoria(request.getIdCategoria());

        // la placa es unica
        if (detalleAutoRepository.existsByPlaca(request.getPlaca())) {
            throw new BadRequestException("La placa ya esta registrada");
        }

        // 1. primero el auto (nace disponible) para obtener su id_auto
        Auto auto = autoRepository.save(AutoMapper.createAutoRequestToEntity(request));

        // 2. luego la ficha comercial usando ese id_auto; si falla, se revierte tambien el auto
        Detalle_auto detalle = detalleAutoRepository.save(
                Detalle_autoMapper.createDetalle_autoRequestToEntity(request, auto.getIdAuto()));

        return AutoMapper.entityToCreateAutoResponseWithDetalles(auto, detalle);
    }

    //metodo para actualizar atributos
    @Override
    @Transactional
    public UpdateAutoResponse updateAuto(Long id, UpdateAutoRequest updateAutoRequest) {

        //busca auto por id, 404 si no existe
        Auto auto = autoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auto no encontrado con id " + id));

        //actualiza disponibilidad
        if (updateAutoRequest.getDisponibilidad() != null) {
            auto.setDisponibilidad(updateAutoRequest.getDisponibilidad());
        }

        //actualiza tienda, que debe existir
        if (updateAutoRequest.getIdTienda() != null) {
            tiendaService.getTiendaById(updateAutoRequest.getIdTienda());
            auto.setIdTienda(updateAutoRequest.getIdTienda());
        }

        //actualiza categoria, que debe existir
        if (updateAutoRequest.getIdCategoria() != null) {
            categoriaService.obtenerCategoria(updateAutoRequest.getIdCategoria());
            auto.setIdCategoria(updateAutoRequest.getIdCategoria());
        }

        //guarda entidad actualizada
        auto = autoRepository.save(auto);

        //convierte a update response
        return AutoMapper.entityToUpdateAutoResponse(auto);
    }


    //metodo para eliminar auto
    // HU-13 (Cardona): borra detalle y auto en cascada
    @Override
    @Transactional // une los dos deletes
    public void deleteAuto(Long id) {

        //busca auto por id, 404 si no existe
        Auto auto = autoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auto no encontrado con id " + id));

        //bloquea borrado si esta alquilado
        if (Boolean.FALSE.equals(auto.getDisponibilidad())) {
            throw new BadRequestException("El auto esta alquilado, no se puede eliminar");
        }

        //bloquea si tiene alquileres asociados
        if (alquilerRepository.existsByIdAuto(id)) {
            throw new BadRequestException("El auto tiene alquileres registrados, no se puede eliminar");
        }

        //borra detalle antes del auto
        detalleAutoRepository.findByIdAuto(id)
                .ifPresent(detalleAutoRepository::delete);

        //borra el auto al final
        autoRepository.delete(auto);
    }

}
