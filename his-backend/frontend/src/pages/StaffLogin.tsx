import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { getDefaultRouteForRole, isHospitalStaffRole, useAuth } from '@/context/AuthContext'
import { authAPI, LoginRequest } from '@/services/api'
import Header from '@/components/Header'

const StaffLogin: React.FC = () => {
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
    setFormData((prev) => ({ ...prev, [name]: value }))
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    try {
      const response = await authAPI.login(formData)
      const { token, user } = response.data

      if (!isHospitalStaffRole(user.role)) {
        setError('Esta cuenta pertenece a un paciente. Usa el inicio de sesion de pacientes.')
        navigate('/login/paciente')
        return
      }

      login(user, token)
      navigate(getDefaultRouteForRole(user.role))
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'Error al iniciar sesion')
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
            Acceso Personal Hospitalario
          </h1>
          <p className="text-center text-gray-600 mb-8">
            Inicia sesion con tu cuenta institucional
          </p>

          <form onSubmit={handleSubmit} className="space-y-6">
            {error && (
              <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                {error}
              </div>
            )}

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Correo institucional
              </label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                required
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600"
                placeholder="personal@hospital.com"
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
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-lg transition disabled:opacity-50"
            >
              {loading ? 'Ingresando...' : 'Ingresar como Personal'}
            </button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-xs text-gray-500">
              ¿Eres paciente?{' '}
              <Link to="/login/paciente" className="text-blue-600 hover:underline font-semibold">
                Inicia sesion aqui
              </Link>
            </p>
            <p className="text-xs text-gray-500 mt-2">
              <Link to="/login" className="text-blue-600 hover:underline font-semibold">
                Cambiar tipo de acceso
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default StaffLogin

