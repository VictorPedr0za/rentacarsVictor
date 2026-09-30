package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateAlquilerRequest;
import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.dto.response.CreateAlquilerResponse;
import com.rentacars.dto.response.CreateDetalle_autoResponse;
import com.rentacars.dto.response.UpdateAlquilerResponse;
import com.rentacars.dto.request.UpdateAlquilerRequest;
import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.mapper.AlquilerMapper;
import com.rentacars.model.Alquiler;
import com.rentacars.repository.AlquilerRepository;
import com.rentacars.service.AlquilerService;
import com.rentacars.service.AutoService;
import com.rentacars.service.ClienteService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
// HU-18 (Pedroza): para calcular los dias entre fecha_inicio y fecha_fin
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
public class AlquilerServiceImpl implements AlquilerService {

    private static final String ESTADO_ACTIVO = "ACTIVO";
    private static final String ESTADO_CERRADO = "CERRADO";

    private final AlquilerRepository alquilerRepository;

    // llama actualizarDisponibilidad / obtenerDetalle, antes eran FeignClient
    private final AutoService autoService;

    // HU-18 (Pedroza): valida que el cliente exista con clienteService.obtenerCliente(id)
    private final ClienteService clienteService;

    //obtiene lista alquileres
    @Override
    @Transactional(readOnly = true)
    public List<CreateAlquilerResponse> getAllAlquileres() {

        List<Alquiler> alquileres = alquilerRepository.findAll();
        return AlquilerMapper.entityToListCreateAlquilerResponse(alquileres);

    }

    //obtiene alquiler segun id, 404 si no existe
    @Override
    @Transactional(readOnly = true)
    public CreateAlquilerResponse getAlquilerById(Long id) {

        Alquiler alquiler = buscarAlquiler(id);
        return AlquilerMapper.entityToCreateAlquilerResponse(alquiler);
    }

    //crea alquiler
    // HU-18 (Pedroza): valida cliente y auto, calcula precio y marca el auto ocupado
    @Override
    @Transactional // agrupa guardar el alquiler y actualizar la disponibilidad del auto
    public CreateAlquilerResponse createAlquiler(CreateAlquilerRequest createAlquilerRequest) {

        // valida que la fecha fin no sea anterior a la fecha inicio
        if (createAlquilerRequest.getFechaFin().isBefore(createAlquilerRequest.getFechaInicio())) {
            throw new BadRequestException("La fechaFin no puede ser anterior a la fechaInicio");
        }

        // regla del backlog: la fecha de inicio debe ser posterior a hoy
        if (!createAlquilerRequest.getFechaInicio().isAfter(LocalDate.now())) {
            throw new BadRequestException("La fechaInicio debe ser posterior a hoy");
        }

        // valida que el cliente exista (404), antes esto era un ClienteFeignClient
        clienteService.obtenerCliente(createAlquilerRequest.getIdCliente());

        // valida que el auto exista (404) y trae su precio con oferta, antes un CatalogoFeignClient
        CreateDetalle_autoResponse detalleAuto = autoService.getAutoById(createAlquilerRequest.getIdAuto());

        // el auto debe estar libre para poder alquilarlo
        if (Boolean.FALSE.equals(detalleAuto.getDisponibilidad())) {
            throw new BadRequestException("El auto no esta disponible");
        }

        // calcula cuantos dias dura el alquiler
        long dias = ChronoUnit.DAYS.between(
                createAlquilerRequest.getFechaInicio(), createAlquilerRequest.getFechaFin());

        // el alquiler debe durar al menos un dia completo
        if (dias <= 0) {
            throw new BadRequestException("El alquiler debe durar al menos un dia");
        }

        // precio_total = precio_con_oferta multiplicado por los dias alquilados
        BigDecimal precioTotal = detalleAuto.getPrecioConOferta().multiply(BigDecimal.valueOf(dias));

        // arma la entidad alquiler, siempre inicia en estado ACTIVO
        Alquiler alquiler = Alquiler.builder()
                .idCliente(createAlquilerRequest.getIdCliente())
                .idAuto(createAlquilerRequest.getIdAuto())
                .fechaInicio(createAlquilerRequest.getFechaInicio())
                .fechaFin(createAlquilerRequest.getFechaFin())
                .precioTotal(precioTotal)
                .ciudadRetirada(createAlquilerRequest.getCiudadRetirada())
                .ciudadDevolucion(createAlquilerRequest.getCiudadDevolucion())
                .estado(ESTADO_ACTIVO)
                .build();

        // guarda el alquiler ya con el precio calculado
        alquiler = alquilerRepository.save(alquiler);

        // marca el auto como no disponible, antes esto era un CatalogoFeignClient
        autoService.actualizarDisponibilidad(alquiler.getIdAuto(), new UpdateAutoRequest(false, null, null));

        // convierte la entidad guardada al dto de respuesta
        return AlquilerMapper.entityToCreateAlquilerResponse(alquiler);
    }

