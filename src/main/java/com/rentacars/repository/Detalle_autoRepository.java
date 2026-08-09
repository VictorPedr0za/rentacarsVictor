package com.rentacars.repository;

import com.rentacars.model.Detalle_auto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Detalle_autoRepository extends JpaRepository<Detalle_auto, Long> {

    Optional<Detalle_auto>findByIdAuto(Long idAuti);
}