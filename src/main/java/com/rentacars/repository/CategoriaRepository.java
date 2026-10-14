package com.rentacars.repository;

import com.rentacars.config.DynamoProperties;
import com.rentacars.exception.BadRequestException;
import com.rentacars.model.Categoria;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

@Repository
public class CategoriaRepository extends DynamoRepository<Categoria> {

    private static final String TIPO_NOMBRE = "categoria.nombre";
    private static final String MENSAJE_DUPLICADA = "Ya existe una categoria con ese nombre";

    private final IdGenerator ids;
    private final UnicoRepository unicos;

    public CategoriaRepository(DynamoDbClient cliente, DynamoDbEnhancedClient enhanced,
                               DynamoProperties props, IdGenerator ids, UnicoRepository unicos) {
        super(cliente, enhanced, props, "categorias", Categoria.class);
        this.ids = ids;
        this.unicos = unicos;
    }

    @Override
    protected Long idDe(Categoria item) {
        return item.getIdCategoria();
    }

    /** HU-06: ya no es un UNIQUE de la BD; se consulta el "candado" del nombre. */
    public boolean existsByNombre(String nombre) {
        return unicos.existe(UnicoRepository.clave(TIPO_NOMBRE, nombre));
    }

    /** Al renombrar: el nombre no puede ser el de OTRA categoria (el propio si puede conservarlo). */
    public boolean existsByNombreAndIdCategoriaNot(String nombre, Long idCategoria) {
        return unicos.buscarIdEntidad(UnicoRepository.clave(TIPO_NOMBRE, nombre))
                .map(duenio -> !duenio.equals(idCategoria))
                .orElse(false);
    }

    /**
     * Crea o actualiza. El candado del nombre y la categoria se escriben en UNA transaccion:
     * o quedan los dos o no queda ninguno (esto reemplaza al @Transactional de JPA).
     */
    public Categoria save(Categoria categoria) {
        boolean esNueva = categoria.getIdCategoria() == null;
        if (esNueva) {
            categoria.setIdCategoria(ids.siguiente("categorias"));
        }
        String claveNueva = UnicoRepository.clave(TIPO_NOMBRE, categoria.getNombre());

        TransactWriteItemsEnhancedRequest.Builder tx = TransactWriteItemsEnhancedRequest.builder();
        if (esNueva) {
            tx.addPutItem(unicos.tabla(), unicos.pedir(claveNueva, categoria.getIdCategoria()));
        } else {
            Categoria anterior = findById(categoria.getIdCategoria()).orElse(null);
            if (anterior != null && !anterior.getNombre().equals(categoria.getNombre())) {
                // renombrar: se libera el nombre viejo y se reserva el nuevo
                tx.addDeleteItem(unicos.tabla(), unicos.soltar(UnicoRepository.clave(TIPO_NOMBRE, anterior.getNombre())));
                tx.addPutItem(unicos.tabla(), unicos.pedir(claveNueva, categoria.getIdCategoria()));
            }
        }
        tx.addPutItem(tabla, categoria);

        try {
            enhanced.transactWriteItems(tx.build());
        } catch (TransactionCanceledException e) {
            // si lo que fallo fue la condicion del candado, el nombre ya estaba tomado
            if (esDuplicado(e)) {
                throw new BadRequestException(MENSAJE_DUPLICADA);
            }
            throw e;
        }
        return categoria;
    }

    @Override
    public void delete(Categoria categoria) {
        enhanced.transactWriteItems(TransactWriteItemsEnhancedRequest.builder()
                .addDeleteItem(tabla, llave(categoria.getIdCategoria()))
                .addDeleteItem(unicos.tabla(), unicos.soltar(UnicoRepository.clave(TIPO_NOMBRE, categoria.getNombre())))
                .build());
    }

    private static boolean esDuplicado(TransactionCanceledException e) {
        return e.hasCancellationReasons()
                && e.cancellationReasons().stream().anyMatch(r -> "ConditionalCheckFailed".equals(r.code()));
    }
}
