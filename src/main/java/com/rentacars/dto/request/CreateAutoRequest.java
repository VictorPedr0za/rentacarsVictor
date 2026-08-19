package com.rentacars.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//valid
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * HU-08 (Cifuentes): Registrar auto con detalles -- implementado por Claude.
 *
 *   POST /autos
 *   { "modelo": "Tucson", "marca": "Hyundai", "anio": "2023", "placa": "ABC123",
 *     "precio_dia": 150000, "oferta_porcentaje": 10,
 *     "imagen": "https://url.com/img.jpg", "id_tienda": 1, "id_categoria": 1 }
 *
 * Corregido respecto a la version anterior: este DTO solo tenia
 * disponibilidad/idTienda/idCategoria y AutoServiceImpl.createAuto()
 * guardaba unicamente la fila de "autos", sin crear nunca el
 * "detalles_autos" que pide la HU (ver EMPEZAR_AQUI.md 2bis-a: autos y
 * detalles_autos son tablas distintas). Por eso aqui se agregan los
 * campos de la ficha comercial, y "disponibilidad" ya NO se recibe del
 * cliente: la regla de negocio dice que siempre inicia en true.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateAutoRequest {

    //valida id de tienda requerido
    @NotNull(message = "El id de la tienda es requerido")
    private Long idTienda;

    //valida id de categoria requerido
    @NotNull(message = "El id de la categoria es requerido")
    private Long idCategoria;

    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotBlank(message = "El anio es obligatorio")
    private String anio;

    @NotBlank(message = "La placa es obligatoria")
    private String placa;

    @NotNull(message = "El precioDia es obligatorio")
    @Positive(message = "El precioDia debe ser mayor a 0")
    private BigDecimal precioDia;

    // Puede venir null (auto sin oferta); si viene, debe estar entre 0 y 100.
    @DecimalMin(value = "0", message = "La ofertaPorcentaje debe estar entre 0 y 100")
    @DecimalMax(value = "100", message = "La ofertaPorcentaje debe estar entre 0 y 100")
    private BigDecimal ofertaPorcentaje;

    private String imagen;
}
