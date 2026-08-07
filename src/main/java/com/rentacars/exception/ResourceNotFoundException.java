package com.rentacars.exception;

/**
 * Lanzar esta excepcion cuando algo NO EXISTE.
 * El GlobalExceptionHandler la convierte automaticamente en 404 Not Found.
 *
 * Ejemplo de uso dentro de un ServiceImpl:
 *
 *     Tienda tienda = tiendaRepository.findById(id)
 *             .orElseThrow(() -> new ResourceNotFoundException("Tienda no encontrada con id " + id));
 *
 * NO devuelvas ResponseEntity.status(404) a mano desde el controller.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}
