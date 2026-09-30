package com.rentacars.controller;

import com.rentacars.dto.response.CreateCategoriaResponse;
import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.service.CategoriaService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean CategoriaService categoriaService;

    @Test
    void getPorId_responde200ConSnakeCase() throws Exception {
        when(categoriaService.obtenerCategoria(3L)).thenReturn(
                CreateCategoriaResponse.builder().idCategoria(3L).nombre("SUV").descripcion("Utilitarios").build());

        mockMvc.perform(get("/categorias/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_categoria").value(3))
                .andExpect(jsonPath("$.nombre").value("SUV"));
    }

    @Test
    void getPorId_inexistenteResponde404() throws Exception {
        when(categoriaService.obtenerCategoria(99L))
                .thenThrow(new ResourceNotFoundException("No existe la categoria con id 99"));

        mockMvc.perform(get("/categorias/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la categoria con id 99"));
    }

    @Test
    void put_actualizaYResponde200() throws Exception {
        when(categoriaService.actualizarCategoria(eq(3L), any())).thenReturn(
                CreateCategoriaResponse.builder().idCategoria(3L).nombre("SUV Plus").descripcion("Utilitarios").build());

        mockMvc.perform(put("/categorias/3").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"nombre\": \"SUV Plus\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("SUV Plus"));
    }

    @Test
    void put_conNombreEnBlancoResponde400() throws Exception {
        mockMvc.perform(put("/categorias/3").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"nombre\": \"\" }"))
                .andExpect(status().isBadRequest());

        verify(categoriaService, never()).actualizarCategoria(any(), any());
    }

    @Test
    void delete_responde204() throws Exception {
        mockMvc.perform(delete("/categorias/3")).andExpect(status().isNoContent());
    }

    @Test
    void delete_conAutosAsociadosResponde400() throws Exception {
        Mockito.doThrow(new BadRequestException("La categoria tiene autos asociados, no se puede eliminar"))
                .when(categoriaService).eliminarCategoria(3L);

        mockMvc.perform(delete("/categorias/3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La categoria tiene autos asociados, no se puede eliminar"));
    }
}
