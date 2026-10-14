# Guía: del SQL a DynamoDB (NoSQL)

El proyecto pasó de **PostgreSQL + JPA/Hibernate** a **Amazon DynamoDB**. La API (rutas y JSON) quedó **igual**,
así que el frontend y los controllers no cambiaron. Esta guía explica **qué cambió y por qué**, cómo correrlo
**en local** y cómo **desplegarlo** con la base en AWS.

---

## 1. La idea: pensar en consultas, no en tablas

| En SQL (antes) | En DynamoDB (ahora) |
|---|---|
| Diseñas los datos (normalizados) y luego escribes consultas con `JOIN` | **Partes de las consultas** que necesita tu API y diseñas los datos para responderlas rápido |
| `BIGSERIAL` genera el id | No hay: tabla `contadores` con una suma **atómica** (`ADD valor 1`) |
| `JOIN autos + detalles_autos` | **Desnormalizar**: el detalle va *dentro* del documento del auto |
| `UNIQUE` (email, placa, nombre) | No hay: un ítem **"candado"** en la tabla `unicos` (`attribute_not_exists`) |
| `FOREIGN KEY` | No hay: se valida en el servicio (ya se hacía en los borrados) |
| `WHERE id_cliente = ?` | **Índice secundario global (GSI)** sobre `idCliente` |
| `@Transactional` + rollback | `TransactWriteItems`: varias escrituras **todo o nada**, con condiciones |
| `ILIKE` / ignorar mayúsculas | Guardar una **copia en minúsculas** y consultar esa |
| `SELECT *` ordenado por llave | `Scan` **sin orden garantizado** (se ordena en la aplicación) |

### Tablas e índices

| Tabla (`rentacars_…`) | Llave | Índices (GSI) | Responde a |
|---|---|---|---|
| `tiendas` | `idTienda` | `ciudad-index` | HU-03 tiendas por ciudad |
| `categorias` | `idCategoria` | — | HU-06/07 |
| `autos` | `idAuto` | `idTienda-index`, `idCategoria-index` | HU-09 búsqueda; "¿esta tienda tiene autos?" |
| `clientes` | `idCliente` | `email-index` | HU-14 email repetido |
| `alquileres` | `idAlquiler` | `idCliente-index`, `idAuto-index`, `estado-fechaFin-index` | HU-20 historial; HU-21 activos (`ACTIVO` y `fechaFin >= hoy`) |
| `contadores` | `nombre` | — | ids autoincrementales |
| `unicos` | `clave` | — | email, placa y nombre de categoría únicos |

El **detalle del auto** (marca, modelo, año, placa, precio, oferta, imagen) vive anidado en `autos.detalles`:
una sola lectura trae el auto completo y crearlo es una sola escritura.

### Dónde mirar en el código
- `model/` — los `@DynamoDbBean` (con comentarios de qué reemplaza a qué).
- `repository/DynamoRepository.java` — la base que reemplaza a `JpaRepository`.
- `repository/AlquilerRepository.java` — las **transacciones con condición** (crear alquiler + ocupar auto sin que dos personas ganen el mismo auto).
- `repository/IdGenerator.java` y `UnicoRepository.java` — ids y candados.
- `config/` — conexión, creación de tablas al arrancar y datos de ejemplo.

---

## 2. Correrlo en local (sin cuenta de AWS, sin Docker)

Doble clic en **`iniciar-local.bat`**. Hace tres cosas:
1. Arranca **DynamoDB Local** en el puerto 8000 (la misma base DynamoDB, en tu PC; datos en `.dynamodb-local/`).
2. Arranca el **backend** con el perfil `local`: crea las 7 tablas y carga datos de ejemplo.
3. Abre **<http://localhost:8080>**: el backend sirve también el frontend (un solo puerto, **sin CORS**).

Swagger: <http://localhost:8080/swagger-ui.html>. Para empezar de cero, detén todo y borra la carpeta `.dynamodb-local`.

**Con Docker** (alternativa): `./mvnw clean package -DskipTests` y luego `docker compose up --build`
→ backend <http://localhost:8080>, frontend (nginx) <http://localhost:8081>.

**Con comandos** (si prefieres ver cada parte):
```powershell
# ventana 1: DynamoDB Local
.\mvnw -q test-compile exec:java "-Dexec.classpathScope=test" "-Dexec.mainClass=com.rentacars.LocalDynamoDb"
# ventana 2: backend
.\mvnw spring-boot:run "-Dspring-boot.run.profiles=local"
```
(El proyecto necesita **Java 25 o superior**; los accesos directos lo buscan solos.)

