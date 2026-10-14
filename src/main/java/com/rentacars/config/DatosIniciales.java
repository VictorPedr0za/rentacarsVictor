package com.rentacars.config;

import com.rentacars.model.Alquiler;
import com.rentacars.model.Auto;
import com.rentacars.model.Categoria;
import com.rentacars.model.Cliente;
import com.rentacars.model.Detalle_auto;
import com.rentacars.model.Tienda;
import com.rentacars.repository.AlquilerRepository;
import com.rentacars.repository.AutoRepository;
import com.rentacars.repository.CategoriaRepository;
import com.rentacars.repository.ClienteRepository;
import com.rentacars.repository.TiendaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Datos de ejemplo (la parte INSERT del antiguo script_bd.sql). Solo se cargan si la tabla de
 * tiendas esta vacia y app.dynamodb.seed=true, asi que es seguro dejarlo activo: nunca duplica.
 *
 * Las fechas de los alquileres son RELATIVAS a hoy, para que siempre haya alquileres activos
 * que mostrar. Los autos 3 y 6 quedan "no disponibles" porque tienen un alquiler activo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatosIniciales {

    /**
     * Ilustracion de cada auto de ejemplo (archivos de frontend/img/autos). Son rutas relativas, asi que las
     * sirve el mismo backend (puerto 8080) y tambien el frontend suelto (8081 / nginx). No dependen de internet.
     */
    private static final Map<String, String> IMAGEN_POR_PLACA = Map.of(
            "ABC-001", "/img/autos/hatchback.svg",
            "ABC-002", "/img/autos/sedan.svg",
            "ABC-003", "/img/autos/suv.svg",
            "ABC-004", "/img/autos/pickup.svg",
            "ABC-005", "/img/autos/luxury.svg",
            "ABC-006", "/img/autos/electric.svg",
            "ABC-007", "/img/autos/sport.svg",
            "ABC-008", "/img/autos/minivan.svg");

    /** Las primeras versiones del seed usaban fotos al azar de picsum.photos (no eran carros). */
    private static final String IMAGEN_ANTIGUA = "https://picsum.photos/";

    private final DynamoProperties props;
    private final TiendaRepository tiendas;
    private final CategoriaRepository categorias;
    private final AutoRepository autos;
    private final ClienteRepository clientes;
    private final AlquilerRepository alquileres;

    public void cargarSiHaceFalta() {
        if (!props.isSeed() || !tiendas.estaVacia()) {
            return;
        }

        // Cada guardado devuelve el id REAL asignado por IdGenerator; las demas entidades se enlazan
        // con esos ids (no se asume que empiecen en 1: asi la carga sirve aunque los contadores no esten en cero).
        long[] t = {
                tienda("AutoRent Norte", "Bogotá", "Av. 19 #120-45"),
                tienda("AutoRent Centro", "Medellín", "Calle 50 #40-12"),
                tienda("AutoRent Sur", "Cali", "Carrera 5 #15-30"),
                tienda("AutoRent Costa", "Barranquilla", "Calle 72 #46-20"),
                tienda("AutoRent Oriente", "Bucaramanga", "Carrera 27 #51-10"),
                tienda("AutoRent Eje", "Manizales", "Av. Santander #23-60"),
                tienda("AutoRent Caribe", "Cartagena", "Bocagrande Calle 4 #3-12"),
                tienda("AutoRent Llanos", "Villavicencio", "Calle 40 #32-15")};

        long[] c = {
                categoria("Económico", "Vehículos de bajo consumo y costo accesible"),
                categoria("Sedán", "Vehículos de cuatro puertas para uso familiar"),
                categoria("SUV", "Vehículos utilitarios deportivos de mayor capacidad"),
                categoria("Camioneta", "Vehículos de carga y doble tracción"),
                categoria("Lujo", "Vehículos de alta gama con equipamiento premium"),
                categoria("Eléctrico", "Vehículos de cero emisiones con motor eléctrico"),
                categoria("Deportivo", "Vehículos de alto desempeño y diseño aerodinámico"),
                categoria("Minivan", "Vehículos de gran capacidad para grupos o familias")};

        long[] a = {
                auto(true, t[0], c[0], "Spark", "Chevrolet", "2021", "ABC-001", "80000", null),
                auto(true, t[1], c[1], "Corolla", "Toyota", "2022", "ABC-002", "120000", "10"),
                auto(false, t[2], c[2], "CX-5", "Mazda", "2023", "ABC-003", "180000", null),
                auto(true, t[3], c[3], "Ranger", "Ford", "2022", "ABC-004", "200000", "15"),
                auto(true, t[4], c[4], "Serie 5", "BMW", "2023", "ABC-005", "350000", null),
                auto(false, t[5], c[5], "Ioniq 6", "Hyundai", "2024", "ABC-006", "220000", "5"),
                auto(true, t[6], c[6], "Mustang", "Ford", "2023", "ABC-007", "300000", null),
                auto(true, t[7], c[7], "Carnival", "Kia", "2022", "ABC-008", "250000", "20")};

        long[] k = {
                cliente("Carlos Ramírez", "carlos.ramirez@email.com", "3101234567", "4111111111111111"),
                cliente("Laura Gómez", "laura.gomez@email.com", "3209876543", "4222222222222222"),
                cliente("Andrés Torres", "andres.torres@email.com", "3154567890", "4333333333333333"),
                cliente("María Pérez", "maria.perez@email.com", "3001112233", "4444444444444444"),
                cliente("Juan Martínez", "juan.martinez@email.com", "3183334455", "4555555555555555"),
                cliente("Sofía Herrera", "sofia.herrera@email.com", "3126667788", "4666666666666666"),
                cliente("Diego Castillo", "diego.castillo@email.com", "3049998877", "4777777777777777"),
                cliente("Valentina Ruiz", "valentina.ruiz@email.com", "3172221133", "4888888888888888")};

        LocalDate hoy = LocalDate.now();
        // 6 ya devueltos (CERRADO, en el pasado)
        alquiler(k[0], a[0], hoy.minusDays(60), hoy.minusDays(56), "320000", "Bogotá", "Villavicencio", "CERRADO");
        alquiler(k[1], a[1], hoy.minusDays(45), hoy.minusDays(41), "432000", "Medellín", "Cali", "CERRADO");
        alquiler(k[2], a[3], hoy.minusDays(30), hoy.minusDays(28), "400000", "Cali", "Bucaramanga", "CERRADO");
        alquiler(k[3], a[4], hoy.minusDays(20), hoy.minusDays(15), "875000", "Barranquilla", "Bucaramanga", "CERRADO");
        alquiler(k[6], a[6], hoy.minusDays(12), hoy.minusDays(10), "600000", "Cartagena", "Medellín", "CERRADO");
        alquiler(k[7], a[7], hoy.minusDays(9), hoy.minusDays(7), "400000", "Villavicencio", "Barranquilla", "CERRADO");
        // 2 activos: sus autos (CX-5 y Ioniq 6) estan no disponibles
        alquiler(k[4], a[2], hoy.minusDays(2), hoy.plusDays(2), "720000", "Bucaramanga", "Cartagena", "ACTIVO");
        alquiler(k[5], a[5], hoy.minusDays(1), hoy.plusDays(3), "836000", "Manizales", "Bogotá", "ACTIVO");

        log.info("Datos de ejemplo cargados: 8 tiendas, 8 categorias, 8 autos, 8 clientes, 8 alquileres");
    }

    /**
     * Migracion de datos (idempotente): si la base ya tenia el seed viejo con fotos de picsum.photos, cambia SOLO
     * la imagen de esos autos por su ilustracion. Un auto con otra imagen (la que puso el usuario) no se toca,
     * y la segunda vez que corre no encuentra nada que cambiar.
     */
    public void corregirImagenesDeEjemplo() {
        if (!props.isSeed()) {
            return;
        }
        int corregidos = 0;
        for (Auto auto : autos.findAll()) {
            Detalle_auto detalle = auto.getDetalles();
            if (detalle == null || detalle.getImagen() == null || !detalle.getImagen().startsWith(IMAGEN_ANTIGUA)) {
                continue;
            }
            String nueva = IMAGEN_POR_PLACA.get(detalle.getPlaca());
            if (nueva == null) {
                continue;
            }
            // el detalle completo, con la imagen nueva: el update parcial solo reescribe la ficha de ESTE auto
            Detalle_auto actualizado = new Detalle_auto(nueva, detalle.getModelo(), detalle.getMarca(),
                    detalle.getAnio(), detalle.getPlaca(), detalle.getPrecioDia(), detalle.getOfertaPorcentaje());
            autos.actualizarParcial(Auto.builder().idAuto(auto.getIdAuto()).detalles(actualizado).build());
            corregidos++;
        }
        if (corregidos > 0) {
            log.info("Imagenes de ejemplo actualizadas a ilustraciones de carros: {} autos", corregidos);
        }
    }

    private long tienda(String nombre, String ciudad, String direccion) {
        Tienda t = new Tienda();
        t.setNombre(nombre);
        t.setCiudad(ciudad);
        t.setDireccion(direccion);
        return tiendas.save(t).getIdTienda();
    }

    private long categoria(String nombre, String descripcion) {
        return categorias.save(Categoria.builder().nombre(nombre).descripcion(descripcion).build()).getIdCategoria();
    }

    private long auto(boolean disponible, long idTienda, long idCategoria, String modelo, String marca,
                      String anio, String placa, String precioDia, String oferta) {
        Detalle_auto detalle = new Detalle_auto(
                IMAGEN_POR_PLACA.get(placa), modelo, marca, anio, placa,
                new BigDecimal(precioDia), oferta == null ? null : new BigDecimal(oferta));
        return autos.save(Auto.builder()
                .disponibilidad(disponible).idTienda(idTienda).idCategoria(idCategoria).detalles(detalle).build())
                .getIdAuto();
    }

    private long cliente(String nombre, String email, String telefono, String tarjeta) {
        return clientes.save(Cliente.builder()
                .nombre(nombre).email(email).telefono(telefono).tarjetaCredito(tarjeta).build()).getIdCliente();
    }

    private void alquiler(long idCliente, long idAuto, LocalDate inicio, LocalDate fin, String total,
                          String retirada, String devolucion, String estado) {
        alquileres.save(Alquiler.builder()
                .idCliente(idCliente).idAuto(idAuto).fechaInicio(inicio).fechaFin(fin)
                .precioTotal(new BigDecimal(total)).ciudadRetirada(retirada).ciudadDevolucion(devolucion)
                .estado(estado).build());
    }
}
