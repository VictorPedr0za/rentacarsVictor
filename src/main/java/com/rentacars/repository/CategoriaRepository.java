package com.rentacars.repository;

import com.rentacars.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNombre(String nombre);

    // al renombrar: el nombre no puede coincidir con el de OTRA categoria (la misma si puede conservarlo)
    boolean existsByNombreAndIdCategoriaNot(String nombre, Long idCategoria);
}
