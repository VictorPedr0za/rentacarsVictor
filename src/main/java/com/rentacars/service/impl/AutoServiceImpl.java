package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateAutoRequest;
import com.rentacars.dto.request.UpdateDetalle_autoRequest;
import com.rentacars.dto.response.*;
import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.mapper.Detalle_autoMapper;
import com.rentacars.model.Auto;
import com.rentacars.model.Detalle_auto;
import com.rentacars.repository.AlquilerRepository;
import com.rentacars.repository.AutoRepository;
import com.rentacars.repository.Detalle_autoRepository;
import com.rentacars.service.AutoService;
import com.rentacars.service.CategoriaService;
import com.rentacars.service.TiendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AutoServiceImpl implements AutoService {
    private final AutoRepository autoRepository;
    private final Detalle_autoRepository detalleAutoRepository;
    private final AlquilerRepository alquilerRepository;
    private final TiendaService tiendaService;
    private final CategoriaService categoriaService;

    @Override
    @Transactional
    public AutoCreateResponse crearAuto(CreateAutoRequest request) {
        // HU-08: en arquitectura monolito la validacion es una llamada directa a services.
        tiendaService.getTiendaById(request.getIdTienda());
        categoriaService.obtenerCategoria(request.getIdCategoria());

        if (detalleAutoRepository.existsByPlacaIgnoreCase(request.getPlaca())) {
            throw new BadRequestException("La placa ya existe");
        }

        Auto auto = Auto.builder()
                .disponibilidad(true)
                .idTienda(request.getIdTienda())
                .idCategoria(request.getIdCategoria())
                .build();
        Auto guardado = autoRepository.save(auto);

        Detalle_auto detalle = Detalle_autoMapper.fromCreateAutoRequest(request, guardado.getIdAuto());
        detalleAutoRepository.save(detalle);

        return AutoCreateResponse.builder()
                .idAuto(guardado.getIdAuto())
                .disponibilidad(guardado.getDisponibilidad())
                .idTienda(guardado.getIdTienda())
                .idCategoria(guardado.getIdCategoria())
                .detalles(AutoCreateResponse.Detalles.builder()
                        .modelo(detalle.getModelo())
                        .precioDia(detalle.getPrecioDia())
                        .ofertaPorcentaje(detalle.getOfertaPorcentaje())
                        .build())
                .build();
    }

    @Override
    public List<AutoSearchResponse> buscarAutos(String ciudad, Long idCategoria) {
        String ciudadNormalizada = (ciudad == null || ciudad.isBlank()) ? null : ciudad.trim();
        return autoRepository.buscarDisponibles(ciudadNormalizada, idCategoria).stream()
                .map(auto -> {
                    Detalle_auto d = detalle(auto.getIdAuto());
                    return AutoSearchResponse.builder()
                            .idAuto(auto.getIdAuto())
                            .modelo(d.getModelo())
                            .marca(d.getMarca())
                            .precioDia(d.getPrecioDia())
                            .ofertaPorcentaje(d.getOfertaPorcentaje())
                            .disponibilidad(auto.getDisponibilidad())
                            .build();
                }).toList();
    }

    @Override
    @Transactional
    public AutoUpdateDetailResponse actualizarDetalles(Long id, UpdateDetalle_autoRequest request) {
        auto(id);
        Detalle_auto d = detalle(id);
        if (request.getPrecioDia() != null) d.setPrecioDia(request.getPrecioDia());
        if (request.getOfertaPorcentaje() != null) d.setOfertaPorcentaje(request.getOfertaPorcentaje());
        if (request.getImagen() != null) d.setImagen(request.getImagen());
        d = detalleAutoRepository.save(d);

        return AutoUpdateDetailResponse.builder()
                .idAuto(id)
                .modelo(d.getModelo())
                .precioDia(d.getPrecioDia())
                .ofertaPorcentaje(d.getOfertaPorcentaje())
                .imagen(d.getImagen())
                .build();
    }

    @Override
    @Transactional
    public AutoAvailabilityResponse actualizarDisponibilidad(Long id, boolean disponibilidad) {
        Auto auto = auto(id);
        auto.setDisponibilidad(disponibilidad);
        autoRepository.save(auto);
        return AutoAvailabilityResponse.builder()
                .idAuto(auto.getIdAuto())
                .disponibilidad(auto.getDisponibilidad())
                .build();
    }

    @Override
    public AutoDetailResponse obtenerDetalle(Long id) {
        Auto auto = auto(id);
        Detalle_auto d = detalle(id);
        BigDecimal oferta = d.getOfertaPorcentaje() == null ? BigDecimal.ZERO : d.getOfertaPorcentaje();
        BigDecimal descuento = d.getPrecioDia().multiply(oferta)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal precioConOferta = d.getPrecioDia().subtract(descuento);

        return AutoDetailResponse.builder()
                .idAuto(auto.getIdAuto())
                .modelo(d.getModelo())
                .marca(d.getMarca())
                .anio(d.getAnio())
                .placa(d.getPlaca())
                .precioDia(d.getPrecioDia())
                .ofertaPorcentaje(d.getOfertaPorcentaje())
                .precioConOferta(precioConOferta)
                .disponibilidad(auto.getDisponibilidad())
                .build();
    }

    @Override
    @Transactional
    public void eliminarAuto(Long id) {
        Auto auto = auto(id);
        if (Boolean.FALSE.equals(auto.getDisponibilidad())) {
            throw new BadRequestException("El auto esta alquilado, no se puede eliminar");
        }
        // La FK de alquileres impide borrar un auto con historial. Se convierte en 400 controlado.
        if (alquilerRepository.existsByIdAuto(id)) {
            throw new BadRequestException("El auto tiene alquileres registrados, no se puede eliminar");
        }
        detalleAutoRepository.deleteByIdAuto(id);
        autoRepository.delete(auto);
    }

    private Auto auto(Long id) {
        return autoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auto no encontrado con id " + id));
    }

    private Detalle_auto detalle(Long idAuto) {
        return detalleAutoRepository.findByIdAuto(idAuto)
                .orElseThrow(() -> new ResourceNotFoundException("Detalle no encontrado para el auto con id " + idAuto));
    }
}
