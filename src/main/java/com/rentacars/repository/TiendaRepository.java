package com.rentacars.repository;

import com.rentacars.config.DynamoProperties;
import com.rentacars.model.Tienda;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.List;
import java.util.Locale;

@Repository
public class TiendaRepository extends DynamoRepository<Tienda> {

    private final IdGenerator ids;

    public TiendaRepository(DynamoDbClient cliente, DynamoDbEnhancedClient enhanced,
                            DynamoProperties props, IdGenerator ids) {
        super(cliente, enhanced, props, "tiendas", Tienda.class);
        this.ids = ids;
    }

    @Override
    protected Long idDe(Tienda item) {
        return item.getIdTienda();
    }

    @Override
    protected List<EnhancedGlobalSecondaryIndex> indices() {
        // HU-03: buscar tiendas por ciudad
        return List.of(indice("ciudad-index"));
    }

    /** Crea (id nuevo) o reemplaza (id existente) la tienda. */
    public Tienda save(Tienda tienda) {
        if (tienda.getIdTienda() == null) {
            tienda.setIdTienda(ids.siguiente("tiendas"));
        }
        // copia en minusculas para poder buscar sin importar mayusculas (ver model/Tienda)
        tienda.setCiudadNormalizada(normalizar(tienda.getCiudad()));
        tabla.putItem(tienda);
        return tienda;
    }

    /** HU-03: antes un WHERE LOWER(ciudad) = ?; ahora una consulta al indice ciudad-index. */
    public List<Tienda> findByCiudadIgnoreCase(String ciudad) {
        return porIndice("ciudad-index", normalizar(ciudad));
    }

    static String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
    }
}
