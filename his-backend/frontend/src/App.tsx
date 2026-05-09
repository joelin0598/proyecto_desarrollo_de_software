import '@/styles/index.css'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from '@/context/AuthContext'
import LandingPage from '@/pages/LandingPage'
import AccessSelector from '@/pages/AccessSelector'
import Login from '@/pages/Login'
import StaffLogin from '@/pages/StaffLogin'
import Register from '@/pages/Register'
import AdminDashboard from '@/pages/AdminDashboard'
import UserPortal from '@/pages/UserPortal'
import ProtectedRoute from '@/components/ProtectedRoute'
import PublicOnlyRoute from '@/components/PublicOnlyRoute'
import { HOSPITAL_STAFF_ROLES } from '@/services/api'

function App() {
  return (
    <Router>
      <AuthProvider>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<LandingPage />} />
          <Route
            path="/login"
            element={
              <PublicOnlyRoute>
                <AccessSelector />
              </PublicOnlyRoute>
            }
          />
          <Route
            path="/login/paciente"
            element={
              <PublicOnlyRoute>
                <Login />
              </PublicOnlyRoute>
            }
          />
          <Route
            path="/login/personal"
            element={
              <PublicOnlyRoute>
                <StaffLogin />
              </PublicOnlyRoute>
            }
          />
          <Route
            path="/register"
            element={
              <PublicOnlyRoute>
                <Register />
              </PublicOnlyRoute>
            }
          />
          <Route
            path="/register/personal"
            element={<Navigate to="/login/personal" replace />}
          />

          {/* Protected Routes */}
          <Route
            path="/admin"
            element={
              <ProtectedRoute requiredRoles={HOSPITAL_STAFF_ROLES}>
                <AdminDashboard />
              </ProtectedRoute>
            }
          />

          <Route
            path="/portal"
            element={
              <ProtectedRoute requiredRoles={['PACIENTE']}>
                <UserPortal />
              </ProtectedRoute>
            }
          />

          <Route path="/user" element={<Navigate to="/portal" replace />} />
          <Route path="/signin" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </Router>
  )
}

export default App

