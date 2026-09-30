package com.rentacars.controller;

import com.rentacars.dto.response.CreateAlquilerResponse;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.service.AlquilerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlquilerController.class)
class AlquilerControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AlquilerService alquilerService;

    private static final String BODY_HU18 = """
            { "id_cliente": 1, "id_auto": 1, "fecha_inicio": "2026-08-10",
              "fecha_fin": "2026-08-13", "ciudad_retirada": "Bogota", "ciudad_devolucion": "Medellin" }
            """;

    private CreateAlquilerResponse alquiler() {
        return CreateAlquilerResponse.builder()
                .idAlquiler(1L).idCliente(1L).idAuto(1L)
                .fechaInicio(LocalDate.of(2026, 8, 10)).fechaFin(LocalDate.of(2026, 8, 13))
                .precioTotal(new BigDecimal("405000")).ciudadRetirada("Bogota").ciudadDevolucion("Medellin")
                .estado("ACTIVO").build();
    }

    // ---------------------------------------------------------------- HU-18

    @Test
    void postAlquileres_rutaDelBacklogResponde201() throws Exception {
        when(alquilerService.createAlquiler(any())).thenReturn(alquiler());

        mockMvc.perform(post("/alquileres").contentType(MediaType.APPLICATION_JSON).content(BODY_HU18))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id_alquiler").value(1))
                .andExpect(jsonPath("$.precio_total").value(405000))
                .andExpect(jsonPath("$.fecha_inicio").value("2026-08-10"));
    }

    @Test
    void postAlquileresCreate_rutaAnteriorSigueFuncionandoParaElFrontend() throws Exception {
        when(alquilerService.createAlquiler(any())).thenReturn(alquiler());

        mockMvc.perform(post("/alquileres/create").contentType(MediaType.APPLICATION_JSON).content(BODY_HU18))
                .andExpect(status().isCreated());
    }

    @Test
    void postAlquileres_sinCamposObligatoriosResponde400() throws Exception {
        mockMvc.perform(post("/alquileres").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400));

        verify(alquilerService, never()).createAlquiler(any());
    }

    @Test
    void postAlquileres_conJsonMalFormadoResponde400() throws Exception {
        mockMvc.perform(post("/alquileres").contentType(MediaType.APPLICATION_JSON).content("{ esto no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400));
    }

    @Test
    void postAlquileres_conFechaInvalidaResponde400() throws Exception {
        mockMvc.perform(post("/alquileres").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_HU18.replace("2026-08-10", "manana")))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------- HU-20

    @Test
    void getAlquileres_sinIdClienteResponde400ConFormatoErrorResponse() throws Exception {
        mockMvc.perform(get("/alquileres"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value("El parametro 'id_cliente' es obligatorio"));
    }

    @Test
    void getAlquileres_conIdClienteDevuelveElHistorial() throws Exception {
        when(alquilerService.historialPorCliente(1L)).thenReturn(List.of(alquiler()));

        mockMvc.perform(get("/alquileres").param("id_cliente", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id_alquiler").value(1));
    }

    // ---------------------------------------------------------------- HU-21

    @Test
    void getActivos_noSeConfundeConLaRutaPorId() throws Exception {
        when(alquilerService.listarActivos()).thenReturn(List.of());

        mockMvc.perform(get("/alquileres/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ---------------------------------------------------------------- HU-22 / HU-24

    @Test
    void deleteAlquiler_responde204() throws Exception {
        mockMvc.perform(delete("/alquileres/1")).andExpect(status().isNoContent());
    }

    @Test
    void putDevolucion_responde200() throws Exception {
        when(alquilerService.registrarDevolucion(1L)).thenReturn(alquiler());

        mockMvc.perform(put("/alquileres/1/devolucion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_alquiler").value(1));
    }

    // ---------------------------------------------------------------- consulta por id / update

    @Test
    void getAlquilerPorId_responde200NoUn201() throws Exception {
        when(alquilerService.getAlquilerById(1L)).thenReturn(alquiler());

        mockMvc.perform(get("/alquileres/1")).andExpect(status().isOk());
    }

    @Test
    void getAlquilerPorId_inexistenteResponde404() throws Exception {
        when(alquilerService.getAlquilerById(99L))
                .thenThrow(new ResourceNotFoundException("Alquiler no encontrado con id 99"));

        mockMvc.perform(get("/alquileres/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Alquiler no encontrado con id 99"));
    }

    @Test
    void putUpdate_conEstadoInvalidoResponde400() throws Exception {
        mockMvc.perform(put("/alquileres/update/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"estado\": \"CANCELADO\" }"))
                .andExpect(status().isBadRequest());

        verify(alquilerService, never()).updateAlquiler(any(), any());
    }
}
