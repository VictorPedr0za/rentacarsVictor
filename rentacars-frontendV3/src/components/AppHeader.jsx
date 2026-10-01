import { Icon } from './Icons'
import { useApp } from '../context/AppContext'
import './AppHeader.css'

export default function AppHeader({ area, onAreaChange, customerPage, onCustomerPageChange }) {
  const { clienteActual } = useApp()

  return (
    <>
      <header className="topbar">
        <button className="brand" type="button" onClick={() => onAreaChange('cliente')}>
          <span className="brand__mark"><Icon name="car" size={28} /></span>
          <span><strong>Renta</strong>Cars</span>
        </button>

        <span className="topbar__spacer" aria-hidden="true" />

        <button className="account-pill" type="button" onClick={() => { onAreaChange('cliente'); onCustomerPageChange('cuenta') }}>
          <Icon name="user" />
          <span>
            <small>{clienteActual ? 'Hola,' : 'Cliente'}</small>
            <strong>{clienteActual?.nombre?.split(' ')[0] || 'Mi cuenta'}</strong>
          </span>
        </button>

        <button className="admin-pill" type="button" onClick={() => onAreaChange('admin')}>
          <Icon name="admin" />
          <span><small>Panel</small><strong>Administrador</strong></span>
        </button>
      </header>

      <nav className="subnav">
        <button className={area === 'cliente' && customerPage === 'catalogo' ? 'active' : ''} onClick={() => { onAreaChange('cliente'); onCustomerPageChange('catalogo') }}>
          <Icon name="home" size={17} /> Inicio
        </button>
        <button className={area === 'cliente' && customerPage === 'cuenta' ? 'active' : ''} onClick={() => { onAreaChange('cliente'); onCustomerPageChange('cuenta') }}>
          Mi cuenta
        </button>
        <button className={area === 'cliente' && customerPage === 'alquileres' ? 'active' : ''} onClick={() => { onAreaChange('cliente'); onCustomerPageChange('alquileres') }}>
          Mis alquileres
        </button>
        <span className="subnav__spacer" />
        <button className={area === 'admin' ? 'active' : ''} onClick={() => onAreaChange('admin')}>
          Administración
        </button>
      </nav>
    </>
  )
}
