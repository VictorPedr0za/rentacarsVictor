package com.rentacars.service;

import com.rentacars.dto.request.CreateAlquilerRequest;
import com.rentacars.dto.response.CreateAlquilerResponse;
import com.rentacars.dto.request.UpdateAlquilerRequest;
import com.rentacars.dto.response.UpdateAlquilerResponse;

import java.util.List;

public interface AlquilerService {

    CreateAlquilerResponse createAlquiler(CreateAlquilerRequest createAlquilerRequest) throws Exception;

    //get all
    List<CreateAlquilerResponse> getAllAlquileres();

    //get by id
    CreateAlquilerResponse getAlquilerById(Long id);

    //put
    UpdateAlquilerResponse updateAlquiler(Long id, UpdateAlquilerRequest updateAlquilerRequest) throws Exception;

    //delete
    void deleteAlquiler(Long id) throws Exception;


}
