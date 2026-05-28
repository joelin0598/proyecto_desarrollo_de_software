import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { authAPI, LoginRequest } from '@/services/api'
import Header from '@/components/Header'

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

      login(user, token)

      // Redirigir según el rol
      if (user.role === 'ADMIN') {
        navigate('/admin')
      } else {
        navigate('/user')
      }
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'Error al iniciar sesión')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />

      <div className="flex-1 flex items-center justify-center px-4 py-8">
        <div className="bg-white rounded-lg shadow-2xl w-full max-w-md p-8">
          <h1 className="text-3xl font-bold text-center text-gray-800 mb-2">
            Iniciar Sesión
          </h1>
          <p className="text-center text-gray-600 mb-8">
            Accede a tu cuenta del HIS
          </p>

          <form onSubmit={handleSubmit} className="space-y-6">
            {error && (
              <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
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
              className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-lg transition disabled:opacity-50"
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
          </div>

          <div className="mt-8 p-4 bg-gray-100 rounded-lg">
            <p className="text-xs font-semibold text-gray-700 mb-2">Test Usuarios:</p>
            <p className="text-xs text-gray-600">
              <strong>Admin:</strong> admin@hospital.com / AdminPass123!@#
            </p>
            <p className="text-xs text-gray-600">
              <strong>User:</strong> user@example.com / UserPass123!@#
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Login

