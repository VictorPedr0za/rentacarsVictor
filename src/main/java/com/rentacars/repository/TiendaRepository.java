package com.rentacars.repository;

import com.rentacars.model.Tienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TiendaRepository extends JpaRepository<Tienda, Long> {

    // HU-03 (Arango): filtro por ciudad ignorando mayusculas/minusculas -- implementado por Claude
    List<Tienda> findByCiudadIgnoreCase(String ciudad);
}