    //metodo para actualizar atributos
    @Override
    @Transactional
    public UpdateAlquilerResponse updateAlquiler(Long id, UpdateAlquilerRequest updateAlquilerRequest) {

        //busca alquiler por id, 404 si no existe
        Alquiler alquiler = buscarAlquiler(id);

        //actualiza cliente, que debe existir
        if (updateAlquilerRequest.getIdCliente() != null) {
            clienteService.obtenerCliente(updateAlquilerRequest.getIdCliente());
            alquiler.setIdCliente(updateAlquilerRequest.getIdCliente());
        }

        //actualiza auto del alquiler, que debe existir
        if (updateAlquilerRequest.getIdAuto() != null) {
            autoService.getAutoById(updateAlquilerRequest.getIdAuto());
            alquiler.setIdAuto(updateAlquilerRequest.getIdAuto());
        }

        //actualiza fecha inicio
        if (updateAlquilerRequest.getFechaInicio() != null) {
            alquiler.setFechaInicio(updateAlquilerRequest.getFechaInicio());
        }

        //actualiza fecha fin
        if (updateAlquilerRequest.getFechaFin() != null) {
            alquiler.setFechaFin(updateAlquilerRequest.getFechaFin());
        }

        //la BD exige fecha_fin >= fecha_inicio; se valida para responder 400
        if (alquiler.getFechaFin().isBefore(alquiler.getFechaInicio())) {
            throw new BadRequestException("La fechaFin no puede ser anterior a la fechaInicio");
        }

        //actualiza precio total
        if (updateAlquilerRequest.getPrecioTotal() != null) {
            alquiler.setPrecioTotal(updateAlquilerRequest.getPrecioTotal());
        }

        //actualiza ciudad retirada
        if (updateAlquilerRequest.getCiudadRetirada() != null) {
            alquiler.setCiudadRetirada(updateAlquilerRequest.getCiudadRetirada());
        }

        //actualiza ciudad devolucion
        if (updateAlquilerRequest.getCiudadDevolucion() != null) {
            alquiler.setCiudadDevolucion(updateAlquilerRequest.getCiudadDevolucion());
        }

        //actualiza estado (el DTO ya limita los valores a ACTIVO o CERRADO)
        if (updateAlquilerRequest.getEstado() != null) {
            alquiler.setEstado(updateAlquilerRequest.getEstado());
        }

        //guarda entidad actualizada
        alquiler = alquilerRepository.save(alquiler);

        //convierte a update response
        return AlquilerMapper.entityToUpdateAlquilerResponse(alquiler);
    }

    //HU-24
    @Override
    @Transactional
    public CreateAlquilerResponse registrarDevolucion(Long id) {
        Alquiler alquiler = buscarAlquiler(id);

        // una devolucion ya registrada no se repite: volveria a marcar el auto como libre
        if (ESTADO_CERRADO.equals(alquiler.getEstado())) {
            throw new BadRequestException("El alquiler ya fue cerrado, la devolucion ya esta registrada");
        }

        alquiler.setEstado(ESTADO_CERRADO);
        alquiler = alquilerRepository.save(alquiler);

        autoService.actualizarDisponibilidad(alquiler.getIdAuto(), new UpdateAutoRequest(true, null, null));

        return AlquilerMapper.entityToCreateAlquilerResponse(alquiler);
    }

    //metodo para eliminar alquiler
    // HU-22 (Cardona): cancela y libera el auto
    @Override
    @Transactional // agrupa borrado y liberar auto
    public void deleteAlquiler(Long id) {

        //busca alquiler por id, 404 si no existe
        Alquiler alquiler = buscarAlquiler(id);

        //bloquea cancelar si ya inicio
        if (!alquiler.getFechaInicio().isAfter(LocalDate.now())) {
            throw new BadRequestException("El alquiler ya inició, no se puede cancelar");
        }

        //borra el alquiler cancelado
        alquilerRepository.delete(alquiler);

        //arma datos para liberar auto
        UpdateAutoRequest liberarAuto = new UpdateAutoRequest(true, null, null);

        //libera el auto tras cancelar
        autoService.actualizarDisponibilidad(alquiler.getIdAuto(), liberarAuto);
    }

    // HU-20 (Pedroza): historial de alquileres de un cliente
    @Override
    @Transactional(readOnly = true)
    public List<CreateAlquilerResponse> historialPorCliente(Long idCliente) {

        // busca los alquileres del cliente, lista vacia si no tiene ninguno
        List<Alquiler> alquileres = alquilerRepository.findByIdCliente(idCliente);

        // convierte la lista de entidades al dto de respuesta
        return AlquilerMapper.entityToListCreateAlquilerResponse(alquileres);
    }

    // HU-21 (Pedroza): alquileres activos, aun no vencidos y no cerrados
    @Override
    @Transactional(readOnly = true)
    public List<CreateAlquilerResponse> listarActivos() {

        // filtra por fecha fin mayor o igual a hoy y estado ACTIVO
        List<Alquiler> alquileres =
                alquilerRepository.findByFechaFinGreaterThanEqualAndEstado(LocalDate.now(), ESTADO_ACTIVO);

        // convierte la lista de entidades al dto de respuesta
        return AlquilerMapper.entityToListCreateAlquilerResponse(alquileres);
    }

    // busca un alquiler o lanza 404 (una sola vez, en vez de repetir el orElseThrow)
    private Alquiler buscarAlquiler(Long id) {
        return alquilerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alquiler no encontrado con id " + id));
    }

}
