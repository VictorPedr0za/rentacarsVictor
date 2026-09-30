package com.rentacars.controller;

import com.rentacars.dto.request.CreateAlquilerRequest;
import com.rentacars.dto.response.CreateAlquilerResponse;
import com.rentacars.dto.request.UpdateAlquilerRequest;
import com.rentacars.dto.response.UpdateAlquilerResponse;
import com.rentacars.service.AlquilerService;
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
@RequestMapping("/alquileres")
@Tag(name = "alquileres", description = "operaciones de alquileres")
public class AlquilerController {


    private final AlquilerService alquilerService;

    @GetMapping("/ping")
    @Operation(summary = "verificar alquileres")
    public String ping() {
        return "pong";
    }


    //obtiene lista
    @GetMapping("/all")
    @Operation(summary = "listar alquileres")
    public List<CreateAlquilerResponse> getAllAlquileres(){

        return alquilerService.getAllAlquileres();

    }

    //obtiene por id
    @GetMapping("/{id}")
    @Operation(summary = "buscar alquiler por id")
    public ResponseEntity<CreateAlquilerResponse> getAlquilerById(@PathVariable Long id){

        CreateAlquilerResponse alquilerResponse = alquilerService.getAlquilerById(id);

        return ResponseEntity.ok(alquilerResponse);

    }

    //hace post
    // HU-18 (Pedroza): ruta del backlog POST /alquileres; "/create" se conserva porque el frontend aun la usa
    @PostMapping({"", "/create"})
    @Operation(summary = "crear alquiler")
    public ResponseEntity<CreateAlquilerResponse> createAlquiler(
            @Valid @RequestBody CreateAlquilerRequest createAlquilerRequest
    ) {

        CreateAlquilerResponse alquilerCreated = alquilerService.createAlquiler(createAlquilerRequest);

        return new ResponseEntity<>(
                alquilerCreated,
                HttpStatus.CREATED
        );
    }

    //actualizar segun id
    @PutMapping("/update/{id}")
    @Operation(summary = "actualizar alquiler")
    public ResponseEntity<UpdateAlquilerResponse> updateAlquiler(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAlquilerRequest updateAlquilerRequest
    ) {

        //llama update en service
        UpdateAlquilerResponse alquilerUpdated = alquilerService.updateAlquiler(id, updateAlquilerRequest);

        //retorna response
        return ResponseEntity.ok(alquilerUpdated);
    }

    @PutMapping("/{id}/devolucion")
    @Operation(summary = "registrar devolucion de auto")
    public ResponseEntity<CreateAlquilerResponse> registrarDevolucion(@PathVariable Long id) {
        return ResponseEntity.ok(alquilerService.registrarDevolucion(id));
    }

    //elimina alquiler
    // HU-22 (Cardona): ruta y codigo del backlog
    @DeleteMapping("/{id}")
    @Operation(summary = "cancelar alquiler")
    public ResponseEntity<Void> deleteAlquiler(@PathVariable Long id) {

        //llama service delete
        alquilerService.deleteAlquiler(id);

        //devuelve 204 sin contenido
        return ResponseEntity.noContent().build();
    }



    // HU-20 (Pedroza): historial de alquileres de un cliente
    @GetMapping
    @Operation(summary = "historial de alquileres de un cliente")
    public List<CreateAlquilerResponse> historialPorCliente(
            @RequestParam("id_cliente") Long idCliente
    ) {

        //llama service con el id del cliente
        return alquilerService.historialPorCliente(idCliente);
    }

    // HU-21 (Pedroza): lista los alquileres activos
    @GetMapping("/activos")
    @Operation(summary = "listar alquileres activos")
    public List<CreateAlquilerResponse> listarActivos() {

        //llama service, filtra activos
        return alquilerService.listarActivos();
    }
}
