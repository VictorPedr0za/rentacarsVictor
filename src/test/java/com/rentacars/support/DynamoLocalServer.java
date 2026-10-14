package com.rentacars.support;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;

import java.net.ServerSocket;

/**
 * DynamoDB Local EMBEBIDO: la misma base DynamoDB, pero corriendo dentro del proceso de las pruebas,
 * en memoria y en un puerto libre. Asi las pruebas de integracion ejercitan consultas, indices y
 * transacciones REALES de DynamoDB sin necesitar Docker, internet ni una cuenta de AWS.
 *
 * Una sola instancia por ejecucion de pruebas; se cierra sola al terminar.
 */
public final class DynamoLocalServer {

    private static DynamoDBProxyServer servidor;
    private static String endpoint;

    private DynamoLocalServer() {
    }

    /** URL del DynamoDB Local (lo arranca la primera vez que se pide). */
    public static synchronized String endpoint() {
        if (servidor == null) {
            try {
                int puerto = puertoLibre();
                servidor = ServerRunner.createServerFromCommandLineArgs(
                        new String[]{"-inMemory", "-port", String.valueOf(puerto)});
                servidor.start();
                endpoint = "http://localhost:" + puerto;
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        servidor.stop();
                    } catch (Exception ignorada) {
                        // al cerrar la JVM no importa
                    }
                }));
            } catch (Exception e) {
                throw new IllegalStateException("No se pudo arrancar DynamoDB Local", e);
            }
        }
        return endpoint;
    }

    private static int puertoLibre() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
