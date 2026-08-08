package com.rentacars.service.impl;

import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.model.Auto;
import com.rentacars.repository.AutoRepository;
import com.rentacars.service.AutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AutoServiceImpl implements AutoService {

    private final AutoRepository autoRepository;

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
