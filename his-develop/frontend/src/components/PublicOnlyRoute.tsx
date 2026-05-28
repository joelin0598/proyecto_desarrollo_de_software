import React from 'react'
import { Navigate } from 'react-router-dom'
import { getDefaultRouteForRole, useAuth } from '@/context/AuthContext'

interface PublicOnlyRouteProps {
  children: React.ReactNode
}

const PublicOnlyRoute: React.FC<PublicOnlyRouteProps> = ({ children }) => {
  const { isAuthenticated, user, isLoading } = useAuth()

  if (isLoading) {
    return null
  }

  if (isAuthenticated) {
    return <Navigate to={getDefaultRouteForRole(user?.role)} replace />
  }

  return <>{children}</>
}

export default PublicOnlyRoute

