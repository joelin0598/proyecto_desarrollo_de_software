import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { getDefaultRouteForRole, isHospitalStaffRole, useAuth } from '@/context/AuthContext'
import { authAPI, RegisterAdminRequest } from '@/services/api'
import Header from '@/components/Header'

const StaffRegister: React.FC = () => {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [formData, setFormData] = useState<RegisterAdminRequest>({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    direccion: '',
    telefono: '',
    dpi: '',
    numeroColegiado: '',
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
      const payload: RegisterAdminRequest = {
        ...formData,
        numeroColegiado: formData.numeroColegiado?.trim() || undefined,
      }

      const response = await authAPI.registerPersonal(payload)
      const { token, user } = response.data

      if (!isHospitalStaffRole(user.role)) {
        setError('La cuenta creada no fue clasificada como personal hospitalario.')
        return
      }

      login(user, token)
      navigate(getDefaultRouteForRole(user.role))
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'Error al registrar personal hospitalario')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />

      <div className="flex-1 flex items-center justify-center px-4 py-8">
        <div className="bg-white rounded-lg shadow-2xl w-full max-w-lg p-8">
          <h1 className="text-3xl font-bold text-center text-gray-800 mb-2">
            Registro de Personal Hospitalario
          </h1>
          <p className="text-center text-gray-600 mb-8">
            Completa tus datos institucionales para crear tu cuenta
          </p>

          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded text-sm">
                {error}
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Nombre</label>
                <input
                  type="text"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  placeholder="Ana"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Apellido</label>
                <input
                  type="text"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  placeholder="Gomez"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Correo institucional</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                placeholder="usuario@hospital.com"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Direccion</label>
              <input
                type="text"
                name="direccion"
                value={formData.direccion}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                placeholder="Direccion de residencia"
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Telefono</label>
                <input
                  type="text"
                  name="telefono"
                  value={formData.telefono}
                  onChange={handleChange}
                  required
                  pattern="^[0-9]{8,15}$"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  placeholder="50212345678"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">DPI</label>
                <input
                  type="text"
                  name="dpi"
                  value={formData.dpi}
                  onChange={handleChange}
                  required
                  pattern="^[0-9]{13}$"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  placeholder="13 digitos"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Numero de colegiado (opcional)</label>
              <input
                type="text"
                name="numeroColegiado"
                value={formData.numeroColegiado}
                onChange={handleChange}
                maxLength={20}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                placeholder="Si aplica a tu puesto"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Contraseña</label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                placeholder="Contraseña"
              />
              <p className="mt-1 text-xs text-gray-500">Minimo 6 caracteres, 1 mayuscula, 1 numero y 1 simbolo.</p>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-lg transition disabled:opacity-50 mt-4"
            >
              {loading ? 'Registrando...' : 'Crear cuenta de personal'}
            </button>
          </form>

          <div className="mt-6 text-center text-sm">
            <p className="text-gray-600">
              ¿Ya tienes cuenta?{' '}
              <Link to="/login/personal" className="text-blue-600 hover:underline font-semibold">
                Inicia sesion aqui
              </Link>
            </p>
            <p className="text-xs text-gray-500 mt-2">
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

export default StaffRegister

