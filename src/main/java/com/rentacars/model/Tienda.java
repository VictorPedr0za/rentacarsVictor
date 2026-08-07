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
 * Tabla "tiendas" (ver script_bd.sql).
 *
 * Entidad JPA = una clase que representa una tabla de la base de datos.
 * Cada atributo de la clase es una columna de la tabla.
 *
 * OJO: la app corre en modo validate. Si cambias un campo aqui sin
 * cambiar la tabla en script_bd.sql, la aplicacion NO arranca.
 */
@Entity                      // Le dice a Spring: "esto es una tabla"
@Table(name = "tiendas")     // Nombre exacto de la tabla en PostgreSQL
@Getter                      // Lombok genera los getters (getNombre(), etc.)
@Setter                      // Lombok genera los setters (setNombre(), etc.)
@NoArgsConstructor           // Constructor vacio: new Tienda()  -- JPA lo exige
@AllArgsConstructor          // Constructor con todos los campos
public class Tienda {

    @Id                                                  // Llave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // BIGSERIAL: la BD asigna el id sola
    @Column(name = "id_tienda")
    private Long idTienda;

    @Column(nullable = false, length = 70)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String ciudad;

    @Column(nullable = false, length = 70)
    private String direccion;
}
