# Revisión del microservicio rentacars (Spring Boot)

Análisis del código en `src/main/java/com/rentacars`, `pom.xml` y `application.properties`.

## Aspectos positivos

- **Arquitectura en capas clara y consistente**: separación limpia entre `controller`, `service` (interfaz + `impl`), `repository`, `model`, `mapper` y `dto` (`request`/`response`), aplicada de forma uniforme en las seis entidades del dominio (Alquiler, Auto, Cliente, Categoria, Tienda, Detalle_auto).
- **DTOs de entrada/salida separados de las entidades**: evita exponer directamente las `@Entity` en la API y permite validar solo lo que llega del cliente.
- **Manejo de errores centralizado**: un único `@RestControllerAdvice` (`GlobalExceptionHandler`) traduce `ResourceNotFoundException` → 404 y `BadRequestException` → 400, con un `ErrorResponse` consistente en toda la API. Evita duplicar `try/catch` con status codes en cada controller.
- **Validación declarativa con Bean Validation**: uso correcto de `@Valid`, `@NotNull`, `@NotBlank`, `@Size` en los DTOs, y captura de `MethodArgumentNotValidException` para devolver mensajes de campo legibles.
- **Transaccionalidad donde importa**: operaciones que combinan más de una escritura (`registrarDevolucion`, `deleteAlquiler` que además libera el auto) están anotadas con `@Transactional`, evitando estados inconsistentes.
- **Buenas decisiones de configuración**:
  - `ddl-auto=validate` en vez de `update`/`create`, forzando que el script SQL sea la fuente de verdad del esquema (evita drift silencioso entre entidades y BD).
  - `spring.jackson.property-naming-strategy=SNAKE_CASE` centraliza la conversión camelCase↔snake_case sin `@JsonProperty` repetido.
  - `open-in-view=false`, evitando el anti-patrón de lazy-loading fuera de la capa de servicio.
- **Documentación de API con springdoc/OpenAPI** (`@Tag`, `@Operation`) y Swagger UI habilitado, útil para consumo por frontend/QA.
- **Comentarios explicativos con intención pedagógica**: los `application.properties` y algunas clases explican el *por qué* de decisiones (ej. por qué `ddl-auto=validate`, por qué no usar `@JsonProperty`), lo cual es valioso en un proyecto académico/de equipo.
- **Reglas de negocio protegidas en el service, no solo en la BD**: por ejemplo, no cancelar un alquiler ya iniciado (`deleteAlquiler`), a pesar de que la constraint también existe a nivel de base de datos — da mensajes de error entendibles en vez de errores crudos de PostgreSQL.

## Oportunidades de mejora

### Manejo de excepciones
- **Uso de `Exception` genérica en `AlquilerServiceImpl`** (`createAlquiler`, `updateAlquiler`): se lanzan `new Exception("...")` checked y se propagan con `throws Exception` hasta el controller. Esto rompe la consistencia lograda por el `GlobalExceptionHandler` (esas excepciones no son mapeadas a 400/404 y probablemente terminan en un 500 genérico). Deberían reemplazarse por `BadRequestException`/`ResourceNotFoundException`, tal como ya se hace correctamente en `registrarDevolucion` y `deleteAlquiler`.
- **`RuntimeException` cruda en varios `findById(...).orElseThrow(...)`** (ej. `getAlquilerById`, `updateAlquiler`) en lugar de `ResourceNotFoundException`. Da como resultado un 500 en vez de un 404 cuando el id no existe.
- Los bloques `try { ... } catch (Exception e) { throw e; }` en `createAlquiler`/`updateAlquiler` no aportan nada (capturan y relanzan igual): se pueden eliminar.

### Validación duplicada e inconsistente
- Las validaciones manuales dentro de `createAlquiler` (nulls, rangos, fechas) duplican lo que ya deberían cubrir las anotaciones `@NotNull`/`@NotBlank`/`@Size` del DTO `CreateAlquilerRequest`. Convendría dejar Bean Validation como única fuente de validación de forma/presencia, y reservar el service solo para reglas de negocio reales (ej. `fechaFin` posterior a `fechaInicio`, que sí es una regla de dominio y no de forma).
- `CreateAlquilerRequest.estado` está marcado `@NotBlank` y documentado como requerido, pero `AlquilerServiceImpl.createAlquiler` ignora ese valor y siempre fuerza `estado("ACTIVO")`. El campo del DTO es engañoso: o se elimina del request (el estado inicial no debería decidirlo el cliente) o se usa realmente.

