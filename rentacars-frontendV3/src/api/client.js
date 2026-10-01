const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8081'

export class ApiError extends Error {
  constructor(message, status = 0) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

async function request(path, { method = 'GET', body, params } = {}) {
  const url = new URL(path, BASE_URL)

  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        url.searchParams.set(key, value)
      }
    })
  }

  let response
  try {
    response = await fetch(url, {
      method,
      headers: body !== undefined ? { 'Content-Type': 'application/json' } : undefined,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError(
      `No se pudo conectar con el backend en ${BASE_URL}. Verifica que Spring Boot esté activo y que VITE_API_URL sea correcto.`,
      0,
    )
  }

  if (response.status === 204) return null

  const contentType = response.headers.get('content-type') || ''
  const data = contentType.includes('application/json')
    ? await response.json().catch(() => null)
    : await response.text().catch(() => '')

  if (!response.ok) {
    const message =
      (typeof data === 'object' && data?.mensaje) ||
      (typeof data === 'object' && data?.message) ||
      (typeof data === 'object' && data?.error) ||
      (typeof data === 'string' && data) ||
      `Error ${response.status} en ${method} ${path}`
    throw new ApiError(message, response.status)
  }

  return data
}

export const apiClient = {
  get: (path, params) => request(path, { method: 'GET', params }),
  post: (path, body) => request(path, { method: 'POST', body }),
  put: (path, body) => request(path, { method: 'PUT', body }),
  patch: (path, body) => request(path, { method: 'PATCH', body }),
  delete: (path) => request(path, { method: 'DELETE' }),
}

export { BASE_URL }
