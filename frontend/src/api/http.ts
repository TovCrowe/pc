import axios from 'axios'

const STORAGE_KEY = 'pc.auth'

/** The Basic auth token (base64 of "user:pass") persisted across reloads. */
export function getStoredToken(): string | null {
  return localStorage.getItem(STORAGE_KEY)
}

export function storeToken(token: string): void {
  localStorage.setItem(STORAGE_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(STORAGE_KEY)
}

/** Build the HTTP Basic credential value from a username/password. */
export function encodeBasic(username: string, password: string): string {
  return btoa(`${username}:${password}`)
}

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
})

// Attach the Basic auth header to every request when logged in.
http.interceptors.request.use((config) => {
  const token = getStoredToken()
  if (token) {
    config.headers.Authorization = `Basic ${token}`
  }
  return config
})

// On 401 the credentials are invalid/expired — drop them and bounce to login.
http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && getStoredToken()) {
      clearToken()
      window.location.assign('/login')
    }
    return Promise.reject(error)
  },
)
