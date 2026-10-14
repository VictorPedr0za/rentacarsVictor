package com.rentacars;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;

/**
 * Arranca DynamoDB Local en tu computador (puerto 8000) para desarrollar sin AWS.
 * No hace falta Docker ni instalar nada: usa la libreria que Maven ya descargo.
 *
 * Los datos se guardan en la carpeta ".dynamodb-local" (ignorada por git), asi que sobreviven
 * entre ejecuciones. Para empezar de cero, detener este proceso y borrar esa carpeta.
 *
 * Lo ejecutan los accesos directos (iniciar-backend.bat) o a mano:
 *   mvnw -q test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass=com.rentacars.LocalDynamoDb
 */
public class LocalDynamoDb {

    public static void main(String[] args) throws Exception {
        String puerto = args.length > 0 ? args[0] : "8000";
        // DynamoDB Local exige que la carpeta de datos exista antes de arrancar
        java.nio.file.Files.createDirectories(java.nio.file.Path.of(".dynamodb-local"));
        DynamoDBProxyServer servidor = ServerRunner.createServerFromCommandLineArgs(
                new String[]{"-sharedDb", "-dbPath", ".dynamodb-local", "-port", puerto});
        servidor.start();
        System.out.println("DynamoDB Local listo en http://localhost:" + puerto + "  (Ctrl+C para detener)");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                servidor.stop();
            } catch (Exception ignorada) {
                // al cerrar no importa
            }
        }));
        Thread.currentThread().join();
    }
}
