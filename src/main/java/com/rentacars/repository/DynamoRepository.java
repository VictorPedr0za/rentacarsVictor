package com.rentacars.repository;

import com.rentacars.config.DynamoProperties;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

import java.util.List;
import java.util.Optional;

/**
 * Base de todos los repositorios. Reemplaza a JpaRepository (Spring Data JPA) de la version SQL.
 *
 * Se conservan los NOMBRES de metodo de JpaRepository (findById, findAll, save, delete,
 * existsById...) para que los servicios casi no cambien. Lo que cambia es lo de adentro:
 *
 *   JPA / SQL                              DynamoDB
 *   ------------------------------------   ------------------------------------------
 *   findById(id)   -> SELECT ... WHERE id  GetItem por llave primaria (rapidisimo)
 *   findAll()      -> SELECT *             Scan (recorre toda la tabla: OK solo si es pequena)
 *   findByXxx(...) -> WHERE xxx = ?        Query sobre un INDICE SECUNDARIO GLOBAL (GSI)
 *   save(entidad)  -> INSERT / UPDATE      PutItem (crea o reemplaza el item completo)
 *
 * Cada repositorio concreto declara su tabla (nombre + indices) y la clase DynamoSchema (config)
 * la crea al arrancar si no existe.
 */
public abstract class DynamoRepository<T> {

    protected final DynamoDbClient cliente;
    protected final DynamoDbEnhancedClient enhanced;
    protected final DynamoDbTable<T> tabla;
    protected final String nombreTabla;

    /** Orden por llave primaria, para resultados estables (DynamoDB no ordena los Scan). */
    private final java.util.Comparator<T> porId = java.util.Comparator.comparing(this::idDe);

    protected DynamoRepository(DynamoDbClient cliente, DynamoDbEnhancedClient enhanced,
                               DynamoProperties props, String nombreLogico, Class<T> tipo) {
        this.cliente = cliente;
        this.enhanced = enhanced;
        // nombre real = prefijo + nombre logico, por ejemplo "rentacars_tiendas"
        this.nombreTabla = props.getTablePrefix() + nombreLogico;
        this.tabla = enhanced.table(nombreTabla, TableSchema.fromBean(tipo));
    }

    /** Valor de la llave primaria de un item. */
    protected abstract Long idDe(T item);

    /** Indices secundarios globales (GSI) de la tabla; vacio si no tiene. */
    protected List<EnhancedGlobalSecondaryIndex> indices() {
        return List.of();
    }

    public String nombreTabla() {
        return nombreTabla;
    }

    public DynamoDbTable<T> tabla() {
        return tabla;
    }

    // ------------------------------------------------------------------ operaciones basicas

    public Optional<T> findById(Long id) {
        return Optional.ofNullable(tabla.getItem(llave(id)));
    }

    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    /**
     * Scan: lee TODA la tabla. En una tabla grande se usaria paginacion o un indice.
     * OJO: DynamoDB no garantiza ningun orden en un Scan (en SQL lo normal era el orden de la llave),
     * asi que se ordena por id para que la API devuelva siempre las listas igual.
     */
    public List<T> findAll() {
        return tabla.scan().items().stream().sorted(porId).toList();
    }

    public boolean estaVacia() {
        return tabla.scan(r -> r.limit(1)).items().stream().findAny().isEmpty();
    }

    public void delete(T item) {
        tabla.deleteItem(llave(idDe(item)));
    }

    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }

    // ------------------------------------------------------------------ consultas por indice

    protected List<T> porIndice(String indice, Object valor) {
        return tabla.index(indice)
                .query(QueryConditional.keyEqualTo(llave(valor)))
                .stream()
                .flatMap(pagina -> pagina.items().stream())
                .sorted(porId)
                .toList();
    }

    protected boolean existePorIndice(String indice, Object valor) {
        return tabla.index(indice)
                .query(QueryEnhancedRequest.builder()
                        .queryConditional(QueryConditional.keyEqualTo(llave(valor)))
                        .limit(1)
                        .build())
                .stream()
                .flatMap(pagina -> pagina.items().stream())
                .findAny()
                .isPresent();
    }

    // ------------------------------------------------------------------ utilidades

    protected static Key llave(Object valor) {
        return valor instanceof Number n
                ? Key.builder().partitionValue(n).build()
                : Key.builder().partitionValue(String.valueOf(valor)).build();
    }

    protected static EnhancedGlobalSecondaryIndex indice(String nombre) {
        return EnhancedGlobalSecondaryIndex.builder()
                .indexName(nombre)
                .projection(p -> p.projectionType(ProjectionType.ALL))
                .build();
    }

    /** true si la transaccion se cancelo porque la condicion del item numero 'posicion' fallo. */
    protected static boolean fallaCondicion(TransactionCanceledException e, int posicion) {
        return e.hasCancellationReasons()
                && e.cancellationReasons().size() > posicion
                && "ConditionalCheckFailed".equals(e.cancellationReasons().get(posicion).code());
    }

    // ------------------------------------------------------------------ creacion de la tabla

    /** Crea la tabla (con sus indices) si todavia no existe y espera a que quede ACTIVA. */
    public void crearTablaSiNoExiste() {
        try {
            cliente.describeTable(DescribeTableRequest.builder().tableName(nombreTabla).build());
            return;
        } catch (ResourceNotFoundException ignorada) {
            // no existe: se crea abajo
        }
        CreateTableEnhancedRequest.Builder peticion = CreateTableEnhancedRequest.builder();
        if (!indices().isEmpty()) {
            peticion.globalSecondaryIndices(indices());
        }
        try {
            // sin "provisioned throughput" -> facturacion PAY_PER_REQUEST (se paga solo por uso)
            tabla.createTable(peticion.build());
        } catch (ResourceInUseException otraInstanciaLaEstaCreando) {
            // otra copia de la app la creo al mismo tiempo: solo falta esperarla
        }
        cliente.waiter().waitUntilTableExists(r -> r.tableName(nombreTabla));
    }
}
