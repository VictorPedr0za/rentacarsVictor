package com.rentacars.service.impl;

import com.rentacars.dto.request.UpdateCategoriaRequest;
import com.rentacars.dto.response.CreateCategoriaResponse;
import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.model.Categoria;
import com.rentacars.repository.AutoRepository;
import com.rentacars.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceImplTest {

    @Mock CategoriaRepository categoriaRepository;
    @Mock AutoRepository autoRepository;

    @InjectMocks CategoriaServiceImpl service;

    private Categoria suv() {
        return Categoria.builder().idCategoria(3L).nombre("SUV").descripcion("Utilitarios").build();
    }

    @Test
    void actualizarCategoria_soloCambiaLosCamposQueLlegan() {
        Categoria categoria = suv();
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        UpdateCategoriaRequest request = new UpdateCategoriaRequest();
        request.setDescripcion("Utilitarios deportivos de mayor capacidad");

        CreateCategoriaResponse response = service.actualizarCategoria(3L, request);

        assertThat(response.getNombre()).isEqualTo("SUV");
        assertThat(response.getDescripcion()).isEqualTo("Utilitarios deportivos de mayor capacidad");
        verify(categoriaRepository, never()).existsByNombreAndIdCategoriaNot(any(), any());
    }

    @Test
    void actualizarCategoria_conNombreDeOtraCategoriaLanza400() {
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(suv()));
        when(categoriaRepository.existsByNombreAndIdCategoriaNot("Sedán", 3L)).thenReturn(true);

        UpdateCategoriaRequest request = new UpdateCategoriaRequest();
        request.setNombre("Sedán");

        assertThatThrownBy(() -> service.actualizarCategoria(3L, request))
                .isInstanceOf(BadRequestException.class);

        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void actualizarCategoria_inexistenteLanza404() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarCategoria(99L, new UpdateCategoriaRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void eliminarCategoria_sinAutosLaBorra() {
        Categoria categoria = suv();
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria));
        when(autoRepository.existsByIdCategoria(3L)).thenReturn(false);

        service.eliminarCategoria(3L);

        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void eliminarCategoria_conAutosLanza400YNoLaBorra() {
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(suv()));
        when(autoRepository.existsByIdCategoria(3L)).thenReturn(true);

        assertThatThrownBy(() -> service.eliminarCategoria(3L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("autos");

        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    void eliminarCategoria_inexistenteLanza404() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarCategoria(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
