import React, { createContext, useContext, useState, useEffect } from 'react'
import type { UserRole } from '@/services/api'

const AUTH_USER_KEY = 'user'
const AUTH_TOKEN_KEY = 'token'
const storage = window.sessionStorage

const isJwtExpired = (token: string): boolean => {
  try {
    const payloadBase64 = token.split('.')[1]
    if (!payloadBase64) return true
    const payloadJson = atob(payloadBase64)
    const payload = JSON.parse(payloadJson) as { exp?: number }
    if (!payload.exp) return false
    return payload.exp * 1000 <= Date.now()
  } catch {
    return true
  }
}

export const isHospitalStaffRole = (role?: UserRole | null): boolean => {
  return !!role && role !== 'PACIENTE'
}

export const getDefaultRouteForRole = (role?: UserRole | null): string => {
  return isHospitalStaffRole(role) ? '/admin' : '/portal'
}

interface User {
  id: number
  email: string
  firstName: string
  lastName: string
  role: UserRole
}

interface AuthContextType {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  login: (user: User, token: string) => void
  logout: () => void
  isLoading: boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const clearSession = () => {
    setUser(null)
    setToken(null)
    storage.removeItem(AUTH_USER_KEY)
    storage.removeItem(AUTH_TOKEN_KEY)
    // Limpia llaves legacy por compatibilidad con sesiones antiguas.
    window.localStorage.removeItem(AUTH_USER_KEY)
    window.localStorage.removeItem(AUTH_TOKEN_KEY)
  }

  // Restaura sesión local solo si el JWT no está expirado.
  useEffect(() => {
    const storedUser = storage.getItem(AUTH_USER_KEY)
    const storedToken = storage.getItem(AUTH_TOKEN_KEY)

    if (storedUser && storedToken) {
      try {
        if (isJwtExpired(storedToken)) {
          clearSession()
        } else {
          setUser(JSON.parse(storedUser))
          setToken(storedToken)
        }
      } catch (error) {
        console.error('Error restoring auth state:', error)
        clearSession()
      }
    }

    setIsLoading(false)
  }, [])

  // Cierra sesión si el interceptor de API detecta token inválido/revocado.
  useEffect(() => {
    const onUnauthorized = () => clearSession()
    window.addEventListener('auth:unauthorized', onUnauthorized)

    return () => {
      window.removeEventListener('auth:unauthorized', onUnauthorized)
    }
  }, [])

  const login = (newUser: User, newToken: string) => {
    if (isJwtExpired(newToken)) {
      clearSession()
      return
    }

    setUser(newUser)
    setToken(newToken)
    storage.setItem(AUTH_USER_KEY, JSON.stringify(newUser))
    storage.setItem(AUTH_TOKEN_KEY, newToken)
  }

  const logout = () => {
    clearSession()
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        login,
        logout,
        isLoading,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}

