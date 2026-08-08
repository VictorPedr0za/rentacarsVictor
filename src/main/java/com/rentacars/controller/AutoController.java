package com.rentacars.controller;

import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;
import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.dto.response.UpdateAutoResponse;
import com.rentacars.service.AutoService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//importa el valid
import jakarta.validation.Valid;

//importa para agregar documentacion de swagger
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/autos")
@Tag(name = "autos", description = "operaciones de autos")
public class AutoController {


    private final AutoService autoService;

    @GetMapping("/ping")
    @Operation(summary = "verificar autos")
    public String ping() {
        return "pong";
    }


    //obtiene lista
    @GetMapping("/all")
    @Operation(summary = "listar autos")
    public List<CreateAutoResponse> getAllAutos(){

        return autoService.getAllAutos();

    }

    //obtiene por id
    @GetMapping("/{id}")
    @Operation(summary = "buscar auto por id")
    public ResponseEntity<CreateAutoResponse> getAutoById(@PathVariable Long id){

        CreateAutoResponse autoResponse = autoService.getAutoById(id);

        return new ResponseEntity<>(
                autoResponse,
                HttpStatus.CREATED
        );

    }

    //hace post
    @PostMapping("/create")
    @Operation(summary = "crear auto")
    public ResponseEntity<CreateAutoResponse> createAuto(
            @Valid @RequestBody CreateAutoRequest createAutoRequest
    ) throws Exception {

        CreateAutoResponse autoCreated = autoService.createAuto(createAutoRequest);

        return new ResponseEntity<>(
                autoCreated,
                HttpStatus.CREATED
        );
    }

    //actualizar segun id
    @PutMapping("/update/{id}")
    @Operation(summary = "actualizar auto")
    public ResponseEntity<UpdateAutoResponse> updateAuto(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAutoRequest updateAutoRequest
    ) throws Exception {

        //llama update en service
        UpdateAutoResponse autoUpdated = autoService.updateAuto(id, updateAutoRequest);

        //retorna response
        return new ResponseEntity<>(
                autoUpdated,
                HttpStatus.CREATED
        );
    }

    //elimina auto
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "eliminar auto")
    public ResponseEntity<String> deleteAuto(@PathVariable Long id) throws Exception {

        //llama service delete
        autoService.deleteAuto(id);

        //retorna mensaje
        return new ResponseEntity<>(
                "Auto eliminado correctamente",
                HttpStatus.OK
        );
    }

}