package com.rentacars.controller;

import com.rentacars.dto.request.CreateTiendaRequest;
import com.rentacars.dto.response.CreateTiendaResponse;
import com.rentacars.service.TiendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PLANTILLA DE CONTROLLER -- copien este patron para las demas HU.
 *
 * EL CONTROLLER ES LA PUERTA DE ENTRADA. Su unico trabajo es:
 *   1. Recibir la peticion HTTP
 *   2. Pasarsela al service
 *   3. Devolver la respuesta con el codigo HTTP correcto
 *
 * NO lleva ifs, NO lleva calculos, NO habla con el repository.
 * Si estan escribiendo logica aqui, va en el ServiceImpl.
 *
 * ANOTACIONES QUE APARECEN:
 *
 *   @RestController
 *       "Esta clase atiende peticiones HTTP y devuelve JSON".
 *
 *   @RequestMapping("/tiendas")
 *       Prefijo comun de todas las rutas de esta clase.
 *
 *   @PostMapping / @GetMapping / @PutMapping / @DeleteMapping / @PatchMapping
 *       El verbo HTTP de cada endpoint.
 *
 *   @RequestBody
 *       "Convierte el JSON que llega en el cuerpo a un objeto Java".
 *
 *   @Valid
 *       "Antes de entrar al metodo, revisa las anotaciones @NotBlank del DTO".
 *       SIN ESTA ANOTACION LAS VALIDACIONES NO SE EJECUTAN. Es el olvido
 *       mas comun: el DTO tiene @NotBlank pero el controller no puso @Valid.
 *
 * IMPORTANTE: este archivo lo van a editar Arango y Corrales.
 * Cada quien agrega SOLO su metodo.
 */
@RestController
@RequestMapping("/tiendas")
@RequiredArgsConstructor
public class TiendaController {

    // Depende de la INTERFAZ TiendaService, no de TiendaServiceImpl.
    // Eso es lo que pide el principio D de SOLID.
    private final TiendaService tiendaService;

    /**
     * HU-01: Registrar tienda.
     *
     *   POST /tiendas
     *   { "nombre": "Tienda Norte", "ciudad": "Bogota", "direccion": "Calle 100 #15-20" }
     *
     * Devuelve 201 Created, que es el codigo correcto al crear un recurso nuevo.
     */
    @PostMapping
    public ResponseEntity<CreateTiendaResponse> crearTienda(
            @Valid @RequestBody CreateTiendaRequest request) {

        CreateTiendaResponse response = tiendaService.crearTienda(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarTienda(@PathVariable Long id) {
        tiendaService.eliminarTienda(id);
        return ResponseEntity.ok("Reserva eliminada exitosamente");
    }
    // ------------------------------------------------------------------
    // PENDIENTES EN ESTE ARCHIVO:
    //
    //   HU-02 (Arango)   -> @PutMapping("/{id}")     actualizar tienda
    //   HU-03 (Arango)   -> @GetMapping              listar por ciudad (@RequestParam)
    //   HU-04 (Corrales) -> @DeleteMapping("/{id}")  eliminar -> 204 No Content
    //   HU-05 (Corrales) -> @GetMapping("/{id}")     consultar por id
    //
    // Ejemplo de como recibir el id de la URL:
    //   public ResponseEntity<...> obtenerTienda(@PathVariable Long id) { ... }
    //
    // Ejemplo de como recibir un parametro opcional (?ciudad=Bogota):
    //   public ResponseEntity<...> listar(
    //           @RequestParam(required = false) String ciudad) { ... }
    // ------------------------------------------------------------------
}
