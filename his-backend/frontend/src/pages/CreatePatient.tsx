import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { patientsAPI, PatientRequest } from '@/services/api'
import Header from '@/components/Header'

interface PatientFormData {
  firstName: string
  lastName: string
  email: string
  password: string
  telefono: string
  direccion: string
  dpi: string
}

const CreatePatient: React.FC = () => {
  const navigate = useNavigate()
  const [formData, setFormData] = useState<PatientFormData>({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    telefono: '',
    direccion: '',
    dpi: '',
  })
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
    setError('')
    setSuccess('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    setSuccess('')

    // Build payload, omitting empty optional fields
    const payload: PatientRequest = {
      firstName: formData.firstName,
      lastName: formData.lastName,
      email: formData.email,
      password: formData.password,
    }
    if (formData.telefono.trim()) payload.telefono = formData.telefono.trim()
    if (formData.direccion.trim()) payload.direccion = formData.direccion.trim()
    if (formData.dpi.trim()) payload.dpi = formData.dpi.trim()

    try {
      await patientsAPI.create(payload)
      setSuccess('Paciente registrado exitosamente')
      setFormData({
        firstName: '',
        lastName: '',
        email: '',
        password: '',
        telefono: '',
        direccion: '',
        dpi: '',
      })
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'Error al registrar paciente')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />

      <main className="flex-1 flex items-center justify-center px-4 py-8">
        <div className="bg-white rounded-lg shadow-2xl w-full max-w-lg p-8">
          <h1 className="text-3xl font-bold text-center text-gray-800 mb-2">
            Registrar Paciente
          </h1>
          <div className="text-center text-gray-600 mb-8">
            Crear nuevo registro de paciente
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded text-sm">
                {error}
              </div>
            )}

            {success && (
              <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded text-sm">
                {success}
              </div>
            )}

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nombre
                </label>
                <input
                  type="text"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  placeholder="Juan"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Apellido
                </label>
                <input
                  type="text"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  placeholder="Pérez"
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Email
              </label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                placeholder="correo@ejemplo.com"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Contraseña
              </label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                placeholder="Contraseña"
              />
              <div className="mt-1 text-xs text-gray-500">
                Mín 6 caracteres, 1 mayúscula, 1 número, 1 símbolo
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Teléfono <span className="text-gray-400">(opcional)</span>
              </label>
              <input
                type="text"
                name="telefono"
                value={formData.telefono}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                placeholder="71234567"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Dirección <span className="text-gray-400">(opcional)</span>
              </label>
              <input
                type="text"
                name="direccion"
                value={formData.direccion}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                placeholder="Calle Principal 123"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                DPI <span className="text-gray-400">(opcional)</span>
              </label>
              <input
                type="text"
                name="dpi"
                value={formData.dpi}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                placeholder="1234567890123"
              />
              <div className="mt-1 text-xs text-gray-500">
                13 dígitos
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-lg transition disabled:opacity-50 mt-6"
            >
              {loading ? 'Registrando...' : 'Registrar Paciente'}
            </button>
          </form>

          <div className="mt-6 text-center">
            <button
              onClick={() => navigate('/admin')}
              className="text-gray-500 hover:text-gray-700 text-sm"
            >
              ← Volver al Dashboard
            </button>
          </div>
        </div>
      </main>
    </div>
  )
}

export default CreatePatient
