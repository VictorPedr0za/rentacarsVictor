package com.rentacars.exception;

/**
 * Lanzar esta excepcion cuando se rompe una REGLA DE NEGOCIO.
 * El GlobalExceptionHandler la convierte automaticamente en 400 Bad Request.
 *
 * Ejemplos de uso:
 *
 *     if (categoriaRepository.existsByNombre(request.getNombre())) {
 *         throw new BadRequestException("Ya existe una categoria con ese nombre");
 *     }
 *
 *     if (!auto.getDisponibilidad()) {
 *         throw new BadRequestException("El auto no esta disponible");
 *     }
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String mensaje) {
        super(mensaje);
    }
}
