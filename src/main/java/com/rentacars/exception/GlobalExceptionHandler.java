package com.rentacars.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    /**
     * El JSON viene mal formado, o un campo trae un tipo/formato invalido
     * (ej. "fecha_inicio": "manana" o un texto donde va un numero) -> 400 Bad Request.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        return construir(HttpStatus.BAD_REQUEST,
                "El cuerpo de la peticion no es un JSON valido o tiene campos con formato incorrecto");
    }

    /** Falta un @RequestParam obligatorio (ej. id_cliente en HU-20) -> 400 Bad Request */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> manejarParametroFaltante(MissingServletRequestParameterException ex) {
        return construir(HttpStatus.BAD_REQUEST,
                "El parametro '" + ex.getParameterName() + "' es obligatorio");
    }

    /** Un @PathVariable o @RequestParam no se puede convertir (ej. /autos/abc) -> 400 Bad Request */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> manejarTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return construir(HttpStatus.BAD_REQUEST,
                "El valor del parametro '" + ex.getName() + "' no es valido");
    }

    /**
     * La base de datos rechazo la operacion por una restriccion (llave foranea o UNIQUE):
     * por ejemplo borrar una tienda que aun tiene autos -> 409 Conflict.
     * Es la red de seguridad; las reglas conocidas se validan antes en el service con un 400 claro.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> manejarIntegridad(DataIntegrityViolationException ex) {
        return construir(HttpStatus.CONFLICT,
                "La operacion viola una restriccion de datos: el registro esta relacionado con otros o repite un valor unico");
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus estado, String mensaje) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                estado.value(),
                estado.getReasonPhrase(),
                mensaje);
        return ResponseEntity.status(estado).body(error);
    }
}
