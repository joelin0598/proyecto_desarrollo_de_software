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
import TriageConsultationSelection from '@/pages/TriageConsultationSelection'
import AppointmentManagement from '@/pages/AppointmentManagement'
import AppointmentQueue from '@/pages/AppointmentQueue'
import AppointmentAttentionWorkspace from '@/pages/AppointmentAttentionWorkspace'
import AppointmentAttentionInProgress from '@/pages/AppointmentAttentionInProgress'
import LaboratoryWorkbench from '@/pages/LaboratoryWorkbench'
import PharmacyWorkbench from '@/pages/PharmacyWorkbench'
import PatientReminders from '@/pages/PatientReminders'
import UserPortal from '@/pages/UserPortal'
import TriageIntake from '@/pages/TriageIntake'
import MyAppointments from '@/pages/MyAppointments'
import PatientProfileEdit from '@/pages/PatientProfileEdit'
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
              <ProtectedRoute requiredRoles={['ADMIN']}>
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
               <ProtectedRoute requiredRoles={['ADMIN', 'ENFERMERA', 'RECEPCION', 'ADMINISTRATIVO']}>
                 <TriageConsultationSelection />
               </ProtectedRoute>
             }
           />

           <Route
             path="/triage/intake"
             element={
               <ProtectedRoute requiredRoles={['ADMIN', 'ENFERMERA', 'RECEPCION', 'ADMINISTRATIVO']}>
                 <TriageIntake />
               </ProtectedRoute>
             }
           />

          <Route
            path="/admin/triages"
            element={
              <ProtectedRoute requiredRoles={['ADMIN', 'DOCTOR', 'ENFERMERA', 'RECEPCION', 'ADMINISTRATIVO']}>
                <TriageList />
              </ProtectedRoute>
            }
          />

          <Route
            path="/admin/appointments"
            element={
              <ProtectedRoute requiredRoles={['ADMIN', 'DOCTOR', 'ENFERMERA', 'RECEPCION', 'ADMINISTRATIVO']}>
                <AppointmentQueue />
              </ProtectedRoute>
            }
          />

          <Route
            path="/admin/laboratory"
            element={
              <ProtectedRoute requiredRoles={['LABORATORISTA', 'ADMIN']}>
                <LaboratoryWorkbench />
              </ProtectedRoute>
            }
          />

          <Route
            path="/admin/pharmacy"
            element={
              <ProtectedRoute requiredRoles={['FARMACEUTICO', 'ADMIN']}>
                <PharmacyWorkbench />
              </ProtectedRoute>
            }
          />

          <Route
            path="/portal/appointments"
            element={
              <ProtectedRoute requiredRoles={['PACIENTE']}>
                <AppointmentManagement />
              </ProtectedRoute>
            }
          />

          <Route
            path="/portal/my-appointments"
            element={
              <ProtectedRoute requiredRoles={['PACIENTE']}>
                <MyAppointments />
              </ProtectedRoute>
            }
          />

          <Route
            path="/portal/reminders"
            element={
              <ProtectedRoute requiredRoles={['PACIENTE']}>
                <PatientReminders />
              </ProtectedRoute>
            }
          />

          <Route
            path="/portal/profile/edit"
            element={
              <ProtectedRoute requiredRoles={['PACIENTE']}>
                <PatientProfileEdit />
              </ProtectedRoute>
            }
          />

          <Route
            path="/doctor/appointments/attention"
            element={
              <ProtectedRoute requiredRoles={['DOCTOR']}>
                <AppointmentAttentionWorkspace />
              </ProtectedRoute>
            }
          />

          <Route
            path="/doctor/appointments/attention/current"
            element={
              <ProtectedRoute requiredRoles={['DOCTOR']}>
                <AppointmentAttentionInProgress />
              </ProtectedRoute>
            }
          />

          <Route path="/doctor/consultation" element={<Navigate to="/doctor/appointments/attention" replace />} />

          <Route path="/user" element={<Navigate to="/portal" replace />} />
          <Route path="/signin" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </Router>
  )
}

export default App