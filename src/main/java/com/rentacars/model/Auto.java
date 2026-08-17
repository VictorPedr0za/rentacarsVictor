package com.rentacars.model;

import jakarta.persistence.*;
import lombok.*;

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
@Builder

public class Auto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auto")
    private Long idAuto;

    @Column(nullable = false)
    private Boolean disponibilidad;

    @ManyToOne
    @JoinColumn(
            name = "id_tienda",
            nullable = false
    )
    private Tienda tienda;


    @ManyToOne
    @JoinColumn(
            name = "id_categoria",
            nullable = false
    )
    private Categoria categoria;
}
