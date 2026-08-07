package com.rentacars.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla "alquileres".
 *
 * SOBRE EL CAMPO estado:
 * Solo admite "ACTIVO" o "CERRADO" (la BD tiene un CHECK que lo obliga).
 *   - HU-18 crea el alquiler con estado = "ACTIVO"
 *   - HU-24 lo pasa a "CERRADO" al registrar la devolucion
 *   - HU-21 lista solo los que estan en "ACTIVO"
 * Si guardan cualquier otro texto, PostgreSQL rechaza el insert.
 *
 * RESTRICCIONES QUE YA VALIDA LA BASE DE DATOS:
 *   - fecha_fin >= fecha_inicio
 *   - precio_total >= 0
 * Aun asi conviene validarlas en el service para devolver un 400 con
 * mensaje entendible en vez de un error de PostgreSQL.
 *
 * precio_total es DECIMAL(10,2) -> BigDecimal en Java (ver Detalle_auto
 * para los ejemplos de como se hacen los calculos).
 */
@Entity
@Table(name = "alquileres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alquiler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alquiler")
    private Long idAlquiler;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "id_auto", nullable = false)
    private Long idAuto;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "precio_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal;

    @Column(name = "ciudad_retirada", nullable = false, length = 50)
    private String ciudadRetirada;

    @Column(name = "ciudad_devolucion", nullable = false, length = 50)
    private String ciudadDevolucion;

    /** Solo "ACTIVO" o "CERRADO" */
    @Column(nullable = false, length = 20)
    private String estado;
}
