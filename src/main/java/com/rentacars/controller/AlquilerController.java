package com.rentacars.controller;

import com.rentacars.dto.request.CreateAlquilerRequest;
import com.rentacars.dto.response.CreateAlquilerResponse;
import com.rentacars.dto.request.UpdateAlquilerRequest;
import com.rentacars.dto.response.UpdateAlquilerResponse;
import com.rentacars.exception.BadRequestException;
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

        return new ResponseEntity<>(
                alquilerResponse,
                HttpStatus.CREATED
        );

    }

    /**
     * HU-20 (Pedroza): Historial de alquileres del cliente -- implementado por Claude.
     *   GET /alquileres?id_cliente=1
     * Regla del backlog: id_cliente es obligatorio -> 400 si no se envia.
     */
    @GetMapping
    @Operation(summary = "historial de alquileres de un cliente")
    public ResponseEntity<List<CreateAlquilerResponse>> historialPorCliente(
            @RequestParam(name = "id_cliente", required = false) Long idCliente) {

        if (idCliente == null) {
            throw new BadRequestException("El id_cliente es requerido");
        }
        return ResponseEntity.ok(alquilerService.historialPorCliente(idCliente));
    }

    /**
     * HU-21 (Pedroza): Listar alquileres activos -- implementado por Claude.
     *   GET /alquileres/activos
     */
    @GetMapping("/activos")
    @Operation(summary = "listar alquileres activos")
    public ResponseEntity<List<CreateAlquilerResponse>> listarActivos() {
        return ResponseEntity.ok(alquilerService.listarActivos());
    }

    /**
     * HU-24 (Corrales): Registrar devolucion de auto -- implementado por Claude.
     *   PUT /alquileres/{id}/devolucion
     */
    @PutMapping("/{id}/devolucion")
    @Operation(summary = "registrar devolucion de auto")
    public ResponseEntity<CreateAlquilerResponse> registrarDevolucion(@PathVariable Long id) {
        return ResponseEntity.ok(alquilerService.registrarDevolucion(id));
    }

    //hace post
    @PostMapping("/create")
    @Operation(summary = "crear alquiler")
    public ResponseEntity<CreateAlquilerResponse> createAlquiler(
            @Valid @RequestBody CreateAlquilerRequest createAlquilerRequest
    ) throws Exception {

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
    ) throws Exception {

        //llama update en service
        UpdateAlquilerResponse alquilerUpdated = alquilerService.updateAlquiler(id, updateAlquilerRequest);

        //retorna response
        return new ResponseEntity<>(
                alquilerUpdated,
                HttpStatus.CREATED
        );
    }

    /*
    //elimina alquiler
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "eliminar alquiler")
    public ResponseEntity<String> deleteAlquiler(@PathVariable Long id) throws Exception {

        //llama service delete
        alquilerService.deleteAlquiler(id);

        //retorna mensaje
        return new ResponseEntity<>(
                "Alquiler eliminado correctamente",
                HttpStatus.OK
        );
    }

    */

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

}
