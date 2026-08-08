package com.rentacars.repository;

import com.rentacars.model.Auto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutoRepository extends JpaRepository<Auto, Long> {
    // HU-09 (Suarez): busca autos disponibles, con filtros opcionales de ciudad y categoria
    @Query("""
        SELECT a FROM Auto a
        JOIN Tienda t ON a.idTienda = t.idTienda
        WHERE a.disponibilidad = true
          AND (:ciudad IS NULL OR LOWER(t.ciudad) = LOWER(CAST(:ciudad AS string)))
          AND (:idCategoria IS NULL OR a.idCategoria = :idCategoria)
        """)
    List<Auto> buscarDisponibles(@Param("ciudad") String ciudad, @Param("idCategoria") Long idCategoria);

}