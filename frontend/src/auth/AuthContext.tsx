import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import { http, encodeBasic, storeToken, clearToken, getStoredToken } from '../api/http'

interface AuthContextValue {
  isAuthenticated: boolean
  username: string | null
  /** Validates credentials against the API, then persists them on success. */
  login: (username: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

const USER_KEY = 'pc.user'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => getStoredToken())
  const [username, setUsername] = useState<string | null>(() => localStorage.getItem(USER_KEY))

  const value = useMemo<AuthContextValue>(
    () => ({
      isAuthenticated: token != null,
      username,
      async login(user, password) {
        const encoded = encodeBasic(user, password)
        // Verify the credentials with a real authenticated request before storing.
        await http.get('/clients', { headers: { Authorization: `Basic ${encoded}` } })
        storeToken(encoded)
        localStorage.setItem(USER_KEY, user)
        setToken(encoded)
        setUsername(user)
      },
      logout() {
        clearToken()
        localStorage.removeItem(USER_KEY)
        setToken(null)
        setUsername(null)
      },
    }),
    [token, username],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}
