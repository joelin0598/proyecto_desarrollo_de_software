import React from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { authAPI } from '@/services/api'
import Header from '@/components/Header'

const UserPortal: React.FC = () => {
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
        {/* Welcome Section */}
        <div className="bg-white rounded-lg shadow p-8 mb-8">
          <h2 className="text-2xl font-bold text-gray-800 mb-2">
            ¡Bienvenido, {user?.firstName}!
          </h2>
          <p className="text-gray-600">
            Accede a tu información médica y gestiona tus citas desde este portal.
          </p>
        </div>

        {/* Main Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
          {/* Card 1: Próximas Citas */}
          <div className="bg-white rounded-lg shadow overflow-hidden hover:shadow-lg transition">
            <div className="bg-blue-50 px-6 py-4 border-l-4 border-blue-600">
              <h3 className="text-lg font-bold text-gray-800">📅 Próximas Citas</h3>
            </div>
            <div className="p-6">
              <div className="space-y-3">
                <div className="p-3 bg-blue-50 rounded-lg">
                  <p className="font-semibold text-blue-900">Dr. García López</p>
                  <p className="text-sm text-blue-700">6/04/2026 - 10:00 AM</p>
                  <p className="text-xs text-gray-600 mt-1">Cardiología</p>
                </div>
                <div className="p-3 bg-blue-50 rounded-lg">
                  <p className="font-semibold text-blue-900">Dr. Martínez Pérez</p>
                  <p className="text-sm text-blue-700">12/04/2026 - 2:30 PM</p>
                  <p className="text-xs text-gray-600 mt-1">Medicina General</p>
                </div>
              </div>
              <button className="w-full mt-4 bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg transition text-sm font-semibold">
                Ver todas las citas
              </button>
            </div>
          </div>

          {/* Card 2: Historial Médico */}
          <div className="bg-white rounded-lg shadow overflow-hidden hover:shadow-lg transition">
            <div className="bg-green-50 px-6 py-4 border-l-4 border-green-600">
              <h3 className="text-lg font-bold text-gray-800">📋 Historial Médico</h3>
            </div>
            <div className="p-6">
              <div className="space-y-2 text-sm text-gray-700">
                <p>✓ Última consulta: 3/04/2026</p>
                <p>✓ Exámenes recientes: 2/04/2026</p>
                <p>✓ Medicamentos activos: 3</p>
                <p>✓ Alergias registradas: Penicilina</p>
              </div>
              <button className="w-full mt-4 bg-green-600 hover:bg-green-700 text-white py-2 rounded-lg transition text-sm font-semibold">
                Descargar historial
              </button>
            </div>
          </div>

          {/* Card 3: Documentos */}
          <div className="bg-white rounded-lg shadow overflow-hidden hover:shadow-lg transition">
            <div className="bg-purple-50 px-6 py-4 border-l-4 border-purple-600">
              <h3 className="text-lg font-bold text-gray-800">📄 Documentos</h3>
            </div>
            <div className="p-6">
              <div className="space-y-2">
                <div className="p-2 bg-purple-50 rounded flex items-center justify-between">
                  <span className="text-sm font-semibold">Examen de Laboratorio</span>
                  <span className="text-xs text-purple-600">2/04</span>
                </div>
                <div className="p-2 bg-purple-50 rounded flex items-center justify-between">
                  <span className="text-sm font-semibold">Radiografía</span>
                  <span className="text-xs text-purple-600">1/04</span>
                </div>
                <div className="p-2 bg-purple-50 rounded flex items-center justify-between">
                  <span className="text-sm font-semibold">Receta Médica</span>
                  <span className="text-xs text-purple-600">31/03</span>
                </div>
              </div>
              <button className="w-full mt-4 bg-purple-600 hover:bg-purple-700 text-white py-2 rounded-lg transition text-sm font-semibold">
                Ver todos
              </button>
            </div>
          </div>
        </div>

        {/* Additional Cards */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Personal Info */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-bold text-gray-800 mb-4">👤 Información Personal</h3>
            <div className="space-y-3">
              <div>
                <p className="text-xs text-gray-500 uppercase">Nombre Completo</p>
                <p className="text-lg font-semibold text-gray-800">{user?.firstName} {user?.lastName}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 uppercase">Email</p>
                <p className="text-lg font-semibold text-gray-800">{user?.email}</p>
              </div>
              <div>
                <p className="text-xs text-gray-500 uppercase">Tipo de Usuario</p>
                <p className="text-lg font-semibold text-gray-800">{user?.role}</p>
              </div>
              <button className="w-full mt-4 bg-gray-600 hover:bg-gray-700 text-white py-2 rounded-lg transition text-sm font-semibold">
                Editar Perfil
              </button>
            </div>
          </div>

          {/* Support */}
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-bold text-gray-800 mb-4">🆘 Necesitas Ayuda?</h3>
            <div className="space-y-3">
              <button className="w-full text-left p-3 bg-orange-50 hover:bg-orange-100 rounded-lg transition">
                <p className="font-semibold text-orange-900">Contactar Soporte</p>
                <p className="text-sm text-orange-700">Disponible 24/7</p>
              </button>
              <button className="w-full text-left p-3 bg-blue-50 hover:bg-blue-100 rounded-lg transition">
                <p className="font-semibold text-blue-900">Ver FAQs</p>
                <p className="text-sm text-blue-700">Preguntas frecuentes</p>
              </button>
              <button className="w-full text-left p-3 bg-green-50 hover:bg-green-100 rounded-lg transition">
                <p className="font-semibold text-green-900">Solicitar Cita</p>
                <p className="text-sm text-green-700">Agendar nueva cita</p>
              </button>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}

export default UserPortal
