import { useState } from 'react'
import { actualizarCliente, listarClientes, registrarCliente } from '../../api/services'
import { useApp } from '../../context/AppContext'
import { Icon } from '../../components/Icons'
import './CustomerPages.css'

export default function AccountPage() {
  const { clienteActual, setClienteActual, notify } = useApp()
  const [mode, setMode] = useState(clienteActual ? 'profile' : 'login')

  return (
    <main className="page-width account-page">
      <div className="section-title-row"><div><p className="eyebrow">Área del cliente</p><h1>Mi cuenta</h1></div></div>
      {clienteActual && mode === 'profile' ? (
        <Profile cliente={clienteActual} onChangeAccount={() => setMode('login')} />
      ) : (
        <div className="account-layout">
          <ExistingClient onSelected={(client) => { setClienteActual(client); setMode('profile'); notify(`Bienvenido, ${client.nombre}.`) }} />
          <CreateClient onCreated={(client) => { setClienteActual(client); setMode('profile'); notify('Cuenta creada correctamente.') }} />
        </div>
      )}
    </main>
  )
}

function ExistingClient({ onSelected }) {
  const [id, setId] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function submit(e) {
    e.preventDefault(); setLoading(true); setError('')
    try {
      const clients = await listarClientes()
      const client = clients.find((c) => String(c.id_cliente) === String(id))
      if (!client) throw new Error(`No existe un cliente con ID ${id}.`)
      onSelected(client)
    } catch (err) { setError(err.message) } finally { setLoading(false) }
  }

  return <section className="account-card"><span className="card-icon"><Icon name="user"/></span><h2>Ya tengo cuenta</h2><p>Ingresa el ID de cliente asignado por el sistema.</p><form className="form-stack" onSubmit={submit}><label className="field"><span>ID de cliente</span><input type="number" min="1" value={id} onChange={(e) => setId(e.target.value)} placeholder="Ej. 1" required /></label>{error && <div className="alert alert-error">{error}</div>}<button className="btn btn-primary" disabled={loading}>{loading ? 'Buscando...' : 'Entrar a mi cuenta'}</button></form></section>
}

function CreateClient({ onCreated }) {
  const [form, setForm] = useState({ nombre:'', email:'', telefono:'', tarjeta_credito:'' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  function set(key, value) { setForm((prev) => ({ ...prev, [key]: value })) }
  async function submit(e) { e.preventDefault(); setLoading(true); setError(''); try { onCreated(await registrarCliente(form)) } catch (err) { setError(err.message) } finally { setLoading(false) } }
  return <section className="account-card account-card--highlight"><span className="card-icon"><Icon name="plus"/></span><h2>Crear cuenta</h2><p>Registra tus datos para poder reservar vehículos.</p><form className="form-stack" onSubmit={submit}><label className="field"><span>Nombre completo</span><input value={form.nombre} onChange={(e) => set('nombre', e.target.value)} required /></label><label className="field"><span>Correo</span><input type="email" value={form.email} onChange={(e) => set('email', e.target.value)} required /></label><div className="form-grid two"><label className="field"><span>Teléfono</span><input value={form.telefono} onChange={(e) => set('telefono', e.target.value)} required /></label><label className="field"><span>Tarjeta</span><input value={form.tarjeta_credito} onChange={(e) => set('tarjeta_credito', e.target.value)} required /></label></div>{error && <div className="alert alert-error">{error}</div>}<button className="btn btn-primary" disabled={loading}>{loading ? 'Creando...' : 'Crear mi cuenta'}</button></form></section>
}

function Profile({ cliente, onChangeAccount }) {
  const { setClienteActual, notify } = useApp()
  const [telefono, setTelefono] = useState(cliente.telefono || '')
  const [tarjeta, setTarjeta] = useState('')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  async function save(e) {
    e.preventDefault(); setSaving(true); setError('')
    try {
      const payload = { telefono }
      if (tarjeta.trim()) payload.tarjeta_credito = tarjeta.trim()
      const updated = await actualizarCliente(cliente.id_cliente, payload)
      setClienteActual({ ...cliente, ...updated })
      setTarjeta('')
      notify('Datos de contacto actualizados.')
    } catch (err) { setError(err.message) } finally { setSaving(false) }
  }

  return <div className="profile-grid"><section className="profile-card"><div className="profile-avatar">{cliente.nombre?.slice(0,1)?.toUpperCase()}</div><h2>{cliente.nombre}</h2><p>{cliente.email}</p><dl><div><dt>ID cliente</dt><dd>#{cliente.id_cliente}</dd></div><div><dt>Teléfono</dt><dd>{cliente.telefono || '—'}</dd></div><div><dt>Tarjeta</dt><dd>{cliente.tarjeta_credito || 'Protegida'}</dd></div></dl><button className="btn btn-secondary btn-block" onClick={onChangeAccount}>Cambiar de cuenta</button></section><section className="settings-card"><h2>Gestionar mis datos</h2><p>El backend permite actualizar teléfono y tarjeta. El email se conserva sin cambios.</p><form className="form-stack" onSubmit={save}><label className="field"><span>Teléfono</span><input value={telefono} onChange={(e) => setTelefono(e.target.value)} required /></label><label className="field"><span>Nueva tarjeta (opcional)</span><input value={tarjeta} onChange={(e) => setTarjeta(e.target.value)} placeholder="Déjalo vacío para conservar la actual" /></label>{error && <div className="alert alert-error">{error}</div>}<button className="btn btn-primary" disabled={saving}>{saving ? 'Guardando...' : 'Guardar cambios'}</button></form><button className="text-button danger" onClick={() => { setClienteActual(null); onChangeAccount() }}><Icon name="logout" size={17}/> Cerrar sesión local</button></section></div>
}
