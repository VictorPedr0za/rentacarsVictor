package com.rentacars.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder

public class CreateAutoResponse {

     // DATOS DE AUTO

     // ID generado por la base de datos.

    private Long idAuto;



     // Disponibilidad del auto.

    private Boolean disponibilidad;



     //ID de la tienda.

    private Long idTienda;



     // ID de la categoría.

    private Long idCategoria;



     //DATOS DE DETALLES DEL AUTO

     // Imagen.

    private String imagen;



     // Modelo.

    private String modelo;



     //Marca.

    private String marca;


    //Año.

    private String anio;



     //Placa.

    private String placa;



     //Precio por día.

    private BigDecimal precioDia;



     // Porcentaje de oferta.

    private BigDecimal ofertaPorcentaje;

}
