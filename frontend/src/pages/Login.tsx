import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { getDefaultRouteForRole, isHospitalStaffRole, useAuth } from '@/context/AuthContext'
import { authAPI, LoginRequest } from '@/services/api'
import StatusChip from '@/components/ui/StatusChip'

const Login: React.FC = () => {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [formData, setFormData] = useState<LoginRequest>({
    email: '',
    password: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    try {
      const response = await authAPI.login(formData)
      const { token, user } = response.data

      if (isHospitalStaffRole(user.role)) {
        setError('Esta cuenta pertenece al personal hospitalario. Usa el acceso de personal.')
        navigate('/login/personal')
        return
      }

      login(user, token)
      navigate(getDefaultRouteForRole(user.role))
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'Error al iniciar sesión')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-100 via-sky-50 to-blue-100 text-slate-800 flex">
      <aside className="w-64 bg-blue-100/85 border-r border-blue-200 shadow-sm p-4 flex flex-col justify-between">
        <div>
          <div className="mb-7">
            <p className="text-xs uppercase tracking-[0.2em] text-slate-400">HIS</p>
            <h1 className="text-xl font-bold text-slate-900 mt-1">Paciente</h1>
            <p className="text-xs text-slate-600 mt-1">Acceso al portal</p>
          </div>
          <nav className="space-y-2">
            <button type="button" className="w-full text-left px-3 py-2 rounded-lg text-sm bg-white text-blue-700 border border-blue-200 font-semibold">
              Inicio de sesion
            </button>
            <button type="button" onClick={() => navigate('/register')} className="w-full text-left px-3 py-2 rounded-lg text-sm hover:bg-white/70 text-slate-700 transition">
              Registro de paciente
            </button>
            <button type="button" onClick={() => navigate('/login')} className="w-full text-left px-3 py-2 rounded-lg text-sm hover:bg-white/70 text-slate-700 transition">
              Cambiar tipo de acceso
            </button>
          </nav>
        </div>
        <div className="rounded-lg border border-blue-200 bg-blue-50/70 p-3">
          <p className="text-xs text-slate-500">Sesion</p>
          <p className="font-semibold text-slate-800">Portal de pacientes</p>
        </div>
      </aside>

      <main className="flex-1 p-5 lg:p-6 flex items-center justify-center">
        <section className="w-full max-w-xl bg-white rounded-xl shadow-md border border-gray-100 p-6 lg:p-8">
          <div className="flex items-start justify-between gap-4 mb-6">
            <div>
              <h1 className="text-2xl font-bold text-slate-900">Iniciar sesion paciente</h1>
              <p className="text-sm text-slate-600 mt-1">Accede a tu informacion clinica y servicios en linea.</p>
            </div>
            <StatusChip label="Acceso paciente" tone="blue" />
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            {error && (
              <div className="bg-red-100 border border-red-300 text-red-700 px-4 py-3 rounded-lg text-sm">
                {error}
              </div>
            )}

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Email
              </label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                required
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600"
                placeholder="correo@ejemplo.com"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Contraseña
              </label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                required
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600"
                placeholder="Contraseña"
              />
              <p className="mt-2 text-xs text-gray-500">
                Mínimo 6 caracteres, 1 mayúscula, 1 número, 1 símbolo especial
              </p>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 px-4 rounded-lg transition disabled:opacity-50"
            >
              {loading ? 'Iniciando...' : 'Iniciar Sesión'}
            </button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-gray-600">
              ¿No tienes cuenta?{' '}
              <Link to="/register" className="text-blue-600 hover:underline font-semibold">
                Registrarse
              </Link>
            </p>
            <p className="text-xs text-gray-500 mt-3">
              ¿Eres personal del hospital?{' '}
              <Link to="/login/personal" className="text-blue-600 hover:underline font-semibold">
                Inicia sesion aqui
              </Link>
            </p>
            <p className="text-xs text-gray-500 mt-2">
              <Link to="/login" className="text-blue-600 hover:underline font-semibold">
                Cambiar tipo de acceso
              </Link>
            </p>
          </div>

          <div className="mt-8 p-4 bg-blue-50 rounded-lg border border-blue-200">
            <p className="text-xs font-semibold text-slate-700 mb-2">Credenciales de prueba:</p>
            <p className="text-xs text-slate-600">
              <strong>Admin:</strong> admin@hospital.com / AdminPass123!@#
            </p>
            <p className="text-xs text-slate-600">
              <strong>User:</strong> user@example.com / UserPass123!@#
            </p>
          </div>
        </section>
      </main>
    </div>
  )
}

export default Login

