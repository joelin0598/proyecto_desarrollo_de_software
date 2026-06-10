import React from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { authAPI } from '@/services/api'
import AdminSidebar from '@/components/ui/AdminSidebar'
import StatusChip from '@/components/ui/StatusChip'
import UseCaseModuleCard, { type UseCaseModule } from '@/components/ui/UseCaseModuleCard'
import useSidebarPreference from '@/hooks/useSidebarPreference'

const baseModules: UseCaseModule[] = [
  { title: 'Registro de Pacientes', subtitle: '', detail: 'Registro y clasificación de urgencia', route: '/triage', enabled: true, accent: 'from-cyan-500 to-cyan-600', icon: '🏥' },
  { title: 'Gestión de Citas', subtitle: '', detail: 'Programación y validación administrativa', route: '/admin/appointments', enabled: true, accent: 'from-sky-500 to-sky-600', icon: '📅' },
  { title: 'Mantenimiento de Usuarios', subtitle: '', detail: 'Altas, edición, suspensión y eliminación', route: '/admin/users', enabled: true, accent: 'from-indigo-500 to-indigo-600', icon: '👥' },
  { title: 'Atención Médica', subtitle: '', detail: 'Asignación clínica y cierre de atención', route: '/doctor/appointments/attention', enabled: false, accent: 'from-emerald-500 to-emerald-600', icon: '⚕️' },
  { title: 'Laboratorio', subtitle: '', detail: 'Muestras y resultados', route: '/admin/laboratory', enabled: false, accent: 'from-violet-500 to-violet-600', icon: '🔬' },
  { title: 'Farmacia', subtitle: '', detail: 'Despacho y recordatorios', route: '/admin/pharmacy', enabled: false, accent: 'from-amber-500 to-amber-600', icon: '💊' },
]

const AdminDashboard: React.FC = () => {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const [loading, setLoading] = React.useState(false)
  const { collapsed: sidebarCollapsed, toggleCollapsed } = useSidebarPreference('admin-shell', false)
  const quickModules = React.useMemo(
    () => baseModules.map((module) =>
      module.title === 'Atención Médica'
        ? { ...module, enabled: user?.role === 'DOCTOR' }
        : module.title === 'Laboratorio'
          ? { ...module, enabled: user?.role === 'LABORATORISTA' || user?.role === 'ADMIN' }
          : module.title === 'Farmacia'
            ? { ...module, enabled: user?.role === 'FARMACEUTICO' || user?.role === 'ADMIN' }
        : module
    ),
    [user?.role],
  )

  const handleLogout = async () => {
    setLoading(true)
    try {
      await authAPI.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      logout()
      navigate('/')
      setLoading(false)
    }
  }

  return (
    <div className="h-screen bg-gradient-to-br from-blue-100 via-sky-50 to-blue-100 text-slate-800 flex overflow-hidden">
      <AdminSidebar
        email={user?.email}
        role={user?.role}
        loading={loading}
        activeSection="dashboard"
        collapsed={sidebarCollapsed}
        showSessionDetails
        onToggleCollapse={toggleCollapsed}
        onDashboard={() => navigate('/admin')}
        onTriage={() => navigate('/triage')}
        onUsers={() => navigate('/admin/users')}
        onTriageList={() => navigate('/admin/triages')}
        onAppointments={() => navigate('/admin/appointments')}
        onConsultation={() => navigate('/doctor/appointments/attention')}
        onLogout={() => void handleLogout()}
      />

      <main className="flex-1 p-5 lg:p-6 overflow-y-auto">
        <div className="flex items-start justify-between gap-4 mb-6">
          <div>
            <h2 className="text-2xl font-bold text-slate-900">Dashboard</h2>
            <p className="text-sm text-slate-600 mt-1">Módulos administrativos e institucionales (acceso según rol)</p>
          </div>
          <StatusChip label="Admin con acceso total" tone="amber" />
        </div>

        <section className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {quickModules.map((module) => (
            <UseCaseModuleCard
              key={module.title}
              module={module}
              onClick={(route) => route && navigate(route)}
            />
          ))}
        </section>
      </main>
    </div>
  )
}

export default AdminDashboard