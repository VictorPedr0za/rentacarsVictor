package com.rentacars.service.impl;


import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;
import com.rentacars.dto.response.UpdateAutoResponse;
import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.mapper.AutoMapper;
import com.rentacars.model.Auto;
import com.rentacars.model.Detalle_auto;
import com.rentacars.repository.AutoRepository;
import com.rentacars.repository.Detalle_autoRepository;
import com.rentacars.service.AutoService;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;
import com.rentacars.dto.response.CreateDetalle_autoResponse;
import com.rentacars.dto.response.UpdateAutoResponse;
import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.mapper.AutoMapper;
import com.rentacars.model.Auto;
import com.rentacars.model.Detalle_auto;
import com.rentacars.repository.AutoRepository;
import com.rentacars.repository.Detalle_autoRepository;
import com.rentacars.service.AutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


import java.util.List;

import java.util.List;
@Service
@RequiredArgsConstructor
public class AutoServiceImpl implements AutoService {

    private final AutoRepository autoRepository;
    private final Detalle_autoRepository detalleAutoRepository;


    //obtiene lista autos
    @Override
    public List<CreateAutoResponse> getAllAutos() {

        List<Auto> autos = autoRepository.findAll();
        List<CreateAutoResponse> createAutoResponseList = AutoMapper.entityToListCreateAutoResponse(autos);
        return createAutoResponseList;

    }
  
    //HU-09  
  @Override
    public List<CreateAutoResponse> buscarAutos(String ciudad, Long idCategoria) {
        List<Auto> autos = autoRepository.buscarDisponibles(ciudad, idCategoria);

        return autos.stream()
                .map(auto -> {
                    Detalle_auto detalle = detalleAutoRepository.findByIdAuto(auto.getIdAuto())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Detalle no encontrado para auto ID: " + auto.getIdAuto()));

                    CreateAutoResponse response = new CreateAutoResponse();
                    response.setIdAuto(auto.getIdAuto());
                    response.setDisponibilidad(auto.getDisponibilidad());
                    response.setModelo(detalle.getModelo());
                    response.setMarca(detalle.getMarca());
                    response.setPrecioDia(detalle.getPrecioDia());
                    response.setOfertaPorcentaje(detalle.getOfertaPorcentaje());
                    return response;
                })
                .toList();
    }
    
    /*
     * HU-11: Actualizar disponibilidad de un auto.
     * Regla: si el auto no existe -> 404 Not Found.
    */ 
 
    //HU-11
   @Override
   @Transactional
   public CreateAutoResponse actualizarDisponibilidad (Long id, UpdateAutoRequest request){
       Auto auto = autoRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Auto no encontrado con ID:"+ id));
       auto.setDisponibilidad(request.getDisponibilidad());
       Auto autoGuardado = autoRepository.save(auto);

       CreateAutoResponse response = new CreateAutoResponse();
       response.setIdAuto(autoGuardado.getIdAuto());
       response.setDisponibilidad(autoGuardado.getDisponibilidad());
       return response;
   }

    // HU-12 (Cardona): obtiene el detalle completo del auto (autos + detalles_autos), con precio calculado
    @Override
    public CreateDetalle_autoResponse getAutoById(Long id) {

        Auto auto = autoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auto no encontrado con id " + id));

        Detalle_auto detalle = detalleAutoRepository.findByIdAuto(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontraron detalles para el auto con id " + id));

        return AutoMapper.entityToCreateDetalle_autoResponse(auto, detalle);
    }

    //crea auto
    @Override
    public CreateAutoResponse createAuto(CreateAutoRequest createAutoRequest) throws Exception {

        try {
            if (createAutoRequest == null) {
                throw new Exception("El objeto CreateAutoRequest no puede ser nulo");
            }

            if (createAutoRequest.getIdTienda() == null || createAutoRequest.getIdTienda() <= 0) {
                throw new Exception("El idTienda es requerido y debe ser mayor a 0");
            }

            if (createAutoRequest.getIdCategoria() == null || createAutoRequest.getIdCategoria() <= 0) {
                throw new Exception("El idCategoria es requerido y debe ser mayor a 0");
            }

            if (createAutoRequest.getDisponibilidad() == null) {
                throw new Exception("La disponibilidad es requerida");
            }

            Auto auto = Auto.builder()
                    .disponibilidad(createAutoRequest.getDisponibilidad())
                    .idTienda(createAutoRequest.getIdTienda())
                    .idCategoria(createAutoRequest.getIdCategoria())
                    .build();

            auto = autoRepository.save(auto);

            return AutoMapper.entityToCreateAutoResponse(auto);

        } catch (Exception e) {
            throw e;
        }
    }

    //metodo para actualizar atributos
    @Override
    public UpdateAutoResponse updateAuto(Long id, UpdateAutoRequest updateAutoRequest) throws Exception {

        try {


            // Validar id no nulo
            if (id == null){
                throw new Exception("El objeto Auto debe existir");
            }


            //valida request no nulo
            if (updateAutoRequest == null){
                throw new Exception("El objeto UpdateAutoRequest no puede ser nulo");
            }

            //busca auto por id
            Auto auto = autoRepository.findById(id).orElseThrow(() -> new RuntimeException("Auto not found with id; " + id));

            //actualiza disponibilidad
            if (updateAutoRequest.getDisponibilidad() != null) {
                auto.setDisponibilidad(updateAutoRequest.getDisponibilidad());
            }

            //actualiza tienda
            if (updateAutoRequest.getIdTienda() != null) {
                auto.setIdTienda(updateAutoRequest.getIdTienda());
            }

            //actualiza categoria
            if (updateAutoRequest.getIdCategoria() != null) {
                auto.setIdCategoria(updateAutoRequest.getIdCategoria());
            }

            //guarda entidad actualizada
            auto = autoRepository.save(auto);

            //convierte a update response
            UpdateAutoResponse response = AutoMapper.entityToUpdateAutoResponse(auto);

            //retorna dto
            return response;

        } catch (Exception e) {
            throw e;
        }
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

        //borra detalle antes del auto
        detalleAutoRepository.findByIdAuto(id)
                .ifPresent(detalleAutoRepository::delete);

        //borra el auto al final
        autoRepository.delete(auto);
    }

  

}