import React from 'react'
import { Link, Navigate } from 'react-router-dom'
import { getDefaultRouteForRole, useAuth } from '@/context/AuthContext'
import type { UserRole } from '@/services/api'

interface ProtectedRouteProps {
  children: React.ReactNode
  requiredRoles?: UserRole[]
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRoles }) => {
  const { isAuthenticated, user, isLoading } = useAuth()

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Cargando...</p>
        </div>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  if (!user) {
    return <Navigate to="/login" replace />
  }

  if (requiredRoles && !requiredRoles.includes(user.role)) {
    const fallbackRoute = getDefaultRouteForRole(user.role)
    return (
      <div className="min-h-screen bg-slate-100 flex items-center justify-center p-4">
        <div className="max-w-lg w-full rounded-xl border border-amber-200 bg-amber-50 px-5 py-6 shadow-sm">
          <h2 className="text-xl font-bold text-amber-900">Sin acceso</h2>
          <p className="text-sm text-amber-800 mt-2">Sin accesos, comuniquese con un administrador.</p>
          <div className="mt-4">
            <Link
              to={fallbackRoute}
              className="inline-flex px-4 py-2 rounded-lg border border-amber-300 bg-white text-amber-900 text-sm font-semibold hover:bg-amber-100"
            >
              Regresar
            </Link>
          </div>
        </div>
      </div>
    )
  }

  return <>{children}</>
}

export default ProtectedRoute

