package com.rentacars.repository;

import com.rentacars.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // HU-14 (Murcia): valida email duplicado antes de registrar -- implementado por Claude
    boolean existsByEmail(String email);
}
