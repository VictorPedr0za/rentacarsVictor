package com.rentacars.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

/**
 * Tabla DynamoDB "unicos": reemplaza a las restricciones UNIQUE de SQL.
 *
 * DynamoDB solo garantiza unicidad de la LLAVE PRIMARIA. Para que no existan dos clientes con el
 * mismo email (o dos autos con la misma placa, o dos categorias con el mismo nombre) se guarda
 * un item "candado" cuya llave ES el valor que debe ser unico, por ejemplo:
 *
 *      clave = "cliente.email#juan@email.com"   ->   idEntidad = 7
 *
 * Crear el candado usa una condicion (attribute_not_exists): si ya existe, DynamoDB lo rechaza
 * de forma atomica, aunque dos peticiones lleguen al mismo tiempo. El candado y el item real se
 * escriben juntos en una transaccion.
 */
@DynamoDbBean
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Unico {

    @Getter(onMethod_ = @DynamoDbPartitionKey)
    private String clave;

    private Long idEntidad;
}
