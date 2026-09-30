package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.response.CreateAutoResponse;
import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.model.Auto;
import com.rentacars.model.Detalle_auto;
import com.rentacars.repository.AlquilerRepository;
import com.rentacars.repository.AutoRepository;
import com.rentacars.repository.Detalle_autoRepository;
import com.rentacars.service.CategoriaService;
import com.rentacars.service.TiendaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoServiceImplTest {

    @Mock AutoRepository autoRepository;
    @Mock Detalle_autoRepository detalleAutoRepository;
    @Mock AlquilerRepository alquilerRepository;
    @Mock TiendaService tiendaService;
    @Mock CategoriaService categoriaService;

    @InjectMocks AutoServiceImpl service;

    private CreateAutoRequest requestValido() {
        CreateAutoRequest request = new CreateAutoRequest();
        request.setModelo("Tucson");
        request.setMarca("Hyundai");
        request.setAnio("2023");
        request.setPlaca("ABC123");
        request.setPrecioDia(new BigDecimal("150000"));
        request.setOfertaPorcentaje(new BigDecimal("10"));
        request.setImagen("https://url.com/img.jpg");
        request.setIdTienda(1L);
        request.setIdCategoria(2L);
        return request;
    }

    // ---------------------------------------------------------------- HU-08

    @Test
    void createAuto_guardaPrimeroElAutoDisponibleYLuegoLaFichaConSuId() {
        when(detalleAutoRepository.existsByPlaca("ABC123")).thenReturn(false);
        when(autoRepository.save(any(Auto.class))).thenAnswer(inv -> {
            Auto auto = inv.getArgument(0);
            auto.setIdAuto(7L);
            return auto;
        });
        when(detalleAutoRepository.save(any(Detalle_auto.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateAutoResponse response = service.createAuto(requestValido());

        // orden exigido por el backlog: primero autos, luego detalles_autos
        InOrder orden = inOrder(autoRepository, detalleAutoRepository);
        ArgumentCaptor<Auto> autoGuardado = ArgumentCaptor.forClass(Auto.class);
        orden.verify(autoRepository).save(autoGuardado.capture());
        ArgumentCaptor<Detalle_auto> detalleGuardado = ArgumentCaptor.forClass(Detalle_auto.class);
        orden.verify(detalleAutoRepository).save(detalleGuardado.capture());

        // el auto nace disponible aunque el cliente no mande nada
        assertThat(autoGuardado.getValue().getDisponibilidad()).isTrue();
        assertThat(autoGuardado.getValue().getIdTienda()).isEqualTo(1L);
        assertThat(autoGuardado.getValue().getIdCategoria()).isEqualTo(2L);
        // la ficha usa el id_auto generado en el primer insert
        assertThat(detalleGuardado.getValue().getIdAuto()).isEqualTo(7L);
        assertThat(detalleGuardado.getValue().getPlaca()).isEqualTo("ABC123");

        assertThat(response.getIdAuto()).isEqualTo(7L);
        assertThat(response.getDisponibilidad()).isTrue();
        assertThat(response.getDetalles().getModelo()).isEqualTo("Tucson");
        assertThat(response.getDetalles().getPrecioDia()).isEqualByComparingTo("150000");
        assertThat(response.getDetalles().getPrecioConOferta()).isEqualByComparingTo("135000");
    }

    @Test
    void createAuto_conPlacaRepetidaLanza400YNoGuardaNada() {
        when(detalleAutoRepository.existsByPlaca("ABC123")).thenReturn(true);

        assertThatThrownBy(() -> service.createAuto(requestValido()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("placa");

        verify(autoRepository, never()).save(any());
        verify(detalleAutoRepository, never()).save(any());
    }

    @Test
    void createAuto_conTiendaInexistenteLanza404YNoGuardaNada() {
        doThrow(new ResourceNotFoundException("Tienda no encontrada con ID: 1"))
                .when(tiendaService).getTiendaById(1L);

        assertThatThrownBy(() -> service.createAuto(requestValido()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(autoRepository, never()).save(any());
        verify(detalleAutoRepository, never()).save(any());
    }

    @Test
    void createAuto_conCategoriaInexistenteLanza404YNoGuardaNada() {
        doThrow(new ResourceNotFoundException("No existe la categoria con id 2"))
                .when(categoriaService).obtenerCategoria(2L);

        assertThatThrownBy(() -> service.createAuto(requestValido()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(autoRepository, never()).save(any());
        verify(detalleAutoRepository, never()).save(any());
    }

    // ---------------------------------------------------------------- HU-09 / listado

    @Test
    void buscarAutos_devuelveLaFichaDeCadaAutoConUnaSolaConsultaDeDetalles() {
        Auto auto = Auto.builder().idAuto(1L).disponibilidad(true).idTienda(1L).idCategoria(2L).build();
        Detalle_auto detalle = new Detalle_auto();
        detalle.setIdAuto(1L);
        detalle.setMarca("Toyota");
        detalle.setModelo("Corolla");
        detalle.setPrecioDia(new BigDecimal("120000"));
        detalle.setImagen("https://imgs.com/corolla.jpg");

        when(autoRepository.buscarDisponibles("Bogota", 2L)).thenReturn(List.of(auto));
        when(detalleAutoRepository.findByIdAutoIn(anyCollection())).thenReturn(List.of(detalle));

        List<CreateAutoResponse> resultado = service.buscarAutos("Bogota", 2L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getMarca()).isEqualTo("Toyota");
        assertThat(resultado.get(0).getImagen()).isEqualTo("https://imgs.com/corolla.jpg");
        assertThat(resultado.get(0).getIdCategoria()).isEqualTo(2L);
    }

    @Test
    void buscarAutos_conCiudadEnBlancoNoFiltraPorCiudad() {
        when(autoRepository.buscarDisponibles(null, null)).thenReturn(List.of());

        assertThat(service.buscarAutos("   ", null)).isEmpty();

        verify(detalleAutoRepository, never()).findByIdAutoIn(anyCollection());
    }

    @Test
    void getAllAutos_unAutoSinFichaNoRompeElListado() {
        Auto conFicha = Auto.builder().idAuto(1L).disponibilidad(true).idTienda(1L).idCategoria(1L).build();
        Auto sinFicha = Auto.builder().idAuto(2L).disponibilidad(true).idTienda(1L).idCategoria(1L).build();
        Detalle_auto detalle = new Detalle_auto();
        detalle.setIdAuto(1L);
        detalle.setMarca("Kia");

        when(autoRepository.findAll()).thenReturn(List.of(conFicha, sinFicha));
        when(detalleAutoRepository.findAll()).thenReturn(List.of(detalle));

        List<CreateAutoResponse> resultado = service.getAllAutos();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getMarca()).isEqualTo("Kia");
        assertThat(resultado.get(1).getMarca()).isNull();
    }
}
