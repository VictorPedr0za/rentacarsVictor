package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateAlquilerRequest;
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

    // ---------------------------------------------------------------- HU-18

    @Test
    void createAlquiler_calculaPrecioTotalConOfertaPorLosDias_yLoGuardaOcupandoElAutoEnUnaTransaccion() {
        LocalDate inicio = LocalDate.now().plusDays(10);
        when(clienteService.obtenerCliente(1L)).thenReturn(new CreateClienteResponse());
        when(autoService.getAutoById(1L)).thenReturn(autoDisponible());
        when(alquilerRepository.crearOcupandoAuto(any(Alquiler.class))).thenAnswer(inv -> {
            Alquiler a = inv.getArgument(0);
            a.setIdAlquiler(1L);
            return a;
        });

        // 3 dias * 135000 (150000 con 10% de oferta) = 405000, como el ejemplo del backlog
        CreateAlquilerResponse response = service.createAlquiler(request(inicio, inicio.plusDays(3)));

        assertThat(response.getPrecioTotal()).isEqualByComparingTo("405000");
        assertThat(response.getEstado()).isEqualTo("ACTIVO");

        // el alquiler y el cambio de disponibilidad del auto salen en UNA sola operacion del repositorio
        ArgumentCaptor<Alquiler> guardado = ArgumentCaptor.forClass(Alquiler.class);
        verify(alquilerRepository).crearOcupandoAuto(guardado.capture());
        assertThat(guardado.getValue().getIdAuto()).isEqualTo(1L);
        assertThat(guardado.getValue().getEstado()).isEqualTo("ACTIVO");
    }

    @Test
    void createAlquiler_conFechaInicioHoyLanza400() {
        LocalDate hoy = LocalDate.now();

        assertThatThrownBy(() -> service.createAlquiler(request(hoy, hoy.plusDays(2))))
                .isInstanceOf(BadRequestException.class);

        verify(alquilerRepository, never()).crearOcupandoAuto(any());
    }

    @Test
    void createAlquiler_conClienteInexistenteLanza404() {
        LocalDate inicio = LocalDate.now().plusDays(5);
        doThrow(new ResourceNotFoundException("Cliente no encontrado con id 1"))
                .when(clienteService).obtenerCliente(1L);

        assertThatThrownBy(() -> service.createAlquiler(request(inicio, inicio.plusDays(2))))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(alquilerRepository, never()).crearOcupandoAuto(any());
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

        verify(alquilerRepository, never()).crearOcupandoAuto(any());
    }

    @Test
    void createAlquiler_siOtroClienteGanoElAutoEnElInstanteLaTransaccionRechazaConLaCondicion() {
        LocalDate inicio = LocalDate.now().plusDays(5);
        when(clienteService.obtenerCliente(1L)).thenReturn(new CreateClienteResponse());
        when(autoService.getAutoById(1L)).thenReturn(autoDisponible());
        // el repositorio traduce la condicion fallida de DynamoDB a un 400
        when(alquilerRepository.crearOcupandoAuto(any()))
                .thenThrow(new BadRequestException("El auto no esta disponible"));

        assertThatThrownBy(() -> service.createAlquiler(request(inicio, inicio.plusDays(2))))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("El auto no esta disponible");
    }

    // ---------------------------------------------------------------- HU-22

    @Test
    void deleteAlquiler_siYaInicioLanza400ConElMensajeDelBacklog() {
        when(alquilerRepository.findById(10L)).thenReturn(Optional.of(alquiler("ACTIVO", LocalDate.now())));

        assertThatThrownBy(() -> service.deleteAlquiler(10L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("El alquiler ya inició, no se puede cancelar");

        verify(alquilerRepository, never()).cancelarLiberandoAuto(any());
    }

    @Test
    void deleteAlquiler_antesDeIniciarLoCancelaYLiberaElAutoEnUnaTransaccion() {
        Alquiler futuro = alquiler("ACTIVO", LocalDate.now().plusDays(4));
        when(alquilerRepository.findById(10L)).thenReturn(Optional.of(futuro));

        service.deleteAlquiler(10L);

        verify(alquilerRepository).cancelarLiberandoAuto(futuro);
    }

    // ---------------------------------------------------------------- HU-24

    @Test
    void registrarDevolucion_cierraElAlquilerYLiberaElAutoEnUnaTransaccion() {
        Alquiler activo = alquiler("ACTIVO", LocalDate.now().minusDays(2));
        when(alquilerRepository.findById(10L)).thenReturn(Optional.of(activo));
        when(alquilerRepository.cerrarLiberandoAuto(activo)).thenAnswer(inv -> {
            activo.setEstado("CERRADO");
            return activo;
        });

        CreateAlquilerResponse response = service.registrarDevolucion(10L);

        assertThat(response.getEstado()).isEqualTo("CERRADO");
        verify(alquilerRepository).cerrarLiberandoAuto(activo);
    }

    @Test
    void registrarDevolucion_siYaEstabaCerradoLanza400YNoTocaNada() {
        when(alquilerRepository.findById(10L))
                .thenReturn(Optional.of(alquiler("CERRADO", LocalDate.now().minusDays(9))));

        assertThatThrownBy(() -> service.registrarDevolucion(10L))
                .isInstanceOf(BadRequestException.class);

        verify(alquilerRepository, never()).cerrarLiberandoAuto(any());
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
