package com.rentacars.controller;

import com.rentacars.dto.request.UpdateClienteRequest;
import com.rentacars.dto.response.CreateClienteResponse;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ClienteService clienteService;

    // ---------------------------------------------------------------- HU-14

    @Test
    void postClientes_responde201YNoIncluyeLaTarjeta() throws Exception {
        when(clienteService.crearCliente(any())).thenReturn(CreateClienteResponse.builder()
                .idCliente(1L).nombre("Juan Perez").email("juan@email.com").telefono("3001234567").build());

        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content("""
                        { "nombre": "Juan Perez", "email": "juan@email.com",
                          "telefono": "3001234567", "tarjeta_credito": "4111111111111111" }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id_cliente").value(1))
                .andExpect(jsonPath("$.tarjeta_credito").doesNotExist());
    }

    @Test
    void postClientes_conEmailInvalidoResponde400() throws Exception {
        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content("""
                        { "nombre": "Juan", "email": "no-es-un-email",
                          "telefono": "3001234567", "tarjeta_credito": "4111111111111111" }
                        """))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).crearCliente(any());
    }

    // ---------------------------------------------------------------- HU-15

    @Test
    void putClientes_enlazaElBodyEnSnakeCaseYResponde200SinTarjeta() throws Exception {
        when(clienteService.actualizarCliente(eq(1L), any())).thenReturn(CreateClienteResponse.builder()
                .idCliente(1L).nombre("Juan Perez").email("juan@email.com").telefono("3109876543").build());

        mockMvc.perform(put("/clientes/1").contentType(MediaType.APPLICATION_JSON).content("""
                        { "telefono": "3109876543", "tarjeta_credito": "4222222222222222" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefono").value("3109876543"))
                .andExpect(jsonPath("$.tarjeta_credito").doesNotExist());

        ArgumentCaptor<UpdateClienteRequest> captor = ArgumentCaptor.forClass(UpdateClienteRequest.class);
        verify(clienteService).actualizarCliente(eq(1L), captor.capture());
        assertThat(captor.getValue().getTelefono()).isEqualTo("3109876543");
        assertThat(captor.getValue().getTarjetaCredito()).isEqualTo("4222222222222222");
    }

    @Test
    void putClientes_inexistenteResponde404() throws Exception {
        when(clienteService.actualizarCliente(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado con id 99"));

        mockMvc.perform(put("/clientes/99").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"telefono\": \"3109876543\" }"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.estado").value(404))
                .andExpect(jsonPath("$.mensaje").value("Cliente no encontrado con id 99"));
    }

    @Test
    void putClientes_conTelefonoDemasiadoLargoResponde400() throws Exception {
        mockMvc.perform(put("/clientes/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"telefono\": \"123456789012345678901\" }"))
                .andExpect(status().isBadRequest());

        verify(clienteService, never()).actualizarCliente(any(), any());
    }

    // ---------------------------------------------------------------- consulta por id y eliminar

    @Test
    void getClientePorId_respondeSinTarjeta() throws Exception {
        when(clienteService.obtenerCliente(1L)).thenReturn(CreateClienteResponse.builder()
                .idCliente(1L).nombre("Juan Perez").email("juan@email.com").telefono("3001234567").build());

        mockMvc.perform(get("/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_cliente").value(1))
                .andExpect(jsonPath("$.tarjeta_credito").doesNotExist());
    }

    @Test
    void deleteCliente_responde204() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/clientes/1"))
                .andExpect(status().isNoContent());
    }

    // ---------------------------------------------------------------- HU-16

    @Test
    void getClientes_devuelveLaListaConLaTarjetaEnmascarada() throws Exception {
        when(clienteService.listarClientes()).thenReturn(List.of(CreateClienteResponse.builder()
                .idCliente(1L).nombre("Juan Perez").email("juan@email.com").telefono("3001234567")
                .tarjetaCredito("************1111").build()));

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id_cliente").value(1))
                .andExpect(jsonPath("$[0].tarjeta_credito").value("************1111"));
    }

    @Test
    void getClientes_sinClientesResponde200ConListaVacia() throws Exception {
        when(clienteService.listarClientes()).thenReturn(List.of());

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
