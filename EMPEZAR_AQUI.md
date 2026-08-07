# EMPEZAR AQUÍ

Guía para los 7 del equipo. Léela completa una vez (10 minutos) antes de escribir código.

El **Día 0 ya está hecho**: la configuración, las 6 entidades, el manejo de errores y la **HU-01 completa como ejemplo**. Tu trabajo es copiar ese ejemplo para tu HU.

---

## 1. Glosario: los términos que aparecen en el backlog

Sin esto el PDF no se entiende. Son los 12 términos que vas a ver todo el tiempo.

| Término | Qué es, en cristiano |
|---|---|
| **Endpoint** | Una dirección de tu API que hace una cosa. `POST /tiendas` es un endpoint: crear tienda. |
| **DTO** | *Data Transfer Object*. Una clase que solo sirve para representar el JSON que entra o sale. No se guarda en la base de datos. |
| **Entidad / Model** | Una clase que SÍ representa una tabla de la base de datos. Lleva `@Entity`. |
| **Repository** | La clase que habla con la base de datos. Tú no escribes SQL: heredas de `JpaRepository` y ya tienes `save()`, `findById()`, `findAll()`, `deleteById()`. |
| **Service** | Donde vive la lógica: validaciones, cálculos, reglas de negocio. Es el cerebro. |
| **Controller** | La puerta de entrada. Recibe la petición HTTP y se la pasa al service. No piensa. |
| **Interfaz** | Un contrato: dice *qué* métodos existen, sin decir *cómo* funcionan. `TiendaService` es la interfaz; `TiendaServiceImpl` es el cómo. |
| **Inyección de dependencias** | En vez de hacer `new TiendaRepository()`, tú declaras el campo como `private final` y **Spring te lo entrega ya creado**. Eso hace `@RequiredArgsConstructor`. |
| **JPA / Hibernate** | La herramienta que traduce entre tus clases Java y las tablas de PostgreSQL. |
| **Lombok** | Librería que escribe código repetitivo por ti. `@Getter` te ahorra escribir 20 getters a mano. |
| **`@Transactional`** | "Todo o nada". Si el método falla a la mitad, la base de datos se revierte sola. |
| **Excepción** | Una forma de decir "algo salió mal" y cortar la ejecución. Nosotros lanzamos `ResourceNotFoundException` y Spring devuelve un 404 solo. |

**Códigos HTTP que vas a usar:**

- `200 OK` — consulta exitosa
- `201 Created` — creaste algo nuevo (POST)
- `204 No Content` — borraste algo (DELETE), sin cuerpo de respuesta
- `400 Bad Request` — el cliente mandó algo mal (falta un campo, rompió una regla)
- `404 Not Found` — el recurso no existe

---

## 2. Arrancar el proyecto (haz esto primero, hoy)

**Paso 1 — Crear el usuario y la base.** Conectado como `postgres` (superusuario), ejecuta una sola vez:

```sql
create user cloud_usr with encrypted password 'cloud_pwd';
create database alquilerautos_db with owner cloud_usr;
```

**Paso 2 — Correr el script.** Conéctate a la base `alquiler_vehiculos` y ejecuta el archivo **`script_bd.sql`** que está en la raíz del proyecto. Crea las 6 tablas y mete 8 filas de prueba en cada una.

> Este script es la **versión 2**. Cambió dos cosas respecto al que ya tenían: los ids pasaron de `SERIAL` a `BIGSERIAL` (para que calcen con `Long` en Java) y se agregó la columna `alquileres.estado`, que HU-24 y HU-21 necesitan. El script arranca con `DROP TABLE`, así que se puede correr encima del anterior sin problema.

**Paso 3 — Arrancar.** En IntelliJ, botón verde ▶ sobre `RentacarsApplication`. O en terminal:

```bash
./mvnw spring-boot:run
```

**Paso 4 — Verificar que funcionó.** En la consola debe salir `Started RentacarsApplication`.

> El proyecto corre en modo **`validate`**: Hibernate no crea ni modifica tablas, solo revisa al arrancar que cada `@Entity` coincida con su tabla. Si algo no calza, la app no arranca y te dice exactamente qué columna está mal. Eso es a propósito: mejor enterarse al segundo 1 que a mitad de una petición.

**Paso 5 — Probar HU-01.** Abre en el navegador:

```
http://localhost:8080/swagger-ui.html
```

