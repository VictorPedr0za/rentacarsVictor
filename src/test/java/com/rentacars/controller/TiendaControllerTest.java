package com.rentacars.controller;

import com.rentacars.service.TiendaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TiendaController.class)
class TiendaControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean TiendaService tiendaService;

    // HU-04: borrar una tienda que todavia tiene autos no debe terminar en un 500
    @Test
    void deleteTienda_conAutosAsociadosResponde409ConFormatoErrorResponse() throws Exception {
        Mockito.doThrow(new DataIntegrityViolationException("fk_autos_tienda"))
                .when(tiendaService).eliminarTienda(1L);

        mockMvc.perform(delete("/tiendas/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.estado").value(409));
    }

    @Test
    void deleteTienda_responde204() throws Exception {
        mockMvc.perform(delete("/tiendas/1")).andExpect(status().isNoContent());
    }
}
