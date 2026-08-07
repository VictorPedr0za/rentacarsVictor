package com.rentacars.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * ESTA CLASE SE ESCRIBE UNA SOLA VEZ PARA TODO EL PROYECTO. NO LA DUPLIQUEN.
 *
 * @RestControllerAdvice significa: "vigila TODOS los controllers".
 * Cuando en cualquier parte del codigo se lanza una excepcion, esta clase
 * la atrapa y la convierte en una respuesta HTTP con el codigo correcto.
 *
 * Gracias a esto, en los services solo hay que escribir:
 *     throw new ResourceNotFoundException("...");   -> el cliente recibe 404
 *     throw new BadRequestException("...");         -> el cliente recibe 400
 *
 * Y en los controllers NUNCA hay que hacer:
 *     return ResponseEntity.status(404).body(...)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** El recurso no existe -> 404 Not Found */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /** Se rompio una regla de negocio -> 400 Bad Request */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> manejarPeticionInvalida(BadRequestException ex) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Falto un campo obligatorio o llego con formato invalido -> 400 Bad Request.
     * Esta se dispara sola cuando el controller usa @Valid y el DTO tiene
     * @NotBlank / @NotNull / @Email. Nadie tiene que lanzarla a mano.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                mensaje);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
