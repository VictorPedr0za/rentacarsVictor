# RentaCars — Frontend V3

Frontend React/Vite renovado con estilo de marketplace y dos áreas principales:

- **Cliente**: cuenta, catálogo, creación de alquiler y cancelación de reservas futuras.
- **Administrador**: dashboard, clientes, tiendas, autos, alquileres y categorías.

## Endpoints utilizados

La interfaz consume las historias del backlog implementadas en el backend actual:

- Tiendas: `POST /tiendas`, `PUT /tiendas/{id}`, `GET /tiendas`, `GET /tiendas/{id}`, `DELETE /tiendas/{id}`.
- Categorías: `POST /categorias`, `GET /categorias`.
- Autos: `POST /autos`, `GET /autos`, `GET /autos/{id}`, `PUT /autos/{id}`, `PATCH /autos/{id}/disponibilidad`, `DELETE /autos/{id}`.
- Clientes: `POST /clientes`, `PUT /clientes/{id}`, `GET /clientes`.
- Alquileres: `POST /alquileres`, `GET /alquileres?id_cliente=`, `GET /alquileres/activos`, `DELETE /alquileres/{id}`, `PUT /alquileres/{id}/devolucion`.

### Dos límites reales del backend actual

1. No existe `DELETE /clientes/{id}`. El panel no inventa esa operación y muestra una nota explicativa.
2. No existe un `PUT /alquileres/{id}` general. El backlog define como acciones de gestión la cancelación (HU-22) y la devolución (HU-24), y esas son las operaciones que expone el panel.

Además, `GET /autos` devuelve solo autos disponibles. Para la vista administrativa el frontend combina esa respuesta con los autos presentes en alquileres activos, y también permite consultar un auto directamente por ID.

## Ejecutar localmente

Con el backend Docker publicado en `8081`:

```powershell
npm install
npm run dev
```

Abrir `http://localhost:5173`.

El archivo `.env` usa:

```env
VITE_API_URL=http://localhost:8081
```

## Docker

El `Dockerfile` existente se conserva. Para reconstruir el frontend:

```powershell
docker build --no-cache -t rentacars-frontend:3.0 .
docker run -d --name rentacars-frontend -p 5173:80 rentacars-frontend:3.0
```

Si prefieres usar el `docker-compose.yaml` del backend, puedes volver a habilitar su bloque `frontend` apuntando a esta carpeta.
