package com.rentacars.mapper;

import com.rentacars.dto.response.AutoDetailResponse;
import com.rentacars.model.Auto;
import com.rentacars.model.Detalle_auto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Utilidades de mapeo del dominio Auto. */
public final class AutoMapper {
    private AutoMapper() {}

    public static AutoDetailResponse toDetailResponse(Auto auto, Detalle_auto detalle) {
        BigDecimal oferta = detalle.getOfertaPorcentaje() == null
                ? BigDecimal.ZERO : detalle.getOfertaPorcentaje();
        BigDecimal descuento = detalle.getPrecioDia().multiply(oferta)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return AutoDetailResponse.builder()
                .idAuto(auto.getIdAuto())
                .modelo(detalle.getModelo())
                .marca(detalle.getMarca())
                .anio(detalle.getAnio())
                .placa(detalle.getPlaca())
                .precioDia(detalle.getPrecioDia())
                .ofertaPorcentaje(detalle.getOfertaPorcentaje())
                .precioConOferta(detalle.getPrecioDia().subtract(descuento))
                .disponibilidad(auto.getDisponibilidad())
                .build();
    }
}
