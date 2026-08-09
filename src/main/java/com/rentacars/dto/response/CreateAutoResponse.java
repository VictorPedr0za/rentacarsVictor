package com.rentacars.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
*   HU-11 (Suarez) -> idAuto, disponibilidad
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAutoResponse {

    private Long idAuto;
    private Boolean disponibilidad;
    private Long idTienda;
    private Long idCategoria;


    public CreateAutoResponse(Long idAuto, Boolean disponibilidad) {
    }
}



