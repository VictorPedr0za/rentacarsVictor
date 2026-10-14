package com.rentacars.config;

import com.rentacars.repository.DynamoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Crea las tablas de DynamoDB, si todavia no existen (lo dispara InicializacionDynamo al arrancar).
 *
 * Reemplaza al script_bd.sql (la parte CREATE TABLE): en SQL el esquema lo manda un script y
 * Hibernate solo lo validaba; aqui cada repositorio declara su tabla e indices (ver
 * DynamoRepository.indices()) y esta clase los crea. Es idempotente: si la tabla ya existe, no hace nada.
 *
 * Se puede apagar con app.dynamodb.auto-create=false si las tablas las crea otra herramienta
 * (consola de AWS, CloudFormation...) o si el usuario de AWS no tiene permiso de CreateTable.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TablasDynamo {

    private final List<DynamoRepository<?>> repositorios;
    private final DynamoProperties props;

    public void crearTablas() {
        if (!props.isAutoCreate()) {
            log.info("Creacion automatica de tablas desactivada (app.dynamodb.auto-create=false)");
            return;
        }
        for (DynamoRepository<?> repositorio : repositorios) {
            repositorio.crearTablaSiNoExiste();
        }
        log.info("Tablas DynamoDB listas ({} tablas, prefijo '{}', {})",
                repositorios.size(), props.getTablePrefix(),
                props.esLocal() ? "DynamoDB local: " + props.getEndpoint() : "AWS region " + props.getRegion());
    }
}
