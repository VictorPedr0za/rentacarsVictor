package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateAlquilerRequest;
import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.dto.response.CreateAlquilerResponse;
import com.rentacars.dto.response.CreateClienteResponse;
import com.rentacars.dto.response.CreateDetalle_autoResponse;
import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.model.Alquiler;
import com.rentacars.repository.AlquilerRepository;
import com.rentacars.service.AutoService;
import com.rentacars.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlquilerServiceImplTest {

    @Mock AlquilerRepository alquilerRepository;
    @Mock AutoService autoService;
    @Mock ClienteService clienteService;

    AlquilerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AlquilerServiceImpl(alquilerRepository, autoService, clienteService);
    }

    private CreateAlquilerRequest request(LocalDate inicio, LocalDate fin) {
        return new CreateAlquilerRequest(1L, 1L, inicio, fin, "Bogota", "Medellin");
    }

    private CreateDetalle_autoResponse autoDisponible() {
        return CreateDetalle_autoResponse.builder()
                .idAuto(1L)
                .disponibilidad(true)
                .precioDia(new BigDecimal("150000"))
                .precioConOferta(new BigDecimal("135000.00"))
                .build();
    }

    private Alquiler alquiler(String estado, LocalDate inicio) {
        return Alquiler.builder()
                .idAlquiler(10L).idCliente(1L).idAuto(3L)
                .fechaInicio(inicio).fechaFin(inicio.plusDays(3))
                .precioTotal(new BigDecimal("405000"))
                .ciudadRetirada("Bogota").ciudadDevolucion("Medellin")
                .estado(estado)
                .build();
    }

    private boolean disponibilidadEnviada() {
        ArgumentCaptor<UpdateAutoRequest> captor = ArgumentCaptor.forClass(UpdateAutoRequest.class);
        verify(autoService).actualizarDisponibilidad(eq(3L), captor.capture());
        return captor.getValue().getDisponibilidad();
    }

    // ---------------------------------------------------------------- HU-18

    @Test
    void createAlquiler_calculaPrecioTotalConOfertaPorLosDias_yOcupaElAuto() {
        LocalDate inicio = LocalDate.now().plusDays(10);
        when(clienteService.obtenerCliente(1L)).thenReturn(new CreateClienteResponse());
        when(autoService.getAutoById(1L)).thenReturn(autoDisponible());
        when(alquilerRepository.save(any(Alquiler.class))).thenAnswer(inv -> {
            Alquiler a = inv.getArgument(0);
            a.setIdAlquiler(1L);
            return a;
        });

        // 3 dias * 135000 (150000 con 10% de oferta) = 405000, como el ejemplo del backlog
        CreateAlquilerResponse response = service.createAlquiler(request(inicio, inicio.plusDays(3)));

        assertThat(response.getPrecioTotal()).isEqualByComparingTo("405000");
        assertThat(response.getEstado()).isEqualTo("ACTIVO");

        ArgumentCaptor<UpdateAutoRequest> captor = ArgumentCaptor.forClass(UpdateAutoRequest.class);
        verify(autoService).actualizarDisponibilidad(eq(1L), captor.capture());
        assertThat(captor.getValue().getDisponibilidad()).isFalse();
    }

    @Test
    void createAlquiler_conFechaInicioHoyLanza400() {
        LocalDate hoy = LocalDate.now();

        assertThatThrownBy(() -> service.createAlquiler(request(hoy, hoy.plusDays(2))))
                .isInstanceOf(BadRequestException.class);

        verify(alquilerRepository, never()).save(any());
    }

    @Test
    void createAlquiler_conClienteInexistenteLanza404() {
        LocalDate inicio = LocalDate.now().plusDays(5);
        doThrow(new ResourceNotFoundException("Cliente no encontrado con id 1"))
                .when(clienteService).obtenerCliente(1L);

        assertThatThrownBy(() -> service.createAlquiler(request(inicio, inicio.plusDays(2))))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(alquilerRepository, never()).save(any());
    }

    @Test
    void createAlquiler_conAutoNoDisponibleLanza400() {
        LocalDate inicio = LocalDate.now().plusDays(5);
        CreateDetalle_autoResponse ocupado = autoDisponible();
        ocupado.setDisponibilidad(false);
        when(clienteService.obtenerCliente(1L)).thenReturn(new CreateClienteResponse());
        when(autoService.getAutoById(1L)).thenReturn(ocupado);

        assertThatThrownBy(() -> service.createAlquiler(request(inicio, inicio.plusDays(2))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("disponible");

        verify(alquilerRepository, never()).save(any());
        verify(autoService, never()).actualizarDisponibilidad(any(), any());
    }

    // ---------------------------------------------------------------- HU-22

    @Test
    void deleteAlquiler_siYaInicioLanza400ConElMensajeDelBacklog() {
        when(alquilerRepository.findById(10L)).thenReturn(Optional.of(alquiler("ACTIVO", LocalDate.now())));

        assertThatThrownBy(() -> service.deleteAlquiler(10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("El alquiler ya inició, no se puede cancelar");

        verify(alquilerRepository, never()).delete(any());
    }

    @Test
    void deleteAlquiler_antesDeIniciarLoBorraYLiberaElAuto() {
        Alquiler futuro = alquiler("ACTIVO", LocalDate.now().plusDays(4));
        when(alquilerRepository.findById(10L)).thenReturn(Optional.of(futuro));

        service.deleteAlquiler(10L);

        verify(alquilerRepository).delete(futuro);
        assertThat(disponibilidadEnviada()).isTrue();
    }

    // ---------------------------------------------------------------- HU-24

    @Test
    void registrarDevolucion_cierraElAlquilerYLiberaElAuto() {
        Alquiler activo = alquiler("ACTIVO", LocalDate.now().minusDays(2));
        when(alquilerRepository.findById(10L)).thenReturn(Optional.of(activo));
        when(alquilerRepository.save(activo)).thenReturn(activo);

        CreateAlquilerResponse response = service.registrarDevolucion(10L);

        assertThat(response.getEstado()).isEqualTo("CERRADO");
        assertThat(disponibilidadEnviada()).isTrue();
    }

    @Test
    void registrarDevolucion_siYaEstabaCerradoLanza400YNoTocaElAuto() {
        when(alquilerRepository.findById(10L))
                .thenReturn(Optional.of(alquiler("CERRADO", LocalDate.now().minusDays(9))));

        assertThatThrownBy(() -> service.registrarDevolucion(10L))
                .isInstanceOf(BadRequestException.class);

        verify(alquilerRepository, never()).save(any());
        verify(autoService, never()).actualizarDisponibilidad(any(), any());
    }

    @Test
    void registrarDevolucion_inexistenteLanza404() {
        when(alquilerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarDevolucion(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------------------------------------------------------------- consulta por id

    @Test
    void getAlquilerById_inexistenteLanza404_noUnaRuntimeExceptionGenerica() {
        when(alquilerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAlquilerById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
