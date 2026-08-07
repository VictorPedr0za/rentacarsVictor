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
 * Tabla "autos".
 *
 * MUY IMPORTANTE -- ESTA TABLA TIENE SOLO 4 COLUMNAS.
 * La marca, el modelo, el anio, la placa, el precio y la imagen NO estan aqui:
 * viven en la tabla detalles_autos (clase Detalle_auto).
 *
 * "autos"          -> la unidad fisica y sus relaciones (que tienda, que categoria,
 *                     si esta disponible)
 * "detalles_autos" -> la ficha comercial del vehiculo
 *
 * Por eso HU-08 (registrar auto con detalles) guarda en DOS tablas, y HU-12
 * (ver detalle completo) tiene que leer de las DOS y combinarlas.
 *
 * SOBRE LAS LLAVES FORANEAS (idTienda, idCategoria):
 * Se guardan como numeros simples (Long), no como objetos Tienda/Categoria.
 * Es la forma mas sencilla, coincide con los DTOs del backlog
 * ("id_tienda": 1) y evita errores tipicos de JPA como
 * LazyInitializationException o JSON infinito.
 */
@Entity
@Table(name = "autos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Auto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auto")
    private Long idAuto;

    // En la BD tiene DEFAULT TRUE, pero HU-08 debe ponerlo en true
    // explicitamente al crear el auto.
    @Column(nullable = false)
    private Boolean disponibilidad;

    @Column(name = "id_tienda", nullable = false)
    private Long idTienda;

    @Column(name = "id_categoria", nullable = false)
    private Long idCategoria;
}
