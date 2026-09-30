package com.rentacars.service.impl;

import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.mapper.TiendaMapper;
import com.rentacars.model.Tienda;
import com.rentacars.repository.AutoRepository;
import com.rentacars.repository.TiendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TiendaServiceImplTest {

    @Mock TiendaRepository tiendaRepository;
    @Mock AutoRepository autoRepository;

    TiendaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TiendaServiceImpl(tiendaRepository, new TiendaMapper(), autoRepository);
    }

    @Test
    void eliminarTienda_sinAutosLaBorra() {
        Tienda tienda = new Tienda();
        when(tiendaRepository.findById(1L)).thenReturn(Optional.of(tienda));
        when(autoRepository.existsByIdTienda(1L)).thenReturn(false);

        service.eliminarTienda(1L);

        verify(tiendaRepository).delete(tienda);
    }

    @Test
    void eliminarTienda_conAutosLanza400ConMensajeClaroYNoLaBorra() {
        when(tiendaRepository.findById(1L)).thenReturn(Optional.of(new Tienda()));
        when(autoRepository.existsByIdTienda(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.eliminarTienda(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("autos asociados");

        verify(tiendaRepository, never()).delete(any());
    }

    @Test
    void eliminarTienda_inexistenteLanza404() {
        when(tiendaRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarTienda(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
