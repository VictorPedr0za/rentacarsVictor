package com.rentacars.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HU-14/HU-15/HU-16 (Murcia) -- implementado por Claude.
 *
 * Un solo DTO de respuesta para las tres HU del dominio Clientes, igual
 * que TiendaController reusa CreateTiendaResponse para su GET por id.
 *
 * OJO con tarjetaCredito: en HU-14 (crear) y HU-15 (actualizar) el mapper
 * NUNCA la asigna, asi que queda en null y Jackson la omite del JSON
 * (ver spring.jackson.default-property-inclusion=non_null). Solo
 * HU-16 (listar) la asigna, ya enmascarada (ClienteMapper.enmascararTarjeta).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateClienteResponse {

    private Long idCliente;
    private String nombre;
    private String email;
    private String telefono;

    // Solo se llena en HU-16 (listarClientes), y siempre enmascarada.
    private String tarjetaCredito;
}
