import React, { createContext, useContext, useState, useEffect } from 'react'
import { AuthResponse } from '@/services/api'

interface User {
  id: number
  email: string
  firstName: string
  lastName: string
  role: 'USER' | 'ADMIN'
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

  // Cargar datos del localStorage al iniciar
  useEffect(() => {
    const storedUser = localStorage.getItem('user')
    const storedToken = localStorage.getItem('token')

    if (storedUser && storedToken) {
      try {
        setUser(JSON.parse(storedUser))
        setToken(storedToken)
      } catch (error) {
        console.error('Error restoring auth state:', error)
        localStorage.removeItem('user')
        localStorage.removeItem('token')
      }
    }

    setIsLoading(false)
  }, [])

  const login = (newUser: User, newToken: string) => {
    setUser(newUser)
    setToken(newToken)
    localStorage.setItem('user', JSON.stringify(newUser))
    localStorage.setItem('token', newToken)
  }

  const logout = () => {
    setUser(null)
    setToken(null)
    localStorage.removeItem('user')
    localStorage.removeItem('token')
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

