import React from 'react'
import { Navigate } from 'react-router-dom'
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
    return <Navigate to={getDefaultRouteForRole(user.role)} replace />
  }

  return <>{children}</>
}

export default ProtectedRoute

