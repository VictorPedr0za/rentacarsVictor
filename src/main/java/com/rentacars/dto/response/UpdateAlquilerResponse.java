package com.rentacars.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateAlquilerResponse {

    //atributos a actualizar

    private Long idAlquiler;
    private Long idCliente;
    private Long idAuto;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal precioTotal;
    private String ciudadRetirada;
    private String ciudadDevolucion;
    private String estado;

}
