package com.rentacars.repository;

import com.rentacars.model.Alquiler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AlquilerRepository extends JpaRepository<Alquiler, Long> {

    // valida si el auto tiene alquileres
    boolean existsByIdAuto(Long idAuto);

    // HU-20 (Pedroza): historial de alquileres de un cliente
    List<Alquiler> findByIdCliente(Long idCliente);

    // HU-21 (Pedroza): alquileres activos, no vencidos y no cerrados
    List<Alquiler> findByFechaFinGreaterThanEqualAndEstado(LocalDate fechaFin, String estado);

}