---

## 3. Desplegarlo con la base en AWS (DynamoDB real)

Las tablas viven en tu cuenta de AWS; la app corre en tu PC y se conecta a ellas. DynamoDB se factura **por uso**
(modo on-demand): con unas pocas peticiones cuesta centavos o nada, y no hay nada "encendido" que apagar.

### Paso 1 — Permisos (IAM)
La app necesita credenciales de AWS. En lugar de usar tu usuario principal, crea uno **solo para esto**:

1. Consola AWS → **IAM → Policies → Create policy → JSON**, pega y guarda con el nombre `rentacars-dynamodb`:
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [{
       "Effect": "Allow",
       "Action": ["dynamodb:CreateTable", "dynamodb:DescribeTable", "dynamodb:GetItem", "dynamodb:PutItem",
                  "dynamodb:UpdateItem", "dynamodb:DeleteItem", "dynamodb:Query", "dynamodb:Scan",
                  "dynamodb:ConditionCheckItem"],
       "Resource": ["arn:aws:dynamodb:us-east-1:*:table/rentacars_*",
                    "arn:aws:dynamodb:us-east-1:*:table/rentacars_*/index/*"]
     }]
   }
   ```
2. **IAM → Users → Create user** (`rentacars-app`, sin acceso a la consola) → *Attach policies directly* → `rentacars-dynamodb`.
3. Entra al usuario → **Security credentials → Create access key → Application running outside AWS**.
   Copia el **Access key ID** y el **Secret access key** (el secreto solo se muestra una vez).

> La política solo permite tocar tablas que empiecen con `rentacars_` en `us-east-1`. Si cambias de región o de prefijo, ajústala.

### Paso 2 — Credenciales en tu PC
```powershell
Copy-Item .env.example .env
notepad .env        # completa AWS_ACCESS_KEY_ID y AWS_SECRET_ACCESS_KEY
```
`.env` está en `.gitignore`: **nunca se sube a GitHub**. No pegues las claves en el chat ni en el código.

### Paso 3 — Arrancar contra AWS
Doble clic en **`iniciar-aws.bat`** (o `docker compose -f docker-compose.cloud.yml up --build`).
Al arrancar crea las tablas `rentacars_*` en tu cuenta y carga los datos de ejemplo. Abre <http://localhost:8080>.

### Paso 4 — Comprobar que los datos están en la nube
Consola AWS → **DynamoDB → Tables → `rentacars_clientes` → Explore table items**: ahí aparece lo que creas en el
frontend (crea un cliente y recarga). Esa es la evidencia de que frontend y backend locales escriben en AWS.

### Al terminar
Para no dejar nada en la cuenta: DynamoDB → Tables → selecciona las `rentacars_*` → **Delete**
(y en IAM, desactiva o borra la access key).

---

## 4. Pruebas
`.\mvnw test` corre todo, **sin AWS ni Docker**: las pruebas de integración levantan un DynamoDB Local embebido y
verifican ids, candados de unicidad, índices, transacciones y hasta una **carrera de dos alquileres simultáneos**
sobre el mismo auto (solo uno gana).

## 5. Problemas frecuentes

| Síntoma | Causa y solución |
|---|---|
| `Unable to load credentials` / 503 "No se pudo conectar con la base de datos" | Faltan las credenciales de AWS (paso 2) o no hay internet. |
| `AccessDeniedException` al arrancar | La política de IAM no incluye la región o el prefijo de tus tablas (paso 1). |
| Con `iniciar-local.bat`: "DynamoDB Local no arrancó" | El puerto 8000 está ocupado, o falta Java 25+. Mira la otra ventana. |
| Datos de ejemplo no aparecen | Solo se cargan si la tabla de tiendas está **vacía**. En local, borra `.dynamodb-local`. |
| Una lista aparece vacía justo después de crear algo (solo en AWS real) | Los índices (GSI) son **eventualmente consistentes**: pueden tardar milisegundos en reflejar el cambio. Recarga. |

## 6. Limitaciones conocidas (para ser honestos con el profe)
- `findAll` usa **Scan** (lee toda la tabla). Para un proyecto de este tamaño está bien; con miles de registros se paginaría o se diseñaría un índice.
- Los candados de unicidad y los contadores son patrones estándar de DynamoDB, pero hay que **mantenerlos** (por eso los repositorios los tocan al crear, renombrar y borrar).
- Una `Detalle_auto` ya no tiene id propio: no se puede consultar por separado, solo como parte del auto.
