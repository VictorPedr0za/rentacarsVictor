package com.rentacars.controller;

import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;
import com.rentacars.dto.response.CreateDetalle_autoResponse;
import com.rentacars.service.AutoService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
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

@WebMvcTest(AutoController.class)
class AutoControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AutoService autoService;

    private static final String BODY_HU08 = """
            { "modelo": "Tucson", "marca": "Hyundai", "anio": "2023", "placa": "ABC123",
              "precio_dia": 150000, "oferta_porcentaje": 10,
              "imagen": "https://url.com/img.jpg", "id_tienda": 1, "id_categoria": 1 }
            """;

    // ---------------------------------------------------------------- HU-08

    @Test
    void postAutos_conBodyDelBacklogResponde201ConLaFichaAnidada() throws Exception {
        CreateDetalle_autoResponse detalles = CreateDetalle_autoResponse.builder()
                .modelo("Tucson").precioDia(new BigDecimal("150000")).ofertaPorcentaje(new BigDecimal("10")).build();
        when(autoService.createAuto(any())).thenReturn(CreateAutoResponse.builder()
                .idAuto(1L).disponibilidad(true).idTienda(1L).idCategoria(1L).detalles(detalles).build());

        mockMvc.perform(post("/autos").contentType(MediaType.APPLICATION_JSON).content(BODY_HU08))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id_auto").value(1))
                .andExpect(jsonPath("$.disponibilidad").value(true))
                .andExpect(jsonPath("$.id_tienda").value(1))
                .andExpect(jsonPath("$.id_categoria").value(1))
                .andExpect(jsonPath("$.detalles.modelo").value("Tucson"))
                .andExpect(jsonPath("$.detalles.precio_dia").value(150000))
                .andExpect(jsonPath("$.detalles.oferta_porcentaje").value(10));

        // el JSON plano en snake_case se enlaza bien, incluidos los campos heredados de la ficha
        ArgumentCaptor<CreateAutoRequest> captor = ArgumentCaptor.forClass(CreateAutoRequest.class);
        verify(autoService).createAuto(captor.capture());
        CreateAutoRequest recibido = captor.getValue();
        assertThat(recibido.getPlaca()).isEqualTo("ABC123");
        assertThat(recibido.getPrecioDia()).isEqualByComparingTo("150000");
        assertThat(recibido.getIdTienda()).isEqualTo(1L);
        assertThat(recibido.getIdCategoria()).isEqualTo(1L);
    }

    @Test
    void postAutos_sinCamposObligatoriosResponde400ConFormatoErrorResponse() throws Exception {
        mockMvc.perform(post("/autos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("La placa es obligatoria")))
                .andExpect(jsonPath("$.mensaje").value(
                        org.hamcrest.Matchers.containsString("El id de la tienda es requerido")));

        verify(autoService, never()).createAuto(any());
    }

    @Test
    void postAutos_conPrecioNegativoOOfertaFueraDeRangoResponde400() throws Exception {
        String body = BODY_HU08.replace("\"precio_dia\": 150000", "\"precio_dia\": -5")
                .replace("\"oferta_porcentaje\": 10", "\"oferta_porcentaje\": 150");

        mockMvc.perform(post("/autos").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verify(autoService, never()).createAuto(any());
    }

    // ---------------------------------------------------------------- HU-10 / HU-13

    @Test
    void putAutos_conOfertaMayorA100Responde400() throws Exception {
        mockMvc.perform(put("/autos/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"oferta_porcentaje\": 150 }"))
                .andExpect(status().isBadRequest());

        verify(autoService, never()).actualizarDetalles(any(), any());
    }

    @Test
    void deleteAutos_responde204SinCuerpo() throws Exception {
        mockMvc.perform(delete("/autos/1"))
                .andExpect(status().isNoContent());
    }

    // ---------------------------------------------------------------- errores generales

    @Test
    void getAutos_conIdNoNumericoResponde400NoUn500() throws Exception {
        mockMvc.perform(get("/autos/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400));
    }
}
