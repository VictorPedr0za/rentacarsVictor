package com.rentacars.dto.response;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateAutoResponse {

    //atributos a actualizar

    private Long idAuto;
    private Boolean disponibilidad;
    private Long idTienda;
    private Long idCategoria;

}
