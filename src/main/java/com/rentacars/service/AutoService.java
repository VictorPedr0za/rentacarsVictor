package com.rentacars.service;

import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;
import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.dto.response.UpdateAutoResponse;

import java.util.List;

public interface AutoService {

    CreateAutoResponse createAuto(CreateAutoRequest createAutoRequest) throws Exception;

    //get all
    List<CreateAutoResponse> getAllAutos();

    //get by id
    CreateAutoResponse getAutoById(Long id);

    //put
    UpdateAutoResponse updateAuto(Long id, UpdateAutoRequest updateAutoRequest) throws Exception;

    //delete
    void deleteAuto(Long id) throws Exception;


}
