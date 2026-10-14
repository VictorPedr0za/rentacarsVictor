package com.rentacars.repository;

import com.rentacars.config.DynamoProperties;
import com.rentacars.exception.BadRequestException;
import com.rentacars.model.Cliente;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

import java.util.List;

@Repository
public class ClienteRepository extends DynamoRepository<Cliente> {

    private static final String TIPO_EMAIL = "cliente.email";

    private final IdGenerator ids;
    private final UnicoRepository unicos;

    public ClienteRepository(DynamoDbClient cliente, DynamoDbEnhancedClient enhanced,
                             DynamoProperties props, IdGenerator ids, UnicoRepository unicos) {
        super(cliente, enhanced, props, "clientes", Cliente.class);
        this.ids = ids;
        this.unicos = unicos;
    }

    @Override
    protected Long idDe(Cliente item) {
        return item.getIdCliente();
    }

    @Override
    protected List<EnhancedGlobalSecondaryIndex> indices() {
        return List.of(indice("email-index"));
    }

    /** HU-14: consulta rapida por el indice email-index (la garantia real es el candado, ver save). */
    public boolean existsByEmail(String email) {
        return existePorIndice("email-index", email);
    }

    /**
     * Cliente nuevo: reserva el email y guarda el cliente en UNA transaccion. Si dos peticiones
     * intentan el mismo email a la vez, DynamoDB rechaza la segunda de forma atomica.
     * Cliente existente (HU-15): el email nunca cambia, asi que basta con reemplazar el item.
     */
    public Cliente save(Cliente cliente) {
        if (cliente.getIdCliente() != null) {
            tabla.putItem(cliente);
            return cliente;
        }
        cliente.setIdCliente(ids.siguiente("clientes"));
        try {
            enhanced.transactWriteItems(TransactWriteItemsEnhancedRequest.builder()
                    .addPutItem(unicos.tabla(),
                            unicos.pedir(UnicoRepository.clave(TIPO_EMAIL, cliente.getEmail()), cliente.getIdCliente()))
                    .addPutItem(tabla, cliente)
                    .build());
        } catch (TransactionCanceledException e) {
            if (fallaCondicion(e, 0)) {
                throw new BadRequestException("El email ya existe");
            }
            throw e;
        }
        return cliente;
    }

    /** Borra el cliente y libera su email (para poder registrarlo de nuevo), todo en una transaccion. */
    @Override
    public void delete(Cliente cliente) {
        enhanced.transactWriteItems(TransactWriteItemsEnhancedRequest.builder()
                .addDeleteItem(tabla, llave(cliente.getIdCliente()))
                .addDeleteItem(unicos.tabla(), unicos.soltar(UnicoRepository.clave(TIPO_EMAIL, cliente.getEmail())))
                .build());
    }
}
