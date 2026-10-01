import { apiClient } from './client'

// CATEGORIAS — HU-06 / HU-07
export const listarCategorias = () => apiClient.get('/categorias')
export const crearCategoria = ({ nombre, descripcion }) =>
  apiClient.post('/categorias', { nombre, descripcion })

// TIENDAS — HU-01 a HU-05
export const listarTiendas = (ciudad) => apiClient.get('/tiendas', { ciudad })
export const obtenerTienda = (id) => apiClient.get(`/tiendas/${id}`)
export const crearTienda = (data) => apiClient.post('/tiendas', data)
export const actualizarTienda = (id, data) => apiClient.put(`/tiendas/${id}`, data)
export const eliminarTienda = (id) => apiClient.delete(`/tiendas/${id}`)

// AUTOS — HU-08 a HU-13
export const buscarAutos = ({ ciudad, idCategoria } = {}) =>
  apiClient.get('/autos', {
    ciudad: ciudad || undefined,
    id_categoria: idCategoria || undefined,
  })
export const obtenerDetalleAuto = (id) => apiClient.get(`/autos/${id}`)
export const crearAuto = (data) => apiClient.post('/autos', data)
export const actualizarDetallesAuto = (id, data) => apiClient.put(`/autos/${id}`, data)
export const actualizarDisponibilidadAuto = (id, disponibilidad) =>
  apiClient.patch(`/autos/${id}/disponibilidad`, { disponibilidad })
export const eliminarAuto = (id) => apiClient.delete(`/autos/${id}`)

// CLIENTES — HU-14 a HU-16
export const listarClientes = () => apiClient.get('/clientes')
export const registrarCliente = (data) => apiClient.post('/clientes', data)
export const actualizarCliente = (id, data) => apiClient.put(`/clientes/${id}`, data)

// ALQUILERES — HU-18, HU-20, HU-21, HU-22, HU-24
export const crearAlquiler = (data) => apiClient.post('/alquileres', data)
export const listarAlquileresCliente = (idCliente) =>
  apiClient.get('/alquileres', { id_cliente: idCliente })
export const listarAlquileresActivos = () => apiClient.get('/alquileres/activos')
export const cancelarAlquiler = (id) => apiClient.delete(`/alquileres/${id}`)
export const registrarDevolucion = (id) => apiClient.put(`/alquileres/${id}/devolucion`)

// Utilidades de UI: combinan endpoints existentes sin inventar APIs nuevas.
export async function obtenerTodosLosAlquileres() {
  const [clientes, activos] = await Promise.all([listarClientes(), listarAlquileresActivos()])
  const activoIds = new Set(activos.map((a) => a.id_alquiler))

  const historiales = await Promise.all(
    clientes.map(async (cliente) => {
      const alquileres = await listarAlquileresCliente(cliente.id_cliente)
      return alquileres.map((alquiler) => ({
        ...alquiler,
        id_cliente: cliente.id_cliente,
        cliente_nombre: cliente.nombre,
        cliente_email: cliente.email,
        estado_ui: activoIds.has(alquiler.id_alquiler) ? 'ACTIVO' : 'FINALIZADO',
      }))
    }),
  )

  return historiales.flat().sort((a, b) => b.id_alquiler - a.id_alquiler)
}

export async function obtenerFlotaAdministrativa() {
  const [disponibles, activos] = await Promise.all([buscarAutos(), listarAlquileresActivos()])
  const conocidos = new Map(disponibles.map((auto) => [auto.id_auto, auto]))
  const idsOcupados = [...new Set(activos.map((a) => a.id_auto))].filter((id) => !conocidos.has(id))

  const ocupados = await Promise.all(
    idsOcupados.map(async (id) => {
      try {
        return await obtenerDetalleAuto(id)
      } catch {
        return null
      }
    }),
  )

  ocupados.filter(Boolean).forEach((auto) => conocidos.set(auto.id_auto, auto))
  return [...conocidos.values()].sort((a, b) => a.id_auto - b.id_auto)
}