Ahí aparece `POST /tiendas`. Dale "Try it out", manda este JSON y dale Execute:

```json
{ "nombre": "Tienda Norte", "ciudad": "Bogota", "direccion": "Calle 100 #15-20" }
```

Debe responder **201** con el `id_tienda` asignado. Ahora borra el campo `"nombre"` y vuelve a mandarlo: debe responder **400** diciendo *"El nombre es obligatorio"*. Ese 400 salió solo, nadie escribió un `if`.

Si los dos casos funcionan, tienes el proyecto corriendo y entiendes el flujo completo.

---

## 2 bis. Dos cosas de la base de datos que TIENES que saber

Sin esto vas a escribir tu HU mal. Son las dos sorpresas del esquema.

### a) `autos` tiene solo 4 columnas

La marca, el modelo, el año, la placa, el precio y la imagen **no están en `autos`**. Están en `detalles_autos`.

```
autos                        detalles_autos
-----                        --------------
id_auto                      id_detalles_autos
disponibilidad               id_auto  ---> apunta a autos
id_tienda                    marca, modelo, anio, placa
id_categoria                 precio_dia, oferta_porcentaje, imagen
```

A quién le afecta:

- **HU-08 (Cifuentes)** — registrar un auto son **dos inserts**: primero en `autos` para obtener el `id_auto`, luego en `detalles_autos` con ese id. Por eso el método lleva `@Transactional`.
- **HU-09 (Suarez)** — el filtro por ciudad necesita un JOIN `autos → tiendas`, y los datos que devuelves (modelo, precio) salen de `detalles_autos`.
- **HU-12 (Cardona)** — hay que leer de las dos tablas y combinarlas en un solo DTO de respuesta.
- **HU-13 (Cardona)** — borrar primero en `detalles_autos`, luego en `autos`, por la llave foránea.

### b) Los precios son `BigDecimal`, no `double`

`precio_dia`, `precio_total` y `oferta_porcentaje` son `DECIMAL(10,2)`. En Java eso es `BigDecimal`, porque con dinero `double` da errores de redondeo.

La diferencia práctica: **no puedes usar `*`, `-`, `+`**.

| Quieres hacer | Se escribe |
|---|---|
| `a + b` | `a.add(b)` |
| `a - b` | `a.subtract(b)` |
| `a * b` | `a.multiply(b)` |
| `a / b` | `a.divide(b, 2, RoundingMode.HALF_UP)` |
| usar el número 100 | `BigDecimal.valueOf(100)` |
| comparar | `a.compareTo(b) > 0` — **nunca `a > b` ni `equals`** |

Y **`oferta_porcentaje` puede ser `null`** (auto sin descuento). Siempre revísalo antes de calcular:

```java
BigDecimal oferta = detalle.getOfertaPorcentaje();
if (oferta == null) oferta = BigDecimal.ZERO;

// precio_con_oferta = precio_dia - (precio_dia * oferta / 100)
BigDecimal descuento = detalle.getPrecioDia()
        .multiply(oferta)
        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

BigDecimal precioConOferta = detalle.getPrecioDia().subtract(descuento);
```

Ese bloque es exactamente el cálculo de HU-12, y HU-18 lo reusa multiplicando por los días:

```java
long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin);
BigDecimal precioTotal = precioConOferta.multiply(BigDecimal.valueOf(dias));
```

---

## 3. La receta: cómo hacer TU historia de usuario

Todas las HU se hacen igual, en este orden. Abre los archivos de la HU-01 y cópialos.

**El flujo siempre es el mismo:**

```
Controller  ->  Service (interfaz)  ->  ServiceImpl  ->  Repository  ->  PostgreSQL
                                             |
                                          Mapper  <->  Model (entidad)
```

### Paso 1 — Crea tu rama

```bash
git checkout develop
git pull origin develop
git checkout -b feature/tuapellido-HU-XX
```

### Paso 2 — DTO de entrada (si tu HU recibe un JSON)

Copia `dto/request/CreateTiendaRequest.java`. Pon `@NotBlank` en los campos obligatorios (o `@NotNull` si es número/fecha).

> Si tu HU es un GET o un DELETE, no necesitas DTO de entrada. Salta este paso.

### Paso 3 — DTO de salida

Copia `dto/response/CreateTiendaResponse.java`. Solo campos, sin validaciones.