### Endpoints y diseño REST
- Verbos en las rutas (`/alquileres/create`, `/alquileres/update/{id}`) en vez de usar los métodos HTTP de forma idiomática (`POST /alquileres`, `PUT /alquileres/{id}`), como sí se hace en `ClienteController` (`POST /clientes`). Falta consistencia entre controllers.
- `getAlquilerById` y `createAlquiler`/`updateAlquiler` devuelven `HttpStatus.CREATED` (201) también para el `GET` por id, donde lo correcto es `200 OK`.
- Endpoint `GET /alquileres/ping` parece un endpoint de diagnóstico dejado en el controller de producción; convendría moverlo a un `HealthController` aparte o eliminarlo si ya no se usa (Spring Boot Actuator cubriría esto mejor).
- Código muerto comentado (bloques `/* ... */` de versiones previas de `deleteAlquiler` en controller y service) debería eliminarse; el historial de git ya preserva esas versiones.

### Cobertura de pruebas
- El único test existente es el `contextLoads()` por defecto generado por Spring Initializr. No hay pruebas unitarias de `service` (reglas de negocio como "no cancelar un alquiler ya iniciado", cálculo de disponibilidad, etc.) ni pruebas de integración de `controller`/`repository`, a pesar de que el `pom.xml` ya incluye `spring-boot-starter-data-jpa-test` y `spring-boot-starter-webmvc-test`. Es el punto de mayor riesgo del proyecto: no hay red de seguridad ante regresiones.

### Seguridad y configuración
- Credenciales de base de datos (`cloud_usr` / `cloud_pwd`) están hardcodeadas en `application.properties` y versionadas en el repo. Aunque el comentario indica "no subir tu cambio local", el valor por defecto igual queda expuesto en git; conviene moverlas a variables de entorno (`${DB_USER}`, `${DB_PASSWORD}`) con un `.env`/`application-local.properties` ignorado por git.
- No hay ninguna dependencia de seguridad (`spring-boot-starter-security`) ni autenticación/autorización en ningún endpoint: toda la API (crear, actualizar, cancelar alquileres, clientes, etc.) está completamente abierta. Aceptable en una fase temprana académica, pero es una brecha importante antes de cualquier despliegue real.
- No hay `application-prod.properties`/perfiles (`spring.profiles.active`) para separar configuración de desarrollo y producción (por ejemplo, `show-sql=true` no debería ir a producción).

### Consistencia de código
- Mezcla de anotaciones Lombok redundantes en los modelos: `@Getter @Setter @Data` juntos en `Alquiler` (`@Data` ya genera getters/setters/equals/hashCode/toString), lo cual es ruido innecesario.
- Mezcla de `@AllArgsConstructor` (en controllers/services) con `@RequiredArgsConstructor` (en `ClienteController`) para inyección de dependencias; conviene unificar en `@RequiredArgsConstructor`, que es más explícito sobre qué campos son requeridos.
- Nombre de paquete/clase con guion bajo (`Detalle_auto`, `Detalle_autoService`, etc.) no sigue la convención de Java (`DetalleAuto`), inconsistente con el resto de clases (`Alquiler`, `Cliente`, `Tienda`).
- `pom.xml` tiene metadatos vacíos (`<name/>`, `<description/>`, `<url/>`, `<license/>`, `<developer/>`) que deberían completarse o eliminarse.

## Resumen de prioridad sugerida

1. **Alto impacto / bajo esfuerzo**: reemplazar `Exception`/`RuntimeException` genéricas por `BadRequestException`/`ResourceNotFoundException` en `AlquilerServiceImpl` para que el `GlobalExceptionHandler` funcione de forma consistente en toda la API.
2. **Alto impacto**: agregar pruebas unitarias a la capa `service` (reglas de negocio) y pruebas de integración a los `controller` más críticos (`Alquiler`, `Auto`).
3. **Medio impacto**: sacar credenciales de `application.properties` a variables de entorno.
4. **Medio impacto**: limpiar validación duplicada entre DTOs y services, y resolver la inconsistencia del campo `estado` en `CreateAlquilerRequest`.
5. **Bajo impacto (cleanup)**: unificar convenciones REST, eliminar código comentado, unificar Lombok/constructor injection.
