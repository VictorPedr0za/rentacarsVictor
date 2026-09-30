package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateClienteRequest;
import com.rentacars.dto.request.UpdateClienteRequest;
import com.rentacars.dto.response.CreateClienteResponse;
import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.mapper.ClienteMapper;
import com.rentacars.model.Cliente;
import com.rentacars.repository.AlquilerRepository;
import com.rentacars.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock ClienteRepository clienteRepository;
    @Mock AlquilerRepository alquilerRepository;

    // mapper real: es el que garantiza que la tarjeta no salga completa
    ClienteMapper clienteMapper = new ClienteMapper();

    ClienteServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ClienteServiceImpl(clienteRepository, clienteMapper, alquilerRepository);
    }

    private Cliente juan() {
        return Cliente.builder()
                .idCliente(1L)
                .nombre("Juan Perez")
                .email("juan@email.com")
                .telefono("3001234567")
                .tarjetaCredito("4111111111111111")
                .build();
    }

    // ---------------------------------------------------------------- HU-14

    @Test
    void crearCliente_conEmailRepetidoLanza400() {
        CreateClienteRequest request = new CreateClienteRequest();
        request.setEmail("juan@email.com");
        when(clienteRepository.existsByEmail("juan@email.com")).thenReturn(true);

        assertThatThrownBy(() -> service.crearCliente(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("El email ya existe");

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void crearCliente_noDevuelveLaTarjeta() {
        CreateClienteRequest request = new CreateClienteRequest();
        request.setNombre("Juan Perez");
        request.setEmail("juan@email.com");
        request.setTelefono("3001234567");
        request.setTarjetaCredito("4111111111111111");
        when(clienteRepository.existsByEmail("juan@email.com")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(juan());

        CreateClienteResponse response = service.crearCliente(request);

        assertThat(response.getIdCliente()).isEqualTo(1L);
        assertThat(response.getTarjetaCredito()).isNull();
    }

    // ---------------------------------------------------------------- HU-15

    @Test
    void actualizarCliente_soloCambiaLosCamposQueLlegan_yNuncaElEmail() {
        Cliente cliente = juan();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        UpdateClienteRequest request = new UpdateClienteRequest();
        request.setTelefono("3109876543");   // la tarjeta no llega

        CreateClienteResponse response = service.actualizarCliente(1L, request);

        assertThat(cliente.getTelefono()).isEqualTo("3109876543");
        assertThat(cliente.getTarjetaCredito()).isEqualTo("4111111111111111");
        assertThat(cliente.getEmail()).isEqualTo("juan@email.com");
        assertThat(response.getTelefono()).isEqualTo("3109876543");
        assertThat(response.getTarjetaCredito()).isNull();
    }

    @Test
    void actualizarCliente_puedeCambiarLaTarjeta_peroLaRespuestaNoLaMuestra() {
        Cliente cliente = juan();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        UpdateClienteRequest request = new UpdateClienteRequest();
        request.setTarjetaCredito("4222222222222222");

        CreateClienteResponse response = service.actualizarCliente(1L, request);

        assertThat(cliente.getTarjetaCredito()).isEqualTo("4222222222222222");
        assertThat(response.getTarjetaCredito()).isNull();
    }

    @Test
    void actualizarCliente_inexistenteLanza404() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.actualizarCliente(99L, new UpdateClienteRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(clienteRepository, never()).save(any());
    }

    // ---------------------------------------------------------------- HU-16

    @Test
    void listarClientes_enmascaraLaTarjetaDejandoLosUltimos4Digitos() {
        when(clienteRepository.findAll()).thenReturn(List.of(juan()));

        List<CreateClienteResponse> lista = service.listarClientes();

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).getTarjetaCredito()).isEqualTo("************1111");
        assertThat(lista.get(0).getEmail()).isEqualTo("juan@email.com");
    }

    @Test
    void listarClientes_sinClientesDevuelveListaVacia() {
        when(clienteRepository.findAll()).thenReturn(List.of());

        assertThat(service.listarClientes()).isEmpty();
    }

    @Test
    void enmascararTarjeta_casosBorde() {
        assertThat(clienteMapper.enmascararTarjeta(null)).isNull();
        assertThat(clienteMapper.enmascararTarjeta("  ")).isNull();
        assertThat(clienteMapper.enmascararTarjeta("1234")).isEqualTo("****");
        assertThat(clienteMapper.enmascararTarjeta("12345")).isEqualTo("*2345");
        assertThat(clienteMapper.enmascararTarjeta("4111111111111111")).isEqualTo("************1111");
    }

    // ---------------------------------------------------------------- eliminar

    @Test
    void eliminarCliente_sinAlquileresLoBorra() {
        Cliente cliente = juan();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(alquilerRepository.existsByIdCliente(1L)).thenReturn(false);

        service.eliminarCliente(1L);

        verify(clienteRepository).delete(cliente);
    }

    @Test
    void eliminarCliente_conAlquileresLanza400YNoLoBorra() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(juan()));
        when(alquilerRepository.existsByIdCliente(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.eliminarCliente(1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("alquileres");

        verify(clienteRepository, never()).delete(any());
    }

    @Test
    void eliminarCliente_inexistenteLanza404() {
        when(clienteRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.eliminarCliente(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------------------------------------------------------------- HU-18 (soporte)

    @Test
    void obtenerCliente_inexistenteLanza404() {
        when(clienteRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerCliente(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
