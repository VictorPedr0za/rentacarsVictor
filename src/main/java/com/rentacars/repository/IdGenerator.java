package com.rentacars.repository;

import com.rentacars.config.DynamoProperties;
import com.rentacars.model.Contador;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.Map;

/**
 * Entrega ids numericos consecutivos (1, 2, 3...). Reemplaza al BIGSERIAL de PostgreSQL.
 *
 * COMO FUNCIONA: hay un item por entidad en la tabla "contadores" ({nombre: "clientes", valor: 7}).
 * Para pedir el siguiente id se hace UpdateItem con "ADD valor 1" y se pide de vuelta el valor nuevo.
 * Esa operacion es ATOMICA dentro de DynamoDB: dos peticiones simultaneas nunca reciben el mismo id,
 * y si el item no existe todavia, DynamoDB lo crea empezando en 0 (el primer id es el 1).
 */
@Repository
public class IdGenerator extends DynamoRepository<Contador> {

    public IdGenerator(DynamoDbClient cliente, DynamoDbEnhancedClient enhanced, DynamoProperties props) {
        super(cliente, enhanced, props, "contadores", Contador.class);
    }

    @Override
    protected Long idDe(Contador item) {
        throw new UnsupportedOperationException("La llave de contadores es un texto, no un numero");
    }

    /** Siguiente id para la entidad indicada ("tiendas", "autos", "clientes"...). */
    public long siguiente(String entidad) {
        UpdateItemResponse respuesta = cliente.updateItem(UpdateItemRequest.builder()
                .tableName(nombreTabla)
                .key(Map.of("nombre", AttributeValue.fromS(entidad)))
                .updateExpression("ADD valor :uno")
                .expressionAttributeValues(Map.of(":uno", AttributeValue.fromN("1")))
                .returnValues(ReturnValue.UPDATED_NEW)
                .build());
        return Long.parseLong(respuesta.attributes().get("valor").n());
    }
}
