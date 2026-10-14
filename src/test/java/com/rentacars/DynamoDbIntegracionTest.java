package com.rentacars;

import com.jayway.jsonpath.JsonPath;
import com.rentacars.support.DynamoLocalServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integracion: toda la aplicacion (controllers, services, repositories) contra una base
 * DynamoDB Local REAL. Verifican justo lo que cambia al pasar de SQL a NoSQL: creacion de tablas,
 * ids por contador, candados de unicidad, indices secundarios, transacciones y concurrencia.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DynamoDbIntegracionTest {

    @DynamicPropertySource
    static void dynamo(DynamicPropertyRegistry registry) {
        registry.add("app.dynamodb.endpoint", DynamoLocalServer::endpoint);
        registry.add("app.dynamodb.table-prefix", () -> "it_");
    }

    @Autowired
    MockMvc mvc;

    // ------------------------------------------------------------------ utilidades

    private ResultActions enviar(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder peticion,
                                 String json) throws Exception {
        return mvc.perform(peticion.contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private long idDe(MvcResult resultado, String campo) throws Exception {
        Number valor = JsonPath.read(resultado.getResponse().getContentAsString(), "$." + campo);
        return valor.longValue();
    }

    private String unico() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private long crearCliente(String email) throws Exception {
        return idDe(enviar(post("/clientes"), """
                { "nombre": "Cliente Prueba", "email": "%s", "telefono": "3000000000",
                  "tarjeta_credito": "4111111111111111" }""".formatted(email))
                .andExpect(status().isCreated()).andReturn(), "id_cliente");
    }

    private String jsonAuto(String placa, long tienda, long categoria) {
        return """
                { "modelo": "Modelo X", "marca": "Marca Y", "anio": "2024", "placa": "%s",
                  "precio_dia": 100000, "oferta_porcentaje": 10,
                  "id_tienda": %d, "id_categoria": %d }""".formatted(placa, tienda, categoria);
    }

    private long crearAuto(String placa) throws Exception {
        return idDe(enviar(post("/autos"), jsonAuto(placa, 1, 1))
                .andExpect(status().isCreated()).andReturn(), "id_auto");
    }

    private String jsonAlquiler(long cliente, long auto) {
        LocalDate inicio = LocalDate.now().plusDays(5);
        return """
                { "id_cliente": %d, "id_auto": %d, "fecha_inicio": "%s", "fecha_fin": "%s",
                  "ciudad_retirada": "Bogota", "ciudad_devolucion": "Cali" }"""
                .formatted(cliente, auto, inicio, inicio.plusDays(3));
    }

    private boolean disponible(long auto) throws Exception {
        String cuerpo = mvc.perform(get("/autos/" + auto)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(cuerpo, "$.disponibilidad");
    }

    // ------------------------------------------------------------------ arranque

    @Test
    void alArrancarCreaLasTablasYCargaLosDatosDeEjemplo() throws Exception {
        mvc.perform(get("/tiendas")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(8)))
                .andExpect(jsonPath("$[?(@.nombre == 'AutoRent Norte')].ciudad").value("Bogotá"));
        mvc.perform(get("/categorias")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(8)));
        // el detalle del auto ahora viaja dentro del documento y la API lo sigue entregando plano
        mvc.perform(get("/autos/all")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.modelo == 'Corolla')].marca").value("Toyota"));
        // los alquileres activos de ejemplo (fechas relativas a hoy) salen del indice estado-fechaFin
        mvc.perform(get("/alquileres/activos")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)));
        // la tarjeta sale enmascarada
        mvc.perform(get("/clientes")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tarjeta_credito", containsString("*")));
    }

    @Test
    void elBackendTambienSirveElFrontendEnLaMismaDireccion() throws Exception {
        // un solo puerto: el frontend y la API comparten origen, asi no puede haber problemas de CORS
        // (MockMvc no sigue el reenvio interno de "/" a index.html; el navegador si, y se verifica en vivo)
        mvc.perform(get("/index.html")).andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content().string(containsString("AutoRent")));
        mvc.perform(get("/js/app.js")).andExpect(status().isOk());
        mvc.perform(get("/css/styles.css")).andExpect(status().isOk());
        // y los archivos internos del frontend no se publican
        mvc.perform(get("/servidor.js")).andExpect(status().isNotFound());
    }

    @Test
    void corsAceptaCualquierPuertoLocalYRechazaOtrosOrigenes() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/tiendas")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/tiendas")
                        .header("Origin", "http://127.0.0.1:5500")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/tiendas")
                        .header("Origin", "http://sitio-ajeno.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ ids (reemplazo de BIGSERIAL)

    @Test
    void losIdsSonNumericosYConsecutivos() throws Exception {
        long primera = idDe(enviar(post("/tiendas"),
                "{ \"nombre\": \"T1\", \"ciudad\": \"Pereira\", \"direccion\": \"Cra 1\" }")
                .andExpect(status().isCreated()).andReturn(), "id_tienda");
        long segunda = idDe(enviar(post("/tiendas"),
                "{ \"nombre\": \"T2\", \"ciudad\": \"Pereira\", \"direccion\": \"Cra 2\" }")
                .andExpect(status().isCreated()).andReturn(), "id_tienda");
        assertThat(segunda).isEqualTo(primera + 1);

        // HU-03 por el indice ciudad-index, sin importar mayusculas
        mvc.perform(get("/tiendas").param("ciudad", "PEREIRA")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ------------------------------------------------------------------ candados (reemplazo de UNIQUE)

    @Test
    void emailRepetidoSeRechazaYSeLiberaAlBorrarElCliente() throws Exception {
        String email = unico() + "@test.com";
        long id = crearCliente(email);

        enviar(post("/clientes"), """
                { "nombre": "Otro", "email": "%s", "telefono": "3000000001", "tarjeta_credito": "4222222222222222" }"""
                .formatted(email))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El email ya existe"));

        mvc.perform(delete("/clientes/" + id)).andExpect(status().isNoContent());
        // el candado se libero junto con el cliente: el email vuelve a estar disponible
        crearCliente(email);
    }

    @Test
    void placaRepetidaSeRechazaYSeLiberaAlBorrarElAuto() throws Exception {
        String placa = "P-" + unico();
        long id = crearAuto(placa);

        enviar(post("/autos"), jsonAuto(placa, 1, 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La placa ya esta registrada"));

        mvc.perform(delete("/autos/" + id)).andExpect(status().isNoContent());
        crearAuto(placa);
    }

    @Test
    void nombreDeCategoriaUnicoYRenombrableLiberaElNombreViejo() throws Exception {
        String nombre = "Cat-" + unico();
        long id = idDe(enviar(post("/categorias"),
                "{ \"nombre\": \"%s\", \"descripcion\": \"d\" }".formatted(nombre))
                .andExpect(status().isCreated()).andReturn(), "id_categoria");

        enviar(post("/categorias"), "{ \"nombre\": \"%s\", \"descripcion\": \"d\" }".formatted(nombre))
                .andExpect(status().isBadRequest());

        // renombrar a un nombre ocupado por OTRA categoria (una del ejemplo) falla
        enviar(put("/categorias/" + id), "{ \"nombre\": \"SUV\" }").andExpect(status().isBadRequest());

        // renombrar a uno libre funciona y libera el anterior
        String nuevo = "Nuevo-" + unico();
        enviar(put("/categorias/" + id), "{ \"nombre\": \"%s\" }".formatted(nuevo)).andExpect(status().isOk());
        enviar(post("/categorias"), "{ \"nombre\": \"%s\", \"descripcion\": \"d\" }".formatted(nombre))
                .andExpect(status().isCreated());
    }

    // ------------------------------------------------------------------ autos

    @Test
    void autoGuardaSuDetalleAnidadoYSePuedeActualizarPorPartes() throws Exception {
        long id = crearAuto("A-" + unico());

        mvc.perform(get("/autos/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.marca").value("Marca Y"))
                .andExpect(jsonPath("$.precio_con_oferta").value(90000.0))
                .andExpect(jsonPath("$.disponibilidad").value(true));

        // HU-10: cambia solo el detalle; HU-11: cambia solo la disponibilidad (no se pisan entre si)
        enviar(put("/autos/" + id), "{ \"precio_dia\": 200000, \"oferta_porcentaje\": 50 }").andExpect(status().isOk());
        enviar(patch("/autos/" + id + "/disponibilidad"), "{ \"disponibilidad\": false }").andExpect(status().isOk());

        mvc.perform(get("/autos/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.precio_con_oferta").value(100000.0))
                .andExpect(jsonPath("$.marca").value("Marca Y"))
                .andExpect(jsonPath("$.disponibilidad").value(false));
    }

    @Test
    void autoInexistenteDa404EnVezDeCrearseUnItemFantasma() throws Exception {
        enviar(patch("/autos/999999/disponibilidad"), "{ \"disponibilidad\": true }").andExpect(status().isNotFound());
        mvc.perform(get("/autos/999999")).andExpect(status().isNotFound());
    }

    @Test
    void busquedaDeAutosPorCiudadYCategoriaUsaLosIndices() throws Exception {
        // en los datos de ejemplo el Corolla esta disponible en Medellin y es categoria 2
        mvc.perform(get("/autos").param("ciudad", "medellín")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.modelo == 'Corolla')].marca").value("Toyota"));
        mvc.perform(get("/autos").param("id_categoria", "2")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.modelo == 'Corolla')].marca").value("Toyota"));
        // los autos no disponibles (CX-5) nunca aparecen
        mvc.perform(get("/autos").param("ciudad", "Cali")).andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.modelo == 'CX-5')]").isEmpty());
    }

    // ------------------------------------------------------------------ alquileres (transacciones)

    @Test
    void alquilerOcupaYLiberaElAutoEnTransaccion() throws Exception {
        long cliente = crearCliente(unico() + "@test.com");
        long auto = crearAuto("R-" + unico());

        // HU-18: crea el alquiler y deja el auto no disponible
        MvcResult creado = enviar(post("/alquileres"), jsonAlquiler(cliente, auto))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.precio_total").value(270000.0)) // 3 dias * 90000
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andReturn();
        long alquiler = idDe(creado, "id_alquiler");
        assertThat(disponible(auto)).isFalse();

        // otro alquiler del mismo auto: rechazado
        enviar(post("/alquileres"), jsonAlquiler(cliente, auto)).andExpect(status().isBadRequest());

        // HU-20: historial por indice idCliente-index
        mvc.perform(get("/alquileres").param("id_cliente", String.valueOf(cliente))).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // HU-24: devolucion cierra y libera; repetirla no esta permitido
        mvc.perform(put("/alquileres/" + alquiler + "/devolucion")).andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADO"));
        assertThat(disponible(auto)).isTrue();
        mvc.perform(put("/alquileres/" + alquiler + "/devolucion")).andExpect(status().isBadRequest());

        // un auto con alquileres en su historial no se puede borrar (no hay llaves foraneas: lo valida el servicio)
        mvc.perform(delete("/autos/" + auto)).andExpect(status().isBadRequest());
        // ni un cliente con alquileres
        mvc.perform(delete("/clientes/" + cliente)).andExpect(status().isBadRequest());
    }

    @Test
    void cancelarUnAlquilerFuturoBorraLaReservaYLiberaElAuto() throws Exception {
        long cliente = crearCliente(unico() + "@test.com");
        long auto = crearAuto("C-" + unico());
        long alquiler = idDe(enviar(post("/alquileres"), jsonAlquiler(cliente, auto))
                .andExpect(status().isCreated()).andReturn(), "id_alquiler");

        mvc.perform(delete("/alquileres/" + alquiler)).andExpect(status().isNoContent());

        assertThat(disponible(auto)).isTrue();
        mvc.perform(get("/alquileres/" + alquiler)).andExpect(status().isNotFound());
    }

    @Test
    void dosClientesAlquilandoElMismoAutoALaVezSoloUnoLoConsigue() throws Exception {
        long clienteA = crearCliente(unico() + "@test.com");
        long clienteB = crearCliente(unico() + "@test.com");
        long auto = crearAuto("X-" + unico());

        CountDownLatch salida = new CountDownLatch(1);
        ExecutorService hilos = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> a = hilos.submit(() -> {
                salida.await();
                return mvc.perform(post("/alquileres").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAlquiler(clienteA, auto))).andReturn().getResponse().getStatus();
            });
            Future<Integer> b = hilos.submit(() -> {
                salida.await();
                return mvc.perform(post("/alquileres").contentType(MediaType.APPLICATION_JSON)
                        .content(jsonAlquiler(clienteB, auto))).andReturn().getResponse().getStatus();
            });
            salida.countDown();
            // exactamente una peticion gana (201) y la otra pierde la condicion de la transaccion (400)
            assertThat(java.util.List.of(a.get(), b.get())).containsExactlyInAnyOrder(201, 400);
        } finally {
            hilos.shutdownNow();
        }
    }

    // ------------------------------------------------------------------ borrados con dependencias

    @Test
    void tiendaConAutosNoSeBorra_unaVaciaSi() throws Exception {
        // la tienda 1 tiene autos
        mvc.perform(delete("/tiendas/1")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("autos asociados")));

        long vacia = idDe(enviar(post("/tiendas"),
                "{ \"nombre\": \"Vacia\", \"ciudad\": \"Armenia\", \"direccion\": \"Cra 9\" }")
                .andExpect(status().isCreated()).andReturn(), "id_tienda");
        mvc.perform(delete("/tiendas/" + vacia)).andExpect(status().isNoContent());
        mvc.perform(get("/tiendas/" + vacia)).andExpect(status().isNotFound());
    }
}
