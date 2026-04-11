import React from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { authAPI } from '@/services/api'
import Header from '@/components/Header'

const AdminDashboard: React.FC = () => {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const [loading, setLoading] = React.useState(false)

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
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />

      <main className="flex-1 max-w-7xl mx-auto px-4 py-8 w-full">
        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="text-gray-500 text-sm font-medium">Pacientes</div>
            <div className="text-3xl font-bold text-blue-600 mt-2">1,234</div>
            <div className="text-green-600 text-sm mt-2">+12% vs mes anterior</div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="text-gray-500 text-sm font-medium">Médicos</div>
            <div className="text-3xl font-bold text-green-600 mt-2">45</div>
            <div className="text-green-600 text-sm mt-2">Todos disponibles</div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="text-gray-500 text-sm font-medium">Citas Hoy</div>
            <div className="text-3xl font-bold text-purple-600 mt-2">28</div>
            <div className="text-green-600 text-sm mt-2">Confirmadas</div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="text-gray-500 text-sm font-medium">Consultas Pendientes</div>
            <div className="text-3xl font-bold text-orange-600 mt-2">12</div>
            <div className="text-orange-600 text-sm mt-2">Atender ahora</div>
          </div>
        </div>

        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left Column */}
          <div className="lg:col-span-2 space-y-6">
            {/* Usuarios Registrados */}
            <div className="bg-white rounded-lg shadow overflow-hidden">
              <div className="bg-red-50 px-6 py-4 border-b">
                <h2 className="text-xl font-bold text-gray-800">Últimos Usuarios Registrados</h2>
              </div>
              <div className="divide-y">
                {[
                  { name: 'Juan Pérez', email: 'juan@example.com', role: 'USER', date: 'Hace 2 horas' },
                  { name: 'María García', email: 'maria@hospital.com', role: 'DOCTOR', date: 'Hace 5 horas' },
                  { name: 'Carlos López', email: 'carlos@example.com', role: 'USER', date: 'Hace 1 día' },
                ].map((user, idx) => (
                  <div key={idx} className="px-6 py-4 flex justify-between items-center hover:bg-gray-50">
                    <div>
                      <p className="font-semibold text-gray-800">{user.name}</p>
                      <p className="text-sm text-gray-500">{user.email}</p>
                    </div>
                    <div className="text-right">
                      <span className="inline-block bg-blue-100 text-blue-800 text-xs font-semibold px-3 py-1 rounded-full">
                        {user.role}
                      </span>
                      <p className="text-xs text-gray-500 mt-1">{user.date}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Actividad */}
            <div className="bg-white rounded-lg shadow overflow-hidden">
              <div className="bg-red-50 px-6 py-4 border-b">
                <h2 className="text-xl font-bold text-gray-800">Actividad del Sistema</h2>
              </div>
              <div className="px-6 py-4 space-y-3">
                {[
                  'Usuario juan@example.com registrado exitosamente',
                  'Backup de base de datos completado',
                  'Servidor iniciado correctamente',
                  'Token JWT validado para 156 usuarios',
                ].map((activity, idx) => (
                  <div key={idx} className="flex items-start gap-3">
                    <div className="w-2 h-2 bg-green-500 rounded-full mt-2"></div>
                    <p className="text-gray-700">{activity}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Right Column */}
          <div className="space-y-6">
            {/* Info Box */}
            <div className="bg-white rounded-lg shadow p-6 border-l-4 border-red-600">
              <h3 className="font-bold text-lg text-gray-800 mb-3">👤 Tu Información</h3>
              <div className="space-y-2 text-sm">
                <p><strong>Email:</strong> {user?.email}</p>
                <p><strong>Rol:</strong> <span className="bg-red-100 text-red-800 px-2 py-1 rounded">{user?.role}</span></p>
                <p><strong>Usuario desde:</strong> 5/04/2026</p>
                <p><strong>Estado:</strong> <span className="text-green-600">🟢 Activo</span></p>
              </div>
            </div>

            {/* Quick Actions */}
            <div className="bg-white rounded-lg shadow p-6">
              <h3 className="font-bold text-lg text-gray-800 mb-4">Acciones Rápidas</h3>
              <div className="space-y-2">
                <button className="w-full bg-blue-600 hover:bg-blue-700 text-white py-2 px-4 rounded-lg transition text-sm font-semibold">
                  Gestionar Usuarios
                </button>
                <button
                  className="w-full bg-orange-600 hover:bg-orange-700 text-white py-2 px-4 rounded-lg transition text-sm font-semibold"
                  onClick={() => navigate('/admin/payments')}
                >
                  Verificación de Pagos
                </button>
                <button className="w-full bg-green-600 hover:bg-green-700 text-white py-2 px-4 rounded-lg transition text-sm font-semibold">
                  Ver Reportes
                </button>
                <button className="w-full bg-purple-600 hover:bg-purple-700 text-white py-2 px-4 rounded-lg transition text-sm font-semibold">
                  Configuración
                </button>
              </div>
            </div>

            {/* Security Info */}
            <div className="bg-yellow-50 rounded-lg shadow p-6 border-l-4 border-yellow-500">
              <h3 className="font-bold text-gray-800 mb-2">🔒 Seguridad</h3>
              <p className="text-sm text-gray-700">Tu sesión está protegida con JWT. Token expira en 24 horas.</p>
              <p className="text-xs text-gray-600 mt-2">Último acceso: Ahora</p>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}

export default AdminDashboard
