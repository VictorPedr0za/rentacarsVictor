package com.rentacars.service.impl;

import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.model.Auto;
import com.rentacars.model.Detalle_auto;
import com.rentacars.repository.AutoRepository;
import com.rentacars.repository.Detalle_autoRepository;
import com.rentacars.service.AutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
public class AutoServiceImpl implements AutoService {

    private final AutoRepository autoRepository;
    private final Detalle_autoRepository detalleAutoRepository;

    /**
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

}
