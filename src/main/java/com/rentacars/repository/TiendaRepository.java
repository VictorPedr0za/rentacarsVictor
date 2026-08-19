package com.rentacars.repository;

import com.rentacars.model.Tienda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TiendaRepository extends JpaRepository<Tienda, Long> {
    List<Tienda> findByCiudadIgnoreCase(String ciudad);
}