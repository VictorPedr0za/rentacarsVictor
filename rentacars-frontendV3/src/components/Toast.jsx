import { useApp } from '../context/AppContext'
import { Icon } from './Icons'
import './Toast.css'

export default function Toast() {
  const { toast, setToast } = useApp()
  if (!toast) return null
  return (
    <div className={`toast toast--${toast.type}`}>
      <Icon name={toast.type === 'error' ? 'info' : 'check'} />
      <span>{toast.message}</span>
      <button type="button" onClick={() => setToast(null)} aria-label="Cerrar"><Icon name="close" size={17} /></button>
    </div>
  )
}
