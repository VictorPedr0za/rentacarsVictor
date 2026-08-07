package com.rentacars.repository;

import com.rentacars.model.Detalle_auto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Detalle_autoRepository extends JpaRepository<Detalle_auto, Long> {
}