import { createContext, useContext, useMemo, useState } from 'react'

const AppContext = createContext(null)
const CLIENT_KEY = 'rentacars_cliente_actual'

function loadClient() {
  try {
    return JSON.parse(localStorage.getItem(CLIENT_KEY) || 'null')
  } catch {
    return null
  }
}

export function AppProvider({ children }) {
  const [clienteActual, setClienteActualState] = useState(loadClient)
  const [toast, setToast] = useState(null)

  function setClienteActual(cliente) {
    setClienteActualState(cliente)
    if (cliente) localStorage.setItem(CLIENT_KEY, JSON.stringify(cliente))
    else localStorage.removeItem(CLIENT_KEY)
  }

  function notify(message, type = 'success') {
    setToast({ message, type, id: Date.now() })
    window.setTimeout(() => setToast(null), 3600)
  }

  const value = useMemo(
    () => ({ clienteActual, setClienteActual, toast, setToast, notify }),
    [clienteActual, toast],
  )

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>
}

export function useApp() {
  return useContext(AppContext)
}
