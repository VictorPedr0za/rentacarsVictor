package com.rentacars.exception;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Formato unico de respuesta para TODOS los errores de la API.
 * Asi el que consuma la API siempre recibe la misma estructura:
 *
 * {
 *   "fecha": "2026-08-07T15:30:00",
 *   "estado": 404,
 *   "error": "Not Found",
 *   "mensaje": "Tienda no encontrada con id 99"
 * }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime fecha;
    private int estado;
    private String error;
    private String mensaje;
}
