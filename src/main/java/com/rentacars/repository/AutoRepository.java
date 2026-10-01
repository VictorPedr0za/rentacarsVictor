package com.rentacars.repository;

import com.rentacars.model.Auto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutoRepository extends JpaRepository<Auto, Long> {
    @Query(value = """
        SELECT a.*
        FROM autos a
        JOIN tiendas t ON t.id_tienda = a.id_tienda
        WHERE a.disponibilidad = TRUE
          AND (:ciudad IS NULL OR LOWER(t.ciudad) = LOWER(:ciudad))
          AND (:idCategoria IS NULL OR a.id_categoria = :idCategoria)
        ORDER BY a.id_auto
        """, nativeQuery = true)
    List<Auto> buscarDisponibles(@Param("ciudad") String ciudad,
                                 @Param("idCategoria") Long idCategoria);
}
