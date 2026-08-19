package com.rentacars.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//valid
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * HU-18 (Pedroza): Crear alquiler -- implementado por Claude.
 *
 *   POST /alquileres
 *   { "id_cliente": 1, "id_auto": 1, "fecha_inicio": "2026-08-10",
 *     "fecha_fin": "2026-08-13", "ciudad_retirada": "Bogota",
 *     "ciudad_devolucion": "Medellin" }
 *
 * Corregido: la version anterior tambien pedia "precio_total" y "estado"
 * en el body, es decir, el cliente podia inventarse el precio y el estado
 * del alquiler. El backlog es claro: el precio se CALCULA en el servidor
 * (dias * precio_dia * (1 - oferta/100)) y el estado siempre nace en
 * "ACTIVO". Por eso esos dos campos ya no se reciben aqui.
 */
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

    //valida ciudad de retirada requerida
    @NotBlank(message = "La ciudad de retirada es requerida")
    @Size(max = 50, message = "La ciudad de retirada soporta hasta 50 caracteres")
    private String ciudadRetirada;

    //valida ciudad de devolucion requerida
    @NotBlank(message = "La ciudad de devolucion es requerida")
    @Size(max = 50, message = "La ciudad de devolucion soporta hasta 50 caracteres")
    private String ciudadDevolucion;
}
