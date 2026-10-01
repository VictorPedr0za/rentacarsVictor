# Backend completado según Backlog Sprint v2

Se completaron/corrigieron las 21 historias del PDF manteniendo la arquitectura de un solo servicio Spring Boot por capas.

## Endpoints finales

| HU | Método | Endpoint | Estado |
|---|---|---|---|
| HU-01 | POST | `/tiendas` | Completa |
| HU-02 | PUT | `/tiendas/{id}` | Completa |
| HU-03 | GET | `/tiendas?ciudad=...` | Completa |
| HU-04 | DELETE | `/tiendas/{id}` | Completa |
| HU-05 | GET | `/tiendas/{id}` | Completa |
| HU-06 | POST | `/categorias` | Completa |
| HU-07 | GET | `/categorias` | Completa |
| HU-08 | POST | `/autos` | Completa |
| HU-09 | GET | `/autos?ciudad=...&id_categoria=...` | Completa |
| HU-10 | PUT | `/autos/{id}` | Completa |
| HU-11 | PATCH | `/autos/{id}/disponibilidad` | Completa |
| HU-12 | GET | `/autos/{id}` | Completa |
| HU-13 | DELETE | `/autos/{id}` | Completa |
| HU-14 | POST | `/clientes` | Completa |
| HU-15 | PUT | `/clientes/{id}` | Completa |
| HU-16 | GET | `/clientes` | Completa |
| HU-18 | POST | `/alquileres` | Completa |
| HU-20 | GET | `/alquileres?id_cliente=...` | Completa |
| HU-21 | GET | `/alquileres/activos` | Completa |
| HU-22 | DELETE | `/alquileres/{id}` | Completa |
| HU-24 | PUT | `/alquileres/{id}/devolucion` | Completa |

## Correcciones principales

- HU-08 ahora registra el auto en `autos` y su ficha en `detalles_autos` dentro de una transacción, valida tienda y categoría y fuerza `disponibilidad=true`.
- HU-09 devuelve solo autos disponibles y soporta los filtros opcionales de ciudad/categoría.
- HU-10, HU-11, HU-12 y HU-13 respetan las rutas y reglas del backlog.
- HU-15 y HU-16, que estaban pendientes, quedaron implementadas; el email no se modifica y las tarjetas se enmascaran al listar.
- HU-18 calcula el precio con `BigDecimal`, valida cliente/auto y cambia la disponibilidad.
- HU-20, HU-21, HU-22 y HU-24 quedaron alineadas con el flujo de alquileres del PDF.
- Los errores 400/404 siguen centralizados en `GlobalExceptionHandler`.
- Se mantuvo `ddl-auto=validate`, el esquema v2 con `BIGSERIAL` y `alquileres.estado`.

## Dockerización

No se modificaron `Dockerfile`, `docker-compose.yaml` ni `.dockerignore`.

**Importante:** el Dockerfile actual copia el JAR de `target/`. Por eso, después de cambiar el código hay que reconstruir el JAR antes de reconstruir la imagen:

### Windows PowerShell

```powershell
.\mvnw.cmd clean package -DskipTests
docker compose build --no-cache app
docker compose up -d
```

Si tu `docker-compose.yaml` ya apunta a la base PostgreSQL que vienes usando, no necesitas cambiar esa conexión.

## Base de datos

El archivo `script_bd.sql` es el esquema v2 esperado por las entidades. Si tu base todavía tiene el esquema viejo, respalda los datos necesarios y vuelve a ejecutar el script antes de arrancar el backend, porque usa `DROP TABLE ... CASCADE`.

## Prueba rápida

Con el backend arriba, abre:

`http://localhost:8081/swagger-ui.html` si accedes mediante el puerto publicado por Docker Compose (`8081:8080`).

Si ejecutas Spring Boot directamente fuera de Docker, usa:

`http://localhost:8080/swagger-ui.html`
