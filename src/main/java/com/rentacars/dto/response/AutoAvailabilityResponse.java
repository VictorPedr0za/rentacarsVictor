package com.rentacars.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AutoAvailabilityResponse {
    private Long idAuto;
    private Boolean disponibilidad;
}
