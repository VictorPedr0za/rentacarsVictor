package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.dto.request.UpdateDetalle_autoRequest;
import com.rentacars.dto.response.CreateAutoResponse;
import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.model.Auto;
import com.rentacars.model.Detalle_auto;
import com.rentacars.repository.AlquilerRepository;
import com.rentacars.repository.AutoRepository;
import com.rentacars.service.CategoriaService;
import com.rentacars.service.TiendaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoServiceImplTest {

    @Mock AutoRepository autoRepository;
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

    private Auto autoConDetalle(long id, boolean disponible, String marca) {
        Detalle_auto detalle = new Detalle_auto();
        detalle.setMarca(marca);
        detalle.setModelo("Modelo");
        detalle.setPrecioDia(new BigDecimal("120000"));
        detalle.setImagen("https://imgs.com/x.jpg");
        return Auto.builder().idAuto(id).disponibilidad(disponible).idTienda(1L).idCategoria(2L).detalles(detalle).build();
    }

    // ---------------------------------------------------------------- HU-08

    @Test
    void createAuto_guardaUnSoloDocumentoDisponibleConSuDetalleAnidado() {
        when(autoRepository.existsByPlaca("ABC123")).thenReturn(false);
        when(autoRepository.save(any(Auto.class))).thenAnswer(inv -> {
            Auto auto = inv.getArgument(0);
            auto.setIdAuto(7L);
            return auto;
        });

        CreateAutoResponse response = service.createAuto(requestValido());

        ArgumentCaptor<Auto> guardado = ArgumentCaptor.forClass(Auto.class);
        verify(autoRepository).save(guardado.capture());

        // nace disponible y con la ficha comercial DENTRO del mismo documento
        assertThat(guardado.getValue().getDisponibilidad()).isTrue();
        assertThat(guardado.getValue().getIdTienda()).isEqualTo(1L);
        assertThat(guardado.getValue().getIdCategoria()).isEqualTo(2L);
        assertThat(guardado.getValue().getDetalles().getPlaca()).isEqualTo("ABC123");
        assertThat(guardado.getValue().getDetalles().getMarca()).isEqualTo("Hyundai");

        assertThat(response.getIdAuto()).isEqualTo(7L);
        assertThat(response.getDisponibilidad()).isTrue();
        assertThat(response.getDetalles().getModelo()).isEqualTo("Tucson");
        assertThat(response.getDetalles().getPrecioConOferta()).isEqualByComparingTo("135000");
    }

    @Test
    void createAuto_conPlacaRepetidaLanza400YNoGuardaNada() {
        when(autoRepository.existsByPlaca("ABC123")).thenReturn(true);

        assertThatThrownBy(() -> service.createAuto(requestValido()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("placa");

        verify(autoRepository, never()).save(any());
    }

    @Test
    void createAuto_conTiendaInexistenteLanza404YNoGuardaNada() {
        doThrow(new ResourceNotFoundException("Tienda no encontrada con ID: 1"))
                .when(tiendaService).getTiendaById(1L);

        assertThatThrownBy(() -> service.createAuto(requestValido()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(autoRepository, never()).save(any());
    }

    @Test
    void createAuto_conCategoriaInexistenteLanza404YNoGuardaNada() {
        doThrow(new ResourceNotFoundException("No existe la categoria con id 2"))
                .when(categoriaService).obtenerCategoria(2L);

        assertThatThrownBy(() -> service.createAuto(requestValido()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(autoRepository, never()).save(any());
    }

    // ---------------------------------------------------------------- HU-09 / listado

    @Test
    void buscarAutos_devuelveLaFichaQueViajaDentroDelAuto() {
        when(autoRepository.buscarDisponibles("Bogota", 2L)).thenReturn(List.of(autoConDetalle(1L, true, "Toyota")));

        List<CreateAutoResponse> resultado = service.buscarAutos("Bogota", 2L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getMarca()).isEqualTo("Toyota");
        assertThat(resultado.get(0).getImagen()).isEqualTo("https://imgs.com/x.jpg");
        assertThat(resultado.get(0).getIdCategoria()).isEqualTo(2L);
    }

    @Test
    void buscarAutos_conCiudadEnBlancoNoFiltraPorCiudad() {
        when(autoRepository.buscarDisponibles(null, null)).thenReturn(List.of());

        assertThat(service.buscarAutos("   ", null)).isEmpty();
    }

    @Test
    void getAllAutos_unAutoSinFichaNoRompeElListado() {
        Auto sinFicha = Auto.builder().idAuto(2L).disponibilidad(true).idTienda(1L).idCategoria(1L).build();
        when(autoRepository.findAll()).thenReturn(List.of(autoConDetalle(1L, true, "Kia"), sinFicha));

        List<CreateAutoResponse> resultado = service.getAllAutos();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getMarca()).isEqualTo("Kia");
        assertThat(resultado.get(1).getMarca()).isNull();
    }

    // ---------------------------------------------------------------- HU-10 / HU-11 (updates parciales)

    @Test
    void actualizarDetalles_soloCambiaLoQueLlegaYUsaUpdateParcial() {
        Auto auto = autoConDetalle(1L, true, "Mazda");
        when(autoRepository.findById(1L)).thenReturn(Optional.of(auto));
        when(autoRepository.actualizarParcial(any(Auto.class))).thenAnswer(inv -> {
            Auto parcial = inv.getArgument(0);
            // simula lo que devuelve DynamoDB: el item completo ya actualizado
            return Auto.builder().idAuto(1L).disponibilidad(true).detalles(parcial.getDetalles()).build();
        });

        UpdateDetalle_autoRequest request = new UpdateDetalle_autoRequest();
        request.setPrecioDia(new BigDecimal("160000"));

        CreateAutoResponse response = service.actualizarDetalles(1L, request);

        ArgumentCaptor<Auto> parcial = ArgumentCaptor.forClass(Auto.class);
        verify(autoRepository).actualizarParcial(parcial.capture());
        // el update parcial NO lleva disponibilidad: no puede pisar un cambio concurrente
        assertThat(parcial.getValue().getDisponibilidad()).isNull();
        assertThat(parcial.getValue().getDetalles().getPrecioDia()).isEqualByComparingTo("160000");
        assertThat(response.getPrecioDia()).isEqualByComparingTo("160000");
        assertThat(response.getMarca()).isEqualTo("Mazda");
    }

    @Test
    void actualizarDetalles_autoInexistenteLanza404() {
        when(autoRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarDetalles(9L, new UpdateDetalle_autoRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(autoRepository, never()).actualizarParcial(any());
    }

    @Test
    void actualizarDisponibilidad_cambiaSoloElCampoDisponibilidad() {
        when(autoRepository.actualizarParcial(any(Auto.class)))
                .thenReturn(Auto.builder().idAuto(3L).disponibilidad(false).build());

        CreateAutoResponse response = service.actualizarDisponibilidad(3L, new UpdateAutoRequest(false, null, null));

        ArgumentCaptor<Auto> parcial = ArgumentCaptor.forClass(Auto.class);
        verify(autoRepository).actualizarParcial(parcial.capture());
        assertThat(parcial.getValue().getIdAuto()).isEqualTo(3L);
        assertThat(parcial.getValue().getDisponibilidad()).isFalse();
        assertThat(parcial.getValue().getDetalles()).isNull();
        assertThat(response.getDisponibilidad()).isFalse();
    }

    // ---------------------------------------------------------------- HU-13

    @Test
    void deleteAuto_alquiladoLanza400() {
        when(autoRepository.findById(1L)).thenReturn(Optional.of(autoConDetalle(1L, false, "Kia")));

        assertThatThrownBy(() -> service.deleteAuto(1L)).isInstanceOf(BadRequestException.class);

        verify(autoRepository, never()).delete(any());
    }

    @Test
    void deleteAuto_conAlquileresEnElHistorialLanza400() {
        when(autoRepository.findById(1L)).thenReturn(Optional.of(autoConDetalle(1L, true, "Kia")));
        when(alquilerRepository.existsByIdAuto(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteAuto(1L)).isInstanceOf(BadRequestException.class);

        verify(autoRepository, never()).delete(any());
    }

    @Test
    void deleteAuto_libreYSinHistorialLoBorra() {
        Auto auto = autoConDetalle(1L, true, "Kia");
        when(autoRepository.findById(1L)).thenReturn(Optional.of(auto));
        when(alquilerRepository.existsByIdAuto(1L)).thenReturn(false);

        service.deleteAuto(1L);

        verify(autoRepository).delete(auto);
    }
}
