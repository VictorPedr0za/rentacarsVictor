package com.rentacars.repository;

import com.rentacars.config.DynamoProperties;
import com.rentacars.exception.BadRequestException;
import com.rentacars.model.Alquiler;
import com.rentacars.model.Auto;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.enhanced.dynamodb.model.IgnoreNullsMode;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactDeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactUpdateItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

import java.time.LocalDate;
import java.util.List;

@Repository
public class AlquilerRepository extends DynamoRepository<Alquiler> {

    private static final String ACTIVO = "ACTIVO";
    private static final String CERRADO = "CERRADO";

    private final IdGenerator ids;
    private final AutoRepository autos;

    public AlquilerRepository(DynamoDbClient cliente, DynamoDbEnhancedClient enhanced, DynamoProperties props,
                              IdGenerator ids, AutoRepository autos) {
        super(cliente, enhanced, props, "alquileres", Alquiler.class);
        this.ids = ids;
        this.autos = autos;
    }

    @Override
    protected Long idDe(Alquiler item) {
        return item.getIdAlquiler();
    }

    @Override
    protected List<EnhancedGlobalSecondaryIndex> indices() {
        // estado-fechaFin-index: estado es la llave de particion y fechaFin la de orden (ver model/Alquiler)
        return List.of(indice("idCliente-index"), indice("idAuto-index"), indice("estado-fechaFin-index"));
    }

    // ------------------------------------------------------------------ consultas

    /** HU-20: historial de un cliente (indice idCliente-index). */
    public List<Alquiler> findByIdCliente(Long idCliente) {
        return porIndice("idCliente-index", idCliente);
    }

    public boolean existsByIdAuto(Long idAuto) {
        return existePorIndice("idAuto-index", idAuto);
    }

    public boolean existsByIdCliente(Long idCliente) {
        return existePorIndice("idCliente-index", idCliente);
    }

    /**
     * HU-21: activos no vencidos. Una sola consulta al indice estado-fechaFin-index:
     * particion = estado, y el rango de la llave de orden (fechaFin >= hoy) lo resuelve DynamoDB.
     */
    public List<Alquiler> findByFechaFinGreaterThanEqualAndEstado(LocalDate fechaFin, String estado) {
        return tabla.index("estado-fechaFin-index")
                .query(QueryConditional.sortGreaterThanOrEqualTo(
                        Key.builder().partitionValue(estado).sortValue(fechaFin.toString()).build()))
                .stream()
                .flatMap(pagina -> pagina.items().stream())
                .toList();
    }

    // ------------------------------------------------------------------ escritura

    /** Guardado simple (PUT /alquileres/update y datos de ejemplo). */
    public Alquiler save(Alquiler alquiler) {
        if (alquiler.getIdAlquiler() == null) {
            alquiler.setIdAlquiler(ids.siguiente("alquileres"));
        }
        tabla.putItem(alquiler);
        return alquiler;
    }

    /**
     * HU-18. Crea el alquiler Y marca el auto como no disponible en UNA transaccion, con una
     * condicion: el auto debe estar disponible en el instante exacto de escribir.
     *
     * Esto resuelve un problema que el codigo SQL tenia: dos clientes alquilando el mismo auto
     * al mismo tiempo podian pasar los dos la validacion "esta disponible?". Aqui DynamoDB
     * rechaza la segunda transaccion de forma atomica.
     */
    public Alquiler crearOcupandoAuto(Alquiler alquiler) {
        alquiler.setIdAlquiler(ids.siguiente("alquileres"));
        try {
            enhanced.transactWriteItems(TransactWriteItemsEnhancedRequest.builder()
                    .addUpdateItem(autos.tabla(), cambiarDisponibilidad(alquiler.getIdAuto(), false, true)) // item 0
                    .addPutItem(tabla, alquiler)                                                         // item 1
                    .build());
        } catch (TransactionCanceledException e) {
            if (fallaCondicion(e, 0)) {
                throw new BadRequestException("El auto no esta disponible");
            }
            throw e;
        }
        return alquiler;
    }

    /** HU-24. Cierra el alquiler y libera el auto, o ninguna de las dos cosas. */
    public Alquiler cerrarLiberandoAuto(Alquiler alquiler) {
        Alquiler cambio = Alquiler.builder().idAlquiler(alquiler.getIdAlquiler()).estado(CERRADO).build();
        try {
            enhanced.transactWriteItems(TransactWriteItemsEnhancedRequest.builder()
                    .addUpdateItem(tabla, TransactUpdateItemEnhancedRequest.builder(Alquiler.class)
                            .item(cambio)
                            .ignoreNullsMode(IgnoreNullsMode.SCALAR_ONLY)
                            // solo se cierra si sigue ACTIVO: una devolucion no se registra dos veces
                            .conditionExpression(Expression.builder()
                                    .expression("attribute_exists(idAlquiler) AND estado = :activo")
                                    .putExpressionValue(":activo", AttributeValue.fromS(ACTIVO))
                                    .build())
                            .build())                                                           // item 0
                    .addUpdateItem(autos.tabla(), cambiarDisponibilidad(alquiler.getIdAuto(), true, null)) // item 1
                    .build());
        } catch (TransactionCanceledException e) {
            if (fallaCondicion(e, 0)) {
                throw new BadRequestException("El alquiler ya fue cerrado, la devolucion ya esta registrada");
            }
            throw e;
        }
        alquiler.setEstado(CERRADO);
        return alquiler;
    }

    /** HU-22. Borra el alquiler cancelado y libera el auto, o ninguna de las dos cosas. */
    public void cancelarLiberandoAuto(Alquiler alquiler) {
        enhanced.transactWriteItems(TransactWriteItemsEnhancedRequest.builder()
                .addDeleteItem(tabla, TransactDeleteItemEnhancedRequest.builder()
                        .key(llave(alquiler.getIdAlquiler()))
                        .conditionExpression(Expression.builder().expression("attribute_exists(idAlquiler)").build())
                        .build())
                .addUpdateItem(autos.tabla(), cambiarDisponibilidad(alquiler.getIdAuto(), true, null))
                .build());
    }

    /**
     * UPDATE parcial del auto dentro de una transaccion: solo cambia "disponibilidad".
     * 'exigida' (si no es null) es el valor que DEBE tener hoy para que la transaccion siga.
     * El attribute_exists evita que UpdateItem cree un auto fantasma si el id no existe.
     */
    private static TransactUpdateItemEnhancedRequest<Auto> cambiarDisponibilidad(Long idAuto, boolean nueva, Boolean exigida) {
        Expression.Builder condicion = Expression.builder();
        if (exigida == null) {
            condicion.expression("attribute_exists(idAuto)");
        } else {
            condicion.expression("attribute_exists(idAuto) AND disponibilidad = :exigida")
                    .putExpressionValue(":exigida", AttributeValue.fromBool(exigida));
        }
        return TransactUpdateItemEnhancedRequest.builder(Auto.class)
                .item(Auto.builder().idAuto(idAuto).disponibilidad(nueva).build())
                .ignoreNullsMode(IgnoreNullsMode.SCALAR_ONLY)
                .conditionExpression(condicion.build())
                .build();
    }
}
