package com.rentacars.service.impl;


import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;
import com.rentacars.dto.response.UpdateAutoResponse;
import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.mapper.AutoMapper;
import com.rentacars.model.Auto;
import com.rentacars.repository.AutoRepository;
import com.rentacars.service.AutoService;
import lombok.RequiredArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AutoServiceImpl implements AutoService {

    private final AutoRepository autoRepository;


    //obtiene lista autos
    @Override
    public List<CreateAutoResponse> getAllAutos() {

        List<Auto> autos = autoRepository.findAll();
        List<CreateAutoResponse> createAutoResponseList = AutoMapper.entityToListCreateAutoResponse(autos);
        return createAutoResponseList;

    }

    //obtiene auto segun id
    @Override
    public CreateAutoResponse getAutoById(Long id) {

        Auto auto = autoRepository.findById(id).orElseThrow(() -> new RuntimeException("El ID:  " + id + " .No es valido"));
        CreateAutoResponse createAutoResponse = AutoMapper.entityToCreateAutoResponse(auto);
        return createAutoResponse;
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
    @Override
    public void deleteAuto(Long id) throws Exception {

        try {

            //valida id no nulo
            if (id == null){
                throw new Exception("El id del auto es requerido");
            }

            //busca auto por id
            Auto auto = autoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("El ID:  " + id + " .No es valido"));

            //elimina auto
            autoRepository.delete(auto);

        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * HU-11: Actualizar disponibilidad de un auto.
     * Regla: si el auto no existe -> 404 Not Found.
    */
    @Override
    @Transactional
    public CreateAutoResponse actualizarDisponibilidad (Long id, UpdateAutoRequest request){
         Auto auto = autoRepository.findById(id)
                 .orElseThrow(() -> new ResourceNotFoundException("Auto no encontrado con ID:"+ id));
         auto.setDisponibilidad(request.getDisponibilidad());
         Auto autoGuardado = autoRepository.save(auto);

         return new CreateAutoResponse(autoGuardado.getIdAuto(), autoGuardado.getDisponibilidad());
    }


}