**Escribe los campos en camelCase** (`idTienda`, `precioDia`). El JSON los convierte solo a `id_tienda`, `precio_dia`. No pongas `@JsonProperty`.

### Paso 4 — Mapper

Copia `mapper/TiendaMapper.java`. Solo traduce entidad ↔ DTO. Sin ifs, sin lógica.

### Paso 5 — Método en la interfaz Service

Abre `service/XService.java` y **agrega una sola línea** con la firma de tu método:

```java
CreateTiendaResponse obtenerTienda(Long id);
```

Si el archivo no existe todavía (`AutoService`, `ClienteService`...), créalo copiando `TiendaService.java`.

### Paso 6 — Lógica en el ServiceImpl

Abre `service/impl/XServiceImpl.java` y escribe tu método. **Aquí va todo lo interesante.**

Las dos cosas que más vas a necesitar:

```java
// "Búscalo, y si no existe lanza un 404"
Tienda tienda = tiendaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Tienda no encontrada con id " + id));

// "Si se rompe una regla de negocio, lanza un 400"
if (!auto.getDisponibilidad()) {
    throw new BadRequestException("El auto no está disponible");
}
```

### Paso 7 — Endpoint en el Controller

Abre `controller/XController.java` y agrega tu método. 3 líneas máximo.

```java
@GetMapping("/{id}")
public ResponseEntity<CreateTiendaResponse> obtenerTienda(@PathVariable Long id) {
    return ResponseEntity.ok(tiendaService.obtenerTienda(id));
}
```

Si tu HU recibe un JSON, no olvides **`@Valid`**:

```java
public ResponseEntity<...> crear(@Valid @RequestBody CreateTiendaRequest request) {
```

### Paso 8 — Probar y subir

Arranca la app, prueba en Swagger el caso bueno **y un caso de error**. Si los dos funcionan:

```bash
git add .
git commit -m "HU-XX: registrar tienda"
git push origin feature/tuapellido-HU-XX
```

Abre el Pull Request en GitHub hacia `develop` y pide que alguien lo revise.

---

## 4. Errores que vas a cometer (todos los cometemos)

| Síntoma | Causa | Solución |
|---|---|---|
| `Not a managed type: com.rentacars.model.X` | La clase no tiene `@Entity` | Agrégale `@Entity` y `@Table(name = "...")` |
| Mandas un JSON sin campos obligatorios y responde **200** en vez de 400 | Falta `@Valid` en el controller | `@Valid @RequestBody MiRequest request` |
| `Field X required a bean of type Y that could not be found` | Al `ServiceImpl` le falta `@Service`, o al `Mapper` le falta `@Component` | Ponle la anotación |
| `cannot find symbol: method getNombre()` | Falta `@Getter` de Lombok, o Lombok no está activo en IntelliJ | Agrega `@Getter`; en IntelliJ activa *Enable annotation processing* |
| El JSON sale con `idTienda` en vez de `id_tienda` | Alguien borró la línea `SNAKE_CASE` de `application.properties` | Restáurala |
| `Schema-validation: missing table [tiendas]` | No corriste `script_bd.sql`, o estás conectado a otra base | Corre el script sobre `alquiler_vehiculos` |
| `Schema-validation: wrong column type ... found int4, expected bigint` | Corriste el script viejo (con `SERIAL`) | Corre el `script_bd.sql` nuevo, que usa `BIGSERIAL` |
| `Schema-validation: missing column [estado]` | Script viejo, sin la columna `estado` | Corre el `script_bd.sql` nuevo |
| `bad operand types for binary operator '*'` al calcular precios | Estás usando `*` con `BigDecimal` | `a.multiply(b)` — ver sección 2 bis |
| `NullPointerException` al calcular el descuento | `oferta_porcentaje` viene en `null` | `if (oferta == null) oferta = BigDecimal.ZERO;` |
| `new row violates check constraint` en alquileres | Guardaste un `estado` distinto de `ACTIVO`/`CERRADO`, o `fecha_fin < fecha_inicio` | Revisa el valor antes de guardar |
| `Port 8080 was already in use` | Tienes la app corriendo dos veces | Cierra la otra instancia |
| Conflicto de merge en `AutoController.java` | Tres personas editan ese archivo | Normal. `git pull origin develop`, quédate con **ambos** métodos, no borres el del otro |

---

