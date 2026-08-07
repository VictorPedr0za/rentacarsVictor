package com.rentacars.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Tabla "clientes".
 *
 * OJO (HU-14, HU-15, HU-16): la tarjeta de credito SE GUARDA aqui,
 * pero NUNCA se devuelve completa en un DTO de respuesta.
 * En HU-16 se muestra enmascarada: ************1111
 *
 * La columna email si tiene UNIQUE en la base de datos, pero HU-14 igual
 * debe validarlo con existsByEmail() para responder un 400 con el mensaje
 * "El email ya existe" en vez de un error feo de PostgreSQL.
 */
@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String telefono;

    // En la BD admite null, aunque HU-14 la exige al registrar
    @Column(name = "tarjeta_credito", length = 20)
    private String tarjetaCredito;
}
