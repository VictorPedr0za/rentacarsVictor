# 🐳 Dockerizar un Microservicio Spring Boot + Maven

> **Electiva: Computación en la Nube**  
> Stack: Java 25 · Spring Boot · Maven · Amazon Corretto 25 · Docker

---

## Prerrequisitos

- Docker Desktop instalado y en ejecución
- JDK 25 instalado localmente
- Maven configurado (o usar el wrapper `mvnw` incluido en el proyecto)

---

## Paso 1 — Construir el JAR con Maven

Desde la **raíz del proyecto**, ejecuta:

```bash
./mvnw clean package -DskipTests
```

> En Windows usa `mvnw.cmd clean package -DskipTests`

El flag `-DskipTests` omite las pruebas para agilizar el build.

El JAR resultante quedará en:

```
target/nombre-del-proyecto-0.0.1-SNAPSHOT.jar
```

Verifica el nombre exacto del JAR revisando `target/` o consultando `<artifactId>` y `<version>` en tu `pom.xml`.

---

## Paso 2 — Crear el `Dockerfile`

En la **raíz del proyecto** (al mismo nivel que `pom.xml`), crea un archivo llamado `Dockerfile` (sin extensión):

```dockerfile
# Imagen base: Amazon Corretto 25 sobre Alpine Linux (imagen ligera)
FROM amazoncorretto:25-alpine

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el JAR generado por Maven al contenedor
# Ajusta el nombre según tu pom.xml (artifactId + version)
COPY target/nombre-del-proyecto-0.0.1-SNAPSHOT.jar app.jar

# Puerto que expone la aplicación (ajusta si usas otro)
EXPOSE 8080

# Comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Paso 3 — Crear el `.dockerignore`

En la **raíz del proyecto**, crea un archivo `.dockerignore` para excluir archivos innecesarios del contexto de build:

```
.git
.gitignore
.mvn
*.md
target/*.original
target/classes
target/generated-sources
target/maven-archiver
```

Esto reduce el tamaño del contexto y acelera el proceso de construcción de la imagen.

---

## Paso 4 — Construir la imagen Docker

Con Docker Desktop corriendo, ejecuta desde la raíz del proyecto:

```bash
docker build -t nombre-microservicio:1.0 .
```

| Parte | Descripción |
|---|---|
| `-t nombre-microservicio:1.0` | Nombre y tag de la imagen |
| `.` | Contexto de build: directorio actual |

Verifica que la imagen se creó correctamente:

```bash
docker images
```

---

## Paso 5 — Ejecutar el contenedor

```bash
docker run -d -p 8080:8080 --name mi-microservicio nombre-microservicio:1.0
```

| Flag | Descripción |
|---|---|
| `-d` | Modo detached (corre en segundo plano) |
| `-p 8080:8080` | Mapea puerto `host:contenedor` |
| `--name mi-microservicio` | Nombre amigable para el contenedor |

Verifica que el contenedor esté corriendo:

```bash
docker ps
```

Prueba la aplicación desde el navegador o Postman en:

```
http://localhost:8080
```

---

## Comandos útiles de gestión

```bash
# Ver logs del contenedor
docker logs mi-microservicio

# Detener el contenedor
docker stop mi-microservicio

# Eliminar el contenedor
docker rm mi-microservicio

# Eliminar la imagen
docker rmi nombre-microservicio:1.0
```

---

## Estructura final del proyecto

```
📁 tu-proyecto/
├── 📄 Dockerfile                   ← nuevo
├── 📄 .dockerignore                ← nuevo
├── 📄 pom.xml
├── 📁 src/
└── 📁 target/
    └── 📄 nombre-proyecto-0.0.1-SNAPSHOT.jar
```

---

## Flujo resumido

```
Código fuente
     │
     ▼
mvn clean package
     │
     ▼
  .jar en target/
     │
     ▼
docker build → Imagen (amazoncorretto:25-alpine)
     │
     ▼
docker run → Contenedor en ejecución
     │
     ▼
http://localhost:8080
```

---

## Próximos pasos sugeridos

| Tema | Descripción |
|---|---|
| **Docker Compose** | Orquestar múltiples servicios (app + base de datos) |
| **Multi-stage build** | Reducir el tamaño final de la imagen |
| **Docker Hub / ECR** | Publicar la imagen en un registry en la nube |
| **Variables de entorno** | Configurar perfiles de Spring Boot por entorno |
