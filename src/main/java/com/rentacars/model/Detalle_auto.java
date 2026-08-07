package com.rentacars.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla "detalles_autos".
 *
 * Aqui vive TODA la ficha del vehiculo: marca, modelo, anio, placa,
 * precio por dia, oferta e imagen. Se relaciona con Auto por idAuto.
 *
 * SOBRE BigDecimal:
 * precio_dia y oferta_porcentaje son DECIMAL(10,2) en PostgreSQL, asi que
 * en Java se usa BigDecimal (NO double). Con dinero, double da errores de
 * redondeo. La diferencia practica es que no se usan los operadores * - + :
 *
 *     double:     total = precio * dias;
 *     BigDecimal: total = precio.multiply(BigDecimal.valueOf(dias));
 *
 * Operaciones que van a necesitar:
 *     a.add(b)                    a + b
 *     a.subtract(b)               a - b
 *     a.multiply(b)               a * b
 *     a.divide(b, 2, RoundingMode.HALF_UP)   a / b con 2 decimales
 *     BigDecimal.valueOf(100)     convertir un numero normal a BigDecimal
 *
 * Ejemplo real, el calculo de HU-12 (precio con oferta):
 *
 *     BigDecimal oferta = detalle.getOfertaPorcentaje();
 *     if (oferta == null) oferta = BigDecimal.ZERO;   // la columna admite null
 *
 *     BigDecimal descuento = detalle.getPrecioDia()
 *             .multiply(oferta)
 *             .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
 *
 *     BigDecimal precioConOferta = detalle.getPrecioDia().subtract(descuento);
 */
@Entity
@Table(name = "detalles_autos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Detalle_auto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalles_autos")
    private Long idDetallesAutos;

    @Column(length = 2000)
    private String imagen;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 4)
    private String anio;

    @Column(nullable = false, unique = true, length = 20)
    private String placa;

    // La BD exige precio_dia > 0
    @Column(name = "precio_dia", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioDia;

    // PUEDE SER NULL (auto sin oferta). Siempre revisar null antes de calcular.
    // La BD exige que este entre 0 y 100.
    @Column(name = "oferta_porcentaje", precision = 10, scale = 2)
    private BigDecimal ofertaPorcentaje;

    @Column(name = "id_auto", nullable = false)
    private Long idAuto;
}
