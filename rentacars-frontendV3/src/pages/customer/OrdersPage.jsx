import { useEffect, useState } from 'react'
import { cancelarAlquiler, listarAlquileresCliente } from '../../api/services'
import { useApp } from '../../context/AppContext'
import { Icon } from '../../components/Icons'
import { esFechaFutura, fechaCorta, moneda } from '../../utils/format'
import './CustomerPages.css'

export default function OrdersPage({ onNeedAccount }) {
  const { clienteActual, notify } = useApp()
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function load() {
    if (!clienteActual) return
    setLoading(true); setError('')
    try { setItems(await listarAlquileresCliente(clienteActual.id_cliente)) }
    catch (err) { setError(err.message) }
    finally { setLoading(false) }
  }
  useEffect(() => { load() }, [clienteActual?.id_cliente])

  async function cancel(id) {
    if (!window.confirm(`¿Cancelar el alquiler #${id}? El auto volverá a quedar disponible.`)) return
    try { await cancelarAlquiler(id); notify(`Alquiler #${id} cancelado.`); load() } catch (err) { notify(err.message, 'error') }
  }

  if (!clienteActual) return <main className="page-width orders-page"><div className="empty-card"><Icon name="user" size={44}/><h2>Inicia con tu cuenta</h2><p>Necesitamos tu ID de cliente para consultar el historial.</p><button className="btn btn-primary" onClick={onNeedAccount}>Ir a Mi cuenta</button></div></main>

  return <main className="page-width orders-page"><div className="section-title-row"><div><p className="eyebrow">Cliente #{clienteActual.id_cliente}</p><h1>Mis alquileres</h1><p className="section-subtitle">Consulta tu historial y cancela reservas que todavía no han iniciado.</p></div><button className="btn btn-secondary" onClick={load}><Icon name="refresh" size={17}/> Actualizar</button></div>{error && <div className="alert alert-error">{error}</div>}{loading ? <div className="loading-line">Consultando alquileres...</div> : items.length === 0 ? <div className="empty-card"><Icon name="calendar" size={44}/><h3>Aún no tienes alquileres</h3><p>Elige un auto del catálogo para crear tu primera reserva.</p></div> : <div className="orders-list">{items.map((item) => <article className="order-card" key={item.id_alquiler}><div className="order-card__top"><div><span className="order-number">Alquiler #{item.id_alquiler}</span><h3>Auto #{item.id_auto}</h3></div><span className={`status-pill ${esFechaFutura(item.fecha_inicio) ? 'status-pill--scheduled' : 'status-pill--neutral'}`}>{esFechaFutura(item.fecha_inicio) ? 'PROGRAMADO' : 'INICIADO / HISTÓRICO'}</span></div><div className="order-grid"><div><small>Retiro</small><strong>{fechaCorta(item.fecha_inicio)}</strong><span>{item.ciudad_retirada}</span></div><div><small>Devolución</small><strong>{fechaCorta(item.fecha_fin)}</strong><span>{item.ciudad_devolucion}</span></div><div><small>Total</small><strong>{moneda.format(Number(item.precio_total))}</strong><span>Precio calculado por backend</span></div></div><div className="order-card__actions">{esFechaFutura(item.fecha_inicio) ? <button className="btn btn-danger-outline" onClick={() => cancel(item.id_alquiler)}><Icon name="trash" size={17}/> Cancelar alquiler</button> : <span className="muted-note">El backend solo permite cancelar antes de la fecha de inicio.</span>}</div></article>)}</div>}</main>
}