## 5. Reglas de convivencia en el repositorio

Somos 7 en un solo proyecto. Estas 5 reglas evitan el 90% de los problemas.

1. **Nadie trabaja directo en `develop` ni en `main`.** Siempre una rama `feature/apellido-HU-XX`.
2. **`git pull origin develop` antes de empezar y antes de abrir el PR.** Si no, tu rama queda vieja y el merge duele.
3. **Las entidades ya están hechas y calzan con el script. No las reescribas.** Si necesitas un campo nuevo: modifica `script_bd.sql` → córrelo → ajusta la `@Entity` → avisa al grupo. Nunca al revés, y nunca solo en tu computador.
4. **`GlobalExceptionHandler` y las excepciones ya están hechos. No los dupliques.** Solo lanza `ResourceNotFoundException` o `BadRequestException` desde tu service.
5. **En los archivos compartidos, agrega solo tu método.** `AutoController` lo tocan Cifuentes, Suarez y Cardona; `AlquilerController` lo tocan Corrales, Cardona y Pedroza.

---

## 6. Quién arranca y en qué orden

No todos pueden empezar al mismo tiempo: algunas HU necesitan que otras estén **mergeadas en `develop`** primero.

**Pueden empezar YA (no dependen de nadie):**

| Integrante | HU |
|---|---|
| Arango | HU-02, HU-03 |
| Corrales | HU-04, HU-05 |
| Cifuentes | HU-06, HU-07, HU-08 |
| Suarez | HU-09, HU-10, HU-11 |
| Murcia | HU-14, HU-15, HU-16 |
| Cardona | HU-12, HU-13 |

> HU-01 (Arango) ya está hecha y sirve de ejemplo para todos.

**Tienen que esperar:**

| HU | Espera a que esté en `develop` |
|---|---|
| **HU-18** (Pedroza) | HU-08, HU-11, HU-12, HU-14 |
| **HU-20, HU-21** (Pedroza) | HU-18 |
| **HU-22** (Cardona) | HU-11, HU-18 |
| **HU-24** (Corrales) | HU-11, HU-18 |

Pedroza tiene la HU más pesada del sprint y es la que más espera. Suarez y Cifuentes: **su HU-11 y HU-08 desbloquean a tres personas**, háganlas primero.

---

## 7. Decisiones técnicas que ya se tomaron

Por si el profesor pregunta, o por si alguien se confunde:

- **Un solo proyecto Spring Boot**, no 4 microservicios. Donde el backlog v1 decía "FeignClient", ahora es inyección directa: `AlquilerServiceImpl` inyecta `AutoService` y lo llama como un método normal.
- **Interfaz + implementación en la capa service.** El controller depende de `TiendaService` (la abstracción), no de `TiendaServiceImpl`. Es el principio D de SOLID.
- **Las llaves foráneas son `Long` simples** (`idTienda`, `idCategoria`), no relaciones `@ManyToOne`. Es más simple, coincide con los DTOs del backlog y evita los errores clásicos de JPA (lazy loading, JSON infinito).
- **El script SQL manda, no las entidades** (`ddl-auto=validate`). Hibernate solo verifica que coincidan. Se eligió así porque el script tiene datos de prueba, llaves foráneas y restricciones `CHECK` que Hibernate no generaría solo.
- **Se cambió `SERIAL` por `BIGSERIAL`** en el script. Motivo: en Java los ids son `Long`, que corresponde a `BIGINT`. Con `SERIAL` (que es `INTEGER`) la aplicación no arranca en modo `validate`.
- **Se agregó la columna `alquileres.estado`** (`'ACTIVO'` / `'CERRADO'`). El script original no la tenía y HU-24, HU-21 y HU-22 la necesitan.
- **Precios en `BigDecimal`**, no `double`, porque las columnas son `DECIMAL(10,2)` y con dinero `double` redondea mal.
- **Jackson en `SNAKE_CASE`**: en Java escribimos `camelCase`, el JSON sale en `snake_case` automáticamente.

---

## 8. Si algo no funciona

Antes de preguntar, revisa en este orden:

1. ¿Arranca la app? Lee la **primera** línea roja de la consola, no la última.
2. ¿Está PostgreSQL corriendo y existe la base `rentacars`?
3. ¿Tu rama está actualizada? `git pull origin develop`
4. ¿Buscaste el error en la tabla de la sección 4?


