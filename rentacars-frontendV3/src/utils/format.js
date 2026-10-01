export const moneda = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 0,
})

export function fechaCorta(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('es-CO', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(new Date(`${value}T00:00:00`))
}

function localISO(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

export function mananaISO() {
  const date = new Date()
  date.setDate(date.getDate() + 1)
  return localISO(date)
}

export function sumarDiasISO(iso, days = 1) {
  if (!iso) return ''
  const date = new Date(`${iso}T00:00:00`)
  date.setDate(date.getDate() + days)
  return localISO(date)
}

export function esFechaFutura(iso) {
  if (!iso) return false
  const hoy = new Date()
  hoy.setHours(0, 0, 0, 0)
  return new Date(`${iso}T00:00:00`) > hoy
}
