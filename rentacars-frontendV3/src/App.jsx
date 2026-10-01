import { useState } from 'react'
import { AppProvider } from './context/AppContext'
import AppHeader from './components/AppHeader'
import Toast from './components/Toast'
import CatalogPage from './pages/customer/CatalogPage'
import AccountPage from './pages/customer/AccountPage'
import OrdersPage from './pages/customer/OrdersPage'
import AdminPage from './pages/admin/AdminPage'
import './App.css'

function AppContent() {
  const [area, setArea] = useState('cliente')
  const [customerPage, setCustomerPage] = useState('catalogo')

  return (
    <div className="app">
      <AppHeader area={area} onAreaChange={setArea} customerPage={customerPage} onCustomerPageChange={setCustomerPage} />
      {area === 'admin' ? (
        <AdminPage />
      ) : customerPage === 'cuenta' ? (
        <AccountPage />
      ) : customerPage === 'alquileres' ? (
        <OrdersPage onNeedAccount={() => setCustomerPage('cuenta')} />
      ) : (
        <CatalogPage onNeedAccount={() => setCustomerPage('cuenta')} onGoOrders={() => setCustomerPage('alquileres')} />
      )}
      <footer className="site-footer"><div className="page-width"><strong>RentaCars</strong><span>Proyecto académico · React + Spring Boot + PostgreSQL</span></div></footer>
      <Toast />
    </div>
  )
}

export default function App() {
  return <AppProvider><AppContent /></AppProvider>
}
