package com.rentacars.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//valids
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAlquilerRequest {

    //atributos que se van a actualizar

    //valida id de cliente positivo
    @Positive(message = "El idCliente debe ser mayor a 0")
    private Long idCliente;

    //valida id de auto positivo
    @Positive(message = "El idAuto debe ser mayor a 0")
    private Long idAuto;

    //fechas sin validacion adicional
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    //valida precio total positivo
    @Positive(message = "El precioTotal debe ser mayor a 0")
    private BigDecimal precioTotal;

    //valida ciudad de retirada tamaño
    @Size(max = 50, message = "La ciudadRetirada soporta hasta 50 caracteres")
    private String ciudadRetirada;

    //valida ciudad de devolucion tamaño
    @Size(max = 50, message = "La ciudadDevolucion soporta hasta 50 caracteres")
    private String ciudadDevolucion;

    //valida estado tamaño
    @Size(max = 20, message = "El estado soporta hasta 20 caracteres")
    private String estado;

}