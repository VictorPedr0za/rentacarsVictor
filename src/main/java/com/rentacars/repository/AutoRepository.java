package com.rentacars.repository;

import com.rentacars.config.DynamoProperties;
import com.rentacars.exception.BadRequestException;
import com.rentacars.exception.ResourceNotFoundException;
import com.rentacars.model.Auto;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.enhanced.dynamodb.model.IgnoreNullsMode;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AutoRepository extends DynamoRepository<Auto> {

    private static final String TIPO_PLACA = "auto.placa";

    private final IdGenerator ids;
    private final UnicoRepository unicos;
    private final TiendaRepository tiendas;

    public AutoRepository(DynamoDbClient cliente, DynamoDbEnhancedClient enhanced, DynamoProperties props,
                          IdGenerator ids, UnicoRepository unicos, TiendaRepository tiendas) {
        super(cliente, enhanced, props, "autos", Auto.class);
        this.ids = ids;
        this.unicos = unicos;
        this.tiendas = tiendas;
    }

    @Override
    protected Long idDe(Auto item) {
        return item.getIdAuto();
    }

    @Override
    protected List<EnhancedGlobalSecondaryIndex> indices() {
        return List.of(indice("idTienda-index"), indice("idCategoria-index"));
    }

    // ------------------------------------------------------------------ escritura

    /**
     * HU-08. Auto nuevo: reserva la placa y guarda el auto (con su detalle anidado) en UNA
     * transaccion. Antes eran dos INSERT en dos tablas bajo @Transactional; ahora el detalle
     * viaja dentro del mismo item, y la unica condicion extra es la placa unica.
     */
    public Auto save(Auto auto) {
        if (auto.getIdAuto() != null) {
            tabla.putItem(auto);
            return auto;
        }
        auto.setIdAuto(ids.siguiente("autos"));
        try {
            enhanced.transactWriteItems(TransactWriteItemsEnhancedRequest.builder()
                    .addPutItem(unicos.tabla(),
                            unicos.pedir(UnicoRepository.clave(TIPO_PLACA, auto.getDetalles().getPlaca()), auto.getIdAuto()))
                    .addPutItem(tabla, auto)
                    .build());
        } catch (TransactionCanceledException e) {
            if (fallaCondicion(e, 0)) {
                throw new BadRequestException("La placa ya esta registrada");
            }
            throw e;
        }
        return auto;
    }

    /** HU-13. Borra el auto y libera su placa en una transaccion. */
    @Override
    public void delete(Auto auto) {
        enhanced.transactWriteItems(TransactWriteItemsEnhancedRequest.builder()
                .addDeleteItem(tabla, llave(auto.getIdAuto()))
                .addDeleteItem(unicos.tabla(),
                        unicos.soltar(UnicoRepository.clave(TIPO_PLACA, auto.getDetalles().getPlaca())))
                .build());
    }

    /**
     * Actualiza SOLO los atributos que vienen con valor (HU-10, HU-11 y el PUT /autos/update).
     * Es el UPDATE ... SET col = ? de SQL: no pisa lo demas (por ejemplo, no deshace un cambio
     * de disponibilidad hecho por otro alquiler justo en ese momento). La condicion evita que
     * UpdateItem CREE un item fantasma si el auto no existe.
     */
    public Auto actualizarParcial(Auto parcial) {
        try {
            return tabla.updateItem(UpdateItemEnhancedRequest.builder(Auto.class)
                    .item(parcial)
                    .ignoreNullsMode(IgnoreNullsMode.SCALAR_ONLY)
                    .conditionExpression(Expression.builder().expression("attribute_exists(idAuto)").build())
                    .build());
        } catch (ConditionalCheckFailedException e) {
            throw new ResourceNotFoundException("Auto no encontrado con id " + parcial.getIdAuto());
        }
    }

    // ------------------------------------------------------------------ consultas

    public boolean existsByPlaca(String placa) {
        return unicos.existe(UnicoRepository.clave(TIPO_PLACA, placa));
    }

    /** HU-04: "esta tienda tiene autos?" -> consulta al indice idTienda-index. */
    public boolean existsByIdTienda(Long idTienda) {
        return existePorIndice("idTienda-index", idTienda);
    }

    public boolean existsByIdCategoria(Long idCategoria) {
        return existePorIndice("idCategoria-index", idCategoria);
    }

    /**
     * HU-09. En SQL era UN query con JOIN a tiendas. En DynamoDB no hay JOIN, asi que se hace en
     * pasos, empezando por el filtro mas selectivo:
     *   - con ciudad:      tiendas de esa ciudad (indice ciudad-index) -> autos de cada tienda (idTienda-index)
     *   - solo categoria:  autos de esa categoria (idCategoria-index)
     *   - sin filtros:     todos los autos (Scan)
     * y al final se descartan los no disponibles y los de otra categoria.
     */
    public List<Auto> buscarDisponibles(String ciudad, Long idCategoria) {
        List<Auto> candidatos = new ArrayList<>();
        if (ciudad != null) {
            tiendas.findByCiudadIgnoreCase(ciudad)
                    .forEach(t -> candidatos.addAll(porIndice("idTienda-index", t.getIdTienda())));
        } else if (idCategoria != null) {
            candidatos.addAll(porIndice("idCategoria-index", idCategoria));
        } else {
            candidatos.addAll(findAll());
        }
        return candidatos.stream()
                .filter(a -> Boolean.TRUE.equals(a.getDisponibilidad()))
                .filter(a -> idCategoria == null || idCategoria.equals(a.getIdCategoria()))
                .toList();
    }
}
