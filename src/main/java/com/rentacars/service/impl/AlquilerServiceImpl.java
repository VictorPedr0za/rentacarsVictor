package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateAlquilerRequest;
import com.rentacars.dto.response.*;
import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.mapper.AlquilerMapper;
import com.rentacars.model.Alquiler;
import com.rentacars.repository.AlquilerRepository;
import com.rentacars.service.AlquilerService;
import com.rentacars.service.AutoService;
import com.rentacars.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlquilerServiceImpl implements AlquilerService {
    private final AlquilerRepository alquilerRepository;
    private final ClienteService clienteService;
    private final AutoService autoService;

    @Override
    @Transactional
    public CreateAlquilerResponse crearAlquiler(CreateAlquilerRequest request) {
        if (!request.getFechaInicio().isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha de inicio debe ser mayor a hoy");
        }
        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new BadRequestException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        clienteService.obtenerCliente(request.getIdCliente());
        AutoDetailResponse auto = autoService.obtenerDetalle(request.getIdAuto());
        if (Boolean.FALSE.equals(auto.getDisponibilidad())) {
            throw new BadRequestException("El auto no esta disponible");
        }

        long dias = ChronoUnit.DAYS.between(request.getFechaInicio(), request.getFechaFin());
        BigDecimal total = auto.getPrecioConOferta().multiply(BigDecimal.valueOf(dias));

        Alquiler alquiler = Alquiler.builder()
                .idCliente(request.getIdCliente())
                .idAuto(request.getIdAuto())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .precioTotal(total)
                .ciudadRetirada(request.getCiudadRetirada())
                .ciudadDevolucion(request.getCiudadDevolucion())
                .estado("ACTIVO")
                .build();

        Alquiler guardado = alquilerRepository.save(alquiler);
        autoService.actualizarDisponibilidad(guardado.getIdAuto(), false);
        return AlquilerMapper.entityToCreateAlquilerResponse(guardado);
    }

    @Override
    public List<HistorialAlquilerResponse> historialPorCliente(Long idCliente) {
        return alquilerRepository.findByIdCliente(idCliente).stream()
                .map(a -> HistorialAlquilerResponse.builder()
                        .idAlquiler(a.getIdAlquiler())
                        .idAuto(a.getIdAuto())
                        .fechaInicio(a.getFechaInicio())
                        .fechaFin(a.getFechaFin())
                        .precioTotal(a.getPrecioTotal())
                        .ciudadRetirada(a.getCiudadRetirada())
                        .ciudadDevolucion(a.getCiudadDevolucion())
                        .build())
                .toList();
    }

    @Override
    public List<AlquilerActivoResponse> listarActivos() {
        return alquilerRepository.findByFechaFinGreaterThanEqualAndEstado(LocalDate.now(), "ACTIVO").stream()
                .map(a -> AlquilerActivoResponse.builder()
                        .idAlquiler(a.getIdAlquiler())
                        .idCliente(a.getIdCliente())
                        .idAuto(a.getIdAuto())
                        .fechaInicio(a.getFechaInicio())
                        .fechaFin(a.getFechaFin())
                        .ciudadRetirada(a.getCiudadRetirada())
                        .precioTotal(a.getPrecioTotal())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void cancelarAlquiler(Long id) {
        Alquiler alquiler = alquilerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alquiler no encontrado con id " + id));
        if (!alquiler.getFechaInicio().isAfter(LocalDate.now())) {
            throw new BadRequestException("El alquiler ya inició, no se puede cancelar");
        }
        alquilerRepository.delete(alquiler);
        autoService.actualizarDisponibilidad(alquiler.getIdAuto(), true);
    }

    @Override
    @Transactional
    public DevolucionAlquilerResponse registrarDevolucion(Long id) {
        Alquiler alquiler = alquilerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alquiler no encontrado con id " + id));
        alquiler.setEstado("CERRADO");
        Alquiler guardado = alquilerRepository.save(alquiler);
        autoService.actualizarDisponibilidad(guardado.getIdAuto(), true);
        return DevolucionAlquilerResponse.builder()
                .idAlquiler(guardado.getIdAlquiler())
                .estado(guardado.getEstado())
                .fechaInicio(guardado.getFechaInicio())
                .fechaFin(guardado.getFechaFin())
                .precioTotal(guardado.getPrecioTotal())
                .build();
    }
}
