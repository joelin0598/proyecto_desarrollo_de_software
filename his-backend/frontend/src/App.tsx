import '@/styles/index.css'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { AuthProvider } from '@/context/AuthContext'
import LandingPage from '@/pages/LandingPage'
import Login from '@/pages/Login'
import Register from '@/pages/Register'
import AdminDashboard from '@/pages/AdminDashboard'
import UserPortal from '@/pages/UserPortal'
import AppointmentScheduling from '@/pages/AppointmentScheduling'
import PaymentVerification from '@/pages/PaymentVerification'
import ProtectedRoute from '@/components/ProtectedRoute'

function App() {
  return (
    <Router>
      <AuthProvider>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Protected Routes */}
          <Route
            path="/admin"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminDashboard />
              </ProtectedRoute>
            }
          />

          <Route
            path="/admin/payments"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <PaymentVerification />
              </ProtectedRoute>
            }
          />

          <Route
            path="/user"
            element={
              <ProtectedRoute requiredRole="USER">
                <UserPortal />
              </ProtectedRoute>
            }
          />

          <Route
            path="/user/appointments/new"
            element={
              <ProtectedRoute requiredRole="USER">
                <AppointmentScheduling />
              </ProtectedRoute>
            }
          />
        </Routes>
      </AuthProvider>
    </Router>
  )
}

export default App

