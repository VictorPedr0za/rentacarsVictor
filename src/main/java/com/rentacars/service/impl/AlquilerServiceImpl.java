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
import com.rentacars.model.Auto;
import com.rentacars.repository.AlquilerRepository;
import com.rentacars.repository.AutoRepository;
import com.rentacars.service.AlquilerService;
import com.rentacars.service.AutoService;
import com.rentacars.service.ClienteService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
public class AlquilerServiceImpl implements AlquilerService {

    private final AlquilerRepository alquilerRepository;
    private final AutoRepository autoRepository;

    // llama actualizarDisponibilidad al cancelar, y al crear/devolver un alquiler
    private final AutoService autoService;

    // HU-18 (Pedroza): valida que el cliente exista -- implementado por Claude
    private final ClienteService clienteService;

    //obtiene lista alquileres
    @Override
    public List<CreateAlquilerResponse> getAllAlquileres() {

        List<Alquiler> alquileres = alquilerRepository.findAll();
        List<CreateAlquilerResponse> createAlquilerResponseList = AlquilerMapper.entityToListCreateAlquilerResponse(alquileres);
        return createAlquilerResponseList;

    }

    //obtiene alquiler segun id
    @Override
    public CreateAlquilerResponse getAlquilerById(Long id) {

        Alquiler alquiler = alquilerRepository.findById(id).orElseThrow(() -> new RuntimeException("El ID:  " + id + " .No es valido"));
        CreateAlquilerResponse createAlquilerResponse = AlquilerMapper.entityToCreateAlquilerResponse(alquiler);
        return createAlquilerResponse;
    }

    // HU-18 (Pedroza): crear alquiler -- implementado por Claude.
    //
    // Corregido: la version anterior no validaba que el cliente existiera ni que el auto
    // estuviera disponible, dejaba que el cliente inventara precio_total y estado, y nunca
    // marcaba el auto como no disponible. Ahora, en una sola transaccion:
    //   1. Valida que el cliente exista (404) -- clienteService.obtenerCliente(id).
    //   2. Obtiene el detalle del auto (404 si no existe) y valida que este disponible (400).
    //   3. Valida que fecha_inicio sea posterior a hoy (400).
    //   4. Calcula precio_total = dias * precio_dia * (1 - oferta/100).
    //   5. Guarda el alquiler y marca el auto como no disponible.
    @Override
    @Transactional
    public CreateAlquilerResponse createAlquiler(CreateAlquilerRequest createAlquilerRequest) throws Exception {

        clienteService.obtenerCliente(createAlquilerRequest.getIdCliente());

        CreateDetalle_autoResponse auto = autoService.getAutoById(createAlquilerRequest.getIdAuto());

        if (!Boolean.TRUE.equals(auto.getDisponibilidad())) {
            throw new BadRequestException("El auto no esta disponible");
        }

        if (!createAlquilerRequest.getFechaInicio().isAfter(LocalDate.now())) {
            throw new BadRequestException("La fecha de inicio debe ser posterior a hoy");
        }

        if (createAlquilerRequest.getFechaFin().isBefore(createAlquilerRequest.getFechaInicio())) {
            throw new BadRequestException("La fechaFin no puede ser anterior a la fechaInicio");
        }

        long dias = ChronoUnit.DAYS.between(createAlquilerRequest.getFechaInicio(), createAlquilerRequest.getFechaFin());

        BigDecimal oferta = auto.getOfertaPorcentaje();
        if (oferta == null) {
            oferta = BigDecimal.ZERO;
        }
        BigDecimal descuento = auto.getPrecioDia()
                .multiply(oferta)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal precioConOferta = auto.getPrecioDia().subtract(descuento);
        BigDecimal precioTotal = precioConOferta.multiply(BigDecimal.valueOf(dias));

        Alquiler alquiler = AlquilerMapper.createAlquilerRequestToEntity(createAlquilerRequest, precioTotal);
        alquiler = alquilerRepository.save(alquiler);

        // marca el auto como no disponible, ahora que quedo reservado
        autoService.actualizarDisponibilidad(createAlquilerRequest.getIdAuto(), new UpdateAutoRequest(false, null, null));

        return AlquilerMapper.entityToCreateAlquilerResponse(alquiler);
    }

