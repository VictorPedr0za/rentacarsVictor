package com.rentacars.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
*   HU-11 (Suarez) -> idAuto, disponibilidad
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAutoResponse {

    private Long idAuto;
    private Boolean disponibilidad;

}
