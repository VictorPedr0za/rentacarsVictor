import { useEffect, useMemo, useState } from 'react'
import { buscarAutos, crearAlquiler, listarCategorias, listarTiendas } from '../../api/services'
import { useApp } from '../../context/AppContext'
import CarCard from '../../components/CarCard'
import Modal from '../../components/Modal'
import { Icon } from '../../components/Icons'
import { mananaISO, moneda, sumarDiasISO } from '../../utils/format'
import './CustomerPages.css'

export default function CatalogPage({ onNeedAccount, onGoOrders }) {
  const { clienteActual, notify } = useApp()
  const [autos, setAutos] = useState([])
  const [categorias, setCategorias] = useState([])
  const [tiendas, setTiendas] = useState([])
  const [ciudad, setCiudad] = useState('')
  const [categoria, setCategoria] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [selected, setSelected] = useState(null)

  const ciudades = useMemo(() => [...new Set(tiendas.map((t) => t.ciudad).filter(Boolean))].sort(), [tiendas])

  async function loadReference() {
    try {
      const [cats, stores] = await Promise.all([listarCategorias(), listarTiendas()])
      setCategorias(cats)
      setTiendas(stores)
    } catch (err) {
      setError(err.message)
    }
  }

  async function loadCars() {
    setLoading(true)
    setError('')
    try {
      setAutos(await buscarAutos({ ciudad, idCategoria: categoria }))
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadReference() }, [])
  useEffect(() => { loadCars() }, [])

  function elegir(auto) {
    if (!clienteActual) {
      notify('Primero crea o selecciona tu cuenta de cliente.', 'error')
      onNeedAccount()
      return
    }
    setSelected(auto)
  }

  return (
    <main>
      <section className="hero">
        <div className="hero__content">
          <span className="hero__badge">Reserva fácil · Backend Spring Boot</span>
          <h1>Encuentra el auto ideal para tu próximo viaje</h1>
          <p>Busca por ciudad o categoría, compara el precio diario y reserva con tu cuenta.</p>
        </div>
      </section>

      <section className="search-panel page-width">
        <div className="search-field">
          <label>Ciudad de retiro</label>
          <select value={ciudad} onChange={(e) => setCiudad(e.target.value)}>
            <option value="">Todas las ciudades</option>
            {ciudades.map((c) => <option key={c} value={c}>{c}</option>)}
          </select>
        </div>
        <div className="search-field">
          <label>Categoría</label>
          <select value={categoria} onChange={(e) => setCategoria(e.target.value)}>
            <option value="">Todas las categorías</option>
            {categorias.map((c) => <option key={c.id_categoria} value={c.id_categoria}>{c.nombre}</option>)}
          </select>
        </div>
        <button className="btn btn-primary search-action" onClick={loadCars} disabled={loading}>
          <Icon name="search" size={18} /> {loading ? 'Buscando...' : 'Buscar autos'}
        </button>
      </section>

      <section className="page-width catalog-section">
        <div className="section-title-row">
          <div><p className="eyebrow">Catálogo disponible</p><h2>Autos para alquilar</h2></div>
          <span className="result-count">{autos.length} resultado{autos.length === 1 ? '' : 's'}</span>
        </div>

        {error && <div className="alert alert-error">{error}</div>}
        {loading ? <div className="skeleton-grid">{[1,2,3,4].map((x) => <div className="skeleton-card" key={x} />)}</div> :
          autos.length === 0 ? <div className="empty-card"><Icon name="car" size={42}/><h3>No hay autos disponibles</h3><p>Prueba otra ciudad o categoría.</p></div> :
          <div className="car-grid">{autos.map((auto) => <CarCard key={auto.id_auto} auto={auto} onRent={elegir} />)}</div>
        }
      </section>

      <RentModal auto={selected} open={Boolean(selected)} onClose={() => setSelected(null)} cliente={clienteActual} tiendas={tiendas} onCreated={() => { setSelected(null); loadCars(); onGoOrders() }} />
    </main>
  )
}

function RentModal({ auto, open, onClose, cliente, tiendas, onCreated }) {
  const { notify } = useApp()
  const startDefault = mananaISO()
  const [inicio, setInicio] = useState(startDefault)
  const [fin, setFin] = useState(sumarDiasISO(startDefault, 2))
  const [retirada, setRetirada] = useState('')
  const [devolucion, setDevolucion] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const ciudades = [...new Set(tiendas.map((t) => t.ciudad).filter(Boolean))].sort()

  useEffect(() => {
    if (open) {
      const ini = mananaISO()
      setInicio(ini)
      setFin(sumarDiasISO(ini, 2))
      setError('')
    }
  }, [open])

  async function submit(e) {
    e.preventDefault()
    if (!cliente || !auto) return
    setSaving(true); setError('')
    try {
      const rental = await crearAlquiler({
        id_cliente: cliente.id_cliente,
        id_auto: auto.id_auto,
        fecha_inicio: inicio,
        fecha_fin: fin,
        ciudad_retirada: retirada,
        ciudad_devolucion: devolucion,
      })
      notify(`Alquiler #${rental.id_alquiler} creado correctamente.`)
      onCreated(rental)
    } catch (err) {
      setError(err.message)
    } finally { setSaving(false) }
  }

  const oferta = Number(auto?.oferta_porcentaje || 0)
  const price = auto ? Number(auto.precio_dia) * (1 - oferta / 100) : 0

  return (
    <Modal open={open} onClose={onClose} title="Completar alquiler" subtitle={auto ? `${auto.marca} ${auto.modelo} · ${moneda.format(price)} por día` : ''}>
      <form className="form-stack" onSubmit={submit}>
        <div className="account-summary"><Icon name="user"/><div><small>Reserva a nombre de</small><strong>{cliente?.nombre}</strong><span>{cliente?.email}</span></div></div>
        <div className="form-grid two">
          <label className="field"><span>Fecha de retiro</span><input type="date" min={mananaISO()} value={inicio} onChange={(e) => { setInicio(e.target.value); if (fin <= e.target.value) setFin(sumarDiasISO(e.target.value, 1)) }} required /></label>
          <label className="field"><span>Fecha de devolución</span><input type="date" min={sumarDiasISO(inicio, 1)} value={fin} onChange={(e) => setFin(e.target.value)} required /></label>
          <label className="field"><span>Ciudad de retiro</span><select value={retirada} onChange={(e) => setRetirada(e.target.value)} required><option value="">Selecciona</option>{ciudades.map((c) => <option key={c}>{c}</option>)}</select></label>
          <label className="field"><span>Ciudad de devolución</span><select value={devolucion} onChange={(e) => setDevolucion(e.target.value)} required><option value="">Selecciona</option>{ciudades.map((c) => <option key={c}>{c}</option>)}</select></label>
        </div>
        {error && <div className="alert alert-error">{error}</div>}
        <div className="form-actions"><button className="btn btn-secondary" type="button" onClick={onClose}>Cancelar</button><button className="btn btn-primary" disabled={saving}>{saving ? 'Procesando...' : 'Confirmar alquiler'}</button></div>
      </form>
    </Modal>
  )
}