    // HU-20 (Pedroza): historial de alquileres de un cliente -- implementado por Claude
    @Override
    public List<CreateAlquilerResponse> historialPorCliente(Long idCliente) {
        List<Alquiler> alquileres = alquilerRepository.findByIdCliente(idCliente);
        return AlquilerMapper.entityToListCreateAlquilerResponse(alquileres);
    }

    // HU-21 (Pedroza): alquileres con estado ACTIVO -- implementado por Claude
    @Override
    public List<CreateAlquilerResponse> listarActivos() {
        List<Alquiler> activos = alquilerRepository.findByEstado("ACTIVO");
        return AlquilerMapper.entityToListCreateAlquilerResponse(activos);
    }

    // HU-24 (Corrales): registrar devolucion de auto -- implementado por Claude.
    // Cierra el alquiler (estado = CERRADO) y libera el auto inyectando AutoService
    // (Cambio v2: antes hubiera sido CatalogoFeignClient).
    @Override
    @Transactional
    public CreateAlquilerResponse registrarDevolucion(Long id) {
        Alquiler alquiler = alquilerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alquiler no encontrado con id " + id));

        alquiler.setEstado("CERRADO");
        alquiler = alquilerRepository.save(alquiler);

        autoService.actualizarDisponibilidad(alquiler.getIdAuto(), new UpdateAutoRequest(true, null, null));

        return AlquilerMapper.entityToCreateAlquilerResponse(alquiler);
    }

    //metodo para actualizar atributos
    @Override
    public UpdateAlquilerResponse updateAlquiler(Long id, UpdateAlquilerRequest updateAlquilerRequest) throws Exception {

        try {


            // Validar id no nulo
            if (id == null){
                throw new Exception("El objeto Alquiler debe existir");
            }


            //valida request no nulo
            if (updateAlquilerRequest == null){
                throw new Exception("El objeto UpdateAlquilerRequest no puede ser nulo");
            }

            //busca alquiler por id
            Alquiler alquiler = alquilerRepository.findById(id).orElseThrow(() -> new RuntimeException("Alquiler not found with id; " + id));

            //actualiza cliente
            if (updateAlquilerRequest.getIdCliente() != null) {
                alquiler.setIdCliente(updateAlquilerRequest.getIdCliente());
            }

            //actualiza auto del alquiler
            if (updateAlquilerRequest.getIdAuto() != null) {

                //busca auto por id
                Auto auto = autoRepository.findById(updateAlquilerRequest.getIdAuto())
                        .orElseThrow(() -> new Exception(
                                "No se encontro el auto con id " + updateAlquilerRequest.getIdAuto()
                        ));

                //asigna auto encontrado
                alquiler.setIdAuto(auto.getIdAuto());
            }

            //actualiza fecha inicio
            if (updateAlquilerRequest.getFechaInicio() != null) {
                alquiler.setFechaInicio(updateAlquilerRequest.getFechaInicio());
            }

            //actualiza fecha fin
            if (updateAlquilerRequest.getFechaFin() != null) {
                alquiler.setFechaFin(updateAlquilerRequest.getFechaFin());
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

            //actualiza estado
            if (updateAlquilerRequest.getEstado() != null) {
                alquiler.setEstado(updateAlquilerRequest.getEstado());
            }

            //guarda entidad actualizada
            alquiler = alquilerRepository.save(alquiler);

            //convierte a update response
            UpdateAlquilerResponse response = AlquilerMapper.entityToUpdateAlquilerResponse(alquiler);

            //retorna dto
            return response;

        } catch (Exception e) {
            throw e;
        }
    }

    /*
    //metodo para eliminar alquiler
    @Override
    public void deleteAlquiler(Long id) throws Exception {

        try {

            //valida id no nulo
            if (id == null){
                throw new Exception("El id del alquiler es requerido");
            }

            //busca alquiler por id
            Alquiler alquiler = alquilerRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("El ID:  " + id + " .No es valido"));

            //elimina alquiler
            alquilerRepository.delete(alquiler);

        } catch (Exception e) {
            throw e;
        }
    }
    */

    //metodo para eliminar alquiler
    // HU-22 (Cardona): cancela y libera el auto
    @Override
    @Transactional // agrupa borrado y liberar auto
    public void deleteAlquiler(Long id) {

        //busca alquiler por id, 404 si no existe
        Alquiler alquiler = alquilerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alquiler no encontrado con id " + id));

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

}