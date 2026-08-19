package com.rentacars.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//valid
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateAlquilerRequest {

    //valida id de cliente requerido
    @NotNull(message = "El id del cliente es requerido")
    private Long idCliente;

    //valida id de auto requerido
    @NotNull(message = "El id del auto es requerido")
    private Long idAuto;

    //valida fecha de inicio requerida
    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDate fechaInicio;

    //valida fecha de fin requerida
    @NotNull(message = "La fecha de fin es requerida")
    private LocalDate fechaFin;

    // HU-18 (Pedroza): el precio_total no llega del cliente, lo calcula el servicio

    //valida ciudad de retirada requerida
    @NotBlank(message = "La ciudad de retirada es requerida")
    @Size(max = 50, message = "La ciudad de retirada soporta hasta 50 caracteres")
    private String ciudadRetirada;

    //valida ciudad de devolucion requerida
    @NotBlank(message = "La ciudad de devolucion es requerida")
    @Size(max = 50, message = "La ciudad de devolucion soporta hasta 50 caracteres")
    private String ciudadDevolucion;

    // HU-18 (Pedroza): el estado no llega del cliente, siempre inicia en ACTIVO

}