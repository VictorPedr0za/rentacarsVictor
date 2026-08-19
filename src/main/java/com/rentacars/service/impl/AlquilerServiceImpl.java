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
// HU-18 (Pedroza): repositorio de clientes, solo para validar que el cliente exista
import com.rentacars.repository.ClienteRepository;
import com.rentacars.service.AlquilerService;
import com.rentacars.service.AutoService;
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

    private final AlquilerRepository alquilerRepository;
    private final AutoRepository autoRepository;

    // llama actualizarDisponibilidad al cancelar
    private final AutoService autoService;

    // HU-18 (Pedroza): solo para validar que el id del cliente exista
    private final ClienteRepository clienteRepository;

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

    //crea alquiler
    // HU-18 (Pedroza): valida cliente y auto, calcula precio y marca el auto ocupado
    @Override
    @Transactional // agrupa guardar el alquiler y actualizar la disponibilidad del auto
    public CreateAlquilerResponse createAlquiler(CreateAlquilerRequest createAlquilerRequest) throws Exception {

        // valida que la fecha fin no sea anterior a la fecha inicio
        if (createAlquilerRequest.getFechaFin().isBefore(createAlquilerRequest.getFechaInicio())) {
            throw new BadRequestException("La fechaFin no puede ser anterior a la fechaInicio");
        }

        // regla del backlog: la fecha de inicio debe ser posterior a hoy
        if (!createAlquilerRequest.getFechaInicio().isAfter(LocalDate.now())) {
            throw new BadRequestException("La fechaInicio debe ser posterior a hoy");
        }

        // valida que el cliente exista, antes esto era un ClienteFeignClient
        if (!clienteRepository.existsById(createAlquilerRequest.getIdCliente())) {
            throw new ResourceNotFoundException(
                    "Cliente no encontrado con id " + createAlquilerRequest.getIdCliente());
        }

        // valida que el auto exista y trae su precio con oferta, antes un CatalogoFeignClient
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
                .estado("ACTIVO")
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

    // HU-20 (Pedroza): historial de alquileres de un cliente
    @Override
    public List<CreateAlquilerResponse> historialPorCliente(Long idCliente) {

        // busca los alquileres del cliente, lista vacia si no tiene ninguno
        List<Alquiler> alquileres = alquilerRepository.findByIdCliente(idCliente);

        // convierte la lista de entidades al dto de respuesta
        return AlquilerMapper.entityToListCreateAlquilerResponse(alquileres);
    }

    // HU-21 (Pedroza): alquileres activos, aun no vencidos y no cerrados
    @Override
    public List<CreateAlquilerResponse> listarActivos() {

        // filtra por fecha fin mayor o igual a hoy y estado ACTIVO
        List<Alquiler> alquileres =
                alquilerRepository.findByFechaFinGreaterThanEqualAndEstado(LocalDate.now(), "ACTIVO");

        // convierte la lista de entidades al dto de respuesta
        return AlquilerMapper.entityToListCreateAlquilerResponse(alquileres);
    }

}