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
 * Tabla "categorias". Clasifica los autos (SUV, Sedan, Camioneta...).
 *
 * OJO HU-06: la columna nombre NO tiene restriccion UNIQUE en la base de datos.
 * La regla "no se pueden registrar dos categorias con el mismo nombre" hay que
 * validarla en CategoriaServiceImpl con existsByNombre(), lanzando
 * BadRequestException. Si se deja solo a la BD, no se cumple.
 */
@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long idCategoria;

    @Column(nullable = false, length = 50)
    private String nombre;

    // En PostgreSQL es TEXT (sin limite de longitud)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;
}
