package com.rentacars.repository;

import com.rentacars.model.Cliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByEmail(@NotBlank(message = "El email es obligatorio") @Email(message = "El email no tiene un formato válido") String email);
}
