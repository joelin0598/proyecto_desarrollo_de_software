import '@/styles/index.css'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from '@/context/AuthContext'
import LandingPage from '@/pages/LandingPage'
import AccessSelector from '@/pages/AccessSelector'
import Login from '@/pages/Login'
import StaffLogin from '@/pages/StaffLogin'
import Register from '@/pages/Register'
import StaffRegister from '@/pages/StaffRegister'
import AdminDashboard from '@/pages/AdminDashboard'
import UserMaintenance from '@/pages/UserMaintenance'
import TriageList from '@/pages/TriageList'
import UserPortal from '@/pages/UserPortal'
import TriageIntake from '@/pages/TriageIntake'
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
            element={
              <PublicOnlyRoute>
                <StaffRegister />
              </PublicOnlyRoute>
            }
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
            path="/admin/users"
            element={
              <ProtectedRoute requiredRoles={HOSPITAL_STAFF_ROLES}>
                <UserMaintenance />
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

          <Route
            path="/triage"
            element={
              <ProtectedRoute requiredRoles={HOSPITAL_STAFF_ROLES}>
                <TriageIntake />
              </ProtectedRoute>
            }
          />

          <Route
            path="/admin/triages"
            element={
              <ProtectedRoute requiredRoles={HOSPITAL_STAFF_ROLES}>
                <TriageList />
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

