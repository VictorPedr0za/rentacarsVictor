package com.rentacars.repository;

import com.rentacars.config.DynamoProperties;
import com.rentacars.model.Unico;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactDeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.util.Optional;

/**
 * "Candados" de unicidad (reemplazo de UNIQUE). Ver model/Unico.java para la idea completa.
 *
 * Este repositorio NO escribe por su cuenta: entrega las piezas (pedir / soltar) para que cada
 * repositorio las meta en la MISMA transaccion que guarda o borra su item. Asi el candado y el
 * dato nunca quedan desincronizados.
 */
@Repository
public class UnicoRepository extends DynamoRepository<Unico> {

    public UnicoRepository(DynamoDbClient cliente, DynamoDbEnhancedClient enhanced, DynamoProperties props) {
        super(cliente, enhanced, props, "unicos", Unico.class);
    }

    @Override
    protected Long idDe(Unico item) {
        throw new UnsupportedOperationException("La llave de unicos es un texto, no un numero");
    }

    /** Texto de la llave: tipo + valor, p. ej. clave("cliente.email", "a@b.com"). */
    public static String clave(String tipo, String valor) {
        return tipo + "#" + valor;
    }

    /** Id de la entidad que tiene reservado ese valor, si alguien lo tiene. */
    public Optional<Long> buscarIdEntidad(String clave) {
        return Optional.ofNullable(tabla.getItem(llave(clave))).map(Unico::getIdEntidad);
    }

    public boolean existe(String clave) {
        return buscarIdEntidad(clave).isPresent();
    }

    /** Pieza de transaccion: reservar el valor. Falla (de forma atomica) si ya esta reservado. */
    public TransactPutItemEnhancedRequest<Unico> pedir(String clave, Long idEntidad) {
        return TransactPutItemEnhancedRequest.builder(Unico.class)
                .item(new Unico(clave, idEntidad))
                .conditionExpression(Expression.builder().expression("attribute_not_exists(clave)").build())
                .build();
    }

    /** Pieza de transaccion: liberar el valor reservado. */
    public TransactDeleteItemEnhancedRequest soltar(String clave) {
        return TransactDeleteItemEnhancedRequest.builder()
                .key(Key.builder().partitionValue(clave).build())
                .build();
    }
}
