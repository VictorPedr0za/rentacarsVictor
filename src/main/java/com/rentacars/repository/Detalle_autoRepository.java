package com.rentacars.repository;

import com.rentacars.model.Detalle_auto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface Detalle_autoRepository extends JpaRepository<Detalle_auto, Long> {

    // HU-12 (Cardona): busca la ficha comercial de un auto a partir de su id_auto
    Optional<Detalle_auto> findByIdAuto(Long idAuto);

    // HU-08: la placa es UNIQUE en la BD; se valida antes para responder 400 y no un error de PostgreSQL
    boolean existsByPlaca(String placa);

    // HU-09 y listado general: trae las fichas de varios autos en una sola consulta (evita N+1)
    List<Detalle_auto> findByIdAutoIn(Collection<Long> idsAuto);
}
