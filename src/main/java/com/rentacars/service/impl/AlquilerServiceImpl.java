package com.rentacars.service.impl;

import com.rentacars.dto.request.CreateAlquilerRequest;
import com.rentacars.dto.request.UpdateAutoRequest;
import com.rentacars.dto.response.CreateAlquilerResponse;
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
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class AlquilerServiceImpl implements AlquilerService {

    private final AlquilerRepository alquilerRepository;
    private final AutoRepository autoRepository;

    // llama actualizarDisponibilidad al cancelar
    private final AutoService autoService;

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
    @Override
    public CreateAlquilerResponse createAlquiler(CreateAlquilerRequest createAlquilerRequest) throws Exception {

        try {
            if (createAlquilerRequest == null) {
                throw new Exception("El objeto CreateAlquilerRequest no puede ser nulo");
            }

            if (createAlquilerRequest.getIdCliente() == null || createAlquilerRequest.getIdCliente() <= 0) {
                throw new Exception("El idCliente es requerido y debe ser mayor a 0");
            }

            if (createAlquilerRequest.getIdAuto() == null || createAlquilerRequest.getIdAuto() <= 0) {
                throw new Exception("El idAuto es requerido y debe ser mayor a 0");
            }

            if (createAlquilerRequest.getFechaInicio() == null) {
                throw new Exception("La fechaInicio es requerida");
            }

            if (createAlquilerRequest.getFechaFin() == null) {
                throw new Exception("La fechaFin es requerida");
            }

            if (createAlquilerRequest.getFechaFin().isBefore(createAlquilerRequest.getFechaInicio())) {
                throw new Exception("La fechaFin no puede ser anterior a la fechaInicio");
            }

            if (createAlquilerRequest.getPrecioTotal() == null || createAlquilerRequest.getPrecioTotal().signum() < 0) {
                throw new Exception("El precioTotal es requerido y debe ser mayor o igual a 0");
            }

            if (createAlquilerRequest.getCiudadRetirada() == null || createAlquilerRequest.getCiudadRetirada().isBlank()) {
                throw new Exception("La ciudadRetirada es requerida");
            }

            if (createAlquilerRequest.getCiudadDevolucion() == null || createAlquilerRequest.getCiudadDevolucion().isBlank()) {
                throw new Exception("La ciudadDevolucion es requerida");
            }

            Auto auto = autoRepository.findById(createAlquilerRequest.getIdAuto())
                    .orElseThrow(() -> new Exception(
                            "No se ha encontrado el auto con el id " + createAlquilerRequest.getIdAuto()
                    ));

            Alquiler alquiler = Alquiler.builder()
                    .idCliente(createAlquilerRequest.getIdCliente())
                    .idAuto(auto.getIdAuto())
                    .fechaInicio(createAlquilerRequest.getFechaInicio())
                    .fechaFin(createAlquilerRequest.getFechaFin())
                    .precioTotal(createAlquilerRequest.getPrecioTotal())
                    .ciudadRetirada(createAlquilerRequest.getCiudadRetirada())
                    .ciudadDevolucion(createAlquilerRequest.getCiudadDevolucion())
                    .estado("ACTIVO")
                    .build();

            alquiler = alquilerRepository.save(alquiler);

            return AlquilerMapper.entityToCreateAlquilerResponse(alquiler);

        } catch (Exception e) {
            throw e;
        }
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
    //HU-24
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