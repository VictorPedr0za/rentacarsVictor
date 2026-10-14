package com.rentacars.controller;

import com.rentacars.service.TiendaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TiendaController.class)
class TiendaControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean TiendaService tiendaService;

    // un conflicto de escritura de DynamoDB (condicion fallida) no debe terminar en un 500
    @Test
    void deleteTienda_conConflictoDeEscrituraEnDynamoDbResponde409ConFormatoErrorResponse() throws Exception {
        Mockito.doThrow(ConditionalCheckFailedException.builder().message("conflicto de escritura").build())
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
