package com.rentacars.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

/**
 * Prepara la base de datos ANTES de que el servidor web empiece a recibir peticiones:
 *   1. crea las tablas que falten
 *   2. carga los datos de ejemplo si las tablas estan vacias
 *   3. corrige las imagenes del seed viejo (fotos al azar) si la base ya existia
 *
 * Por que aqui y no en un ApplicationRunner: los runners se ejecutan cuando el puerto 8080 ya esta
 * abierto, asi que las primeras peticiones podian llegar a tablas que todavia no existian (503).
 * SmartInitializingSingleton corre antes de abrir el puerto. Y si algo falla (credenciales de AWS
 * incorrectas, DynamoDB sin conexion...) la aplicacion no arranca a medias: se detiene con el error.
 */
@Component
@RequiredArgsConstructor
public class InicializacionDynamo implements SmartInitializingSingleton {

    private final TablasDynamo tablas;
    private final DatosIniciales datos;

    @Override
    public void afterSingletonsInstantiated() {
        tablas.crearTablas();
        datos.cargarSiHaceFalta();
        datos.corregirImagenesDeEjemplo();
    }
}
