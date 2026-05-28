import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { getDefaultRouteForRole, isHospitalStaffRole, useAuth } from '@/context/AuthContext'
import { authAPI, HOSPITAL_STAFF_ROLES, RegisterAdminRequest, UserRole } from '@/services/api'

const StaffRegister: React.FC = () => {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [formData, setFormData] = useState<RegisterAdminRequest & {
    especialidadIdInput: string
    unidadAtencionIdInput: string
  }>({
    nombreCompleto: '',
    email: '',
    password: '',
    direccion: '',
    telefonoCorporativo: '',
    rol: 'ADMINISTRATIVO',
    numeroColegiado: '',
    especialidadIdInput: '',
    unidadAtencionIdInput: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    try {
      const payload: RegisterAdminRequest = {
        nombreCompleto: formData.nombreCompleto.trim(),
        email: formData.email.trim(),
        password: formData.password,
        direccion: formData.direccion.trim(),
        telefonoCorporativo: formData.telefonoCorporativo.trim(),
        rol: formData.rol as UserRole,
        numeroColegiado: formData.numeroColegiado?.trim() || undefined,
        especialidadId: formData.especialidadIdInput ? Number(formData.especialidadIdInput) : undefined,
        unidadAtencionId: formData.unidadAtencionIdInput ? Number(formData.unidadAtencionIdInput) : undefined,
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
    <div className="h-screen bg-gray-100 overflow-hidden">
      <div className="h-full grid grid-cols-[220px_1fr]">
        <aside className="bg-slate-900 text-white px-5 py-6 flex flex-col justify-between">
          <div>
            <p className="text-xs uppercase tracking-wider text-slate-300">HIS</p>
            <h1 className="text-lg font-semibold mt-1">Registro de personal</h1>
            <nav className="mt-8 space-y-2 text-sm">
              <p className="bg-slate-800 px-3 py-2 rounded">Perfil profesional</p>
              <p className="text-slate-300 px-3 py-2">Acceso al sistema</p>
              <p className="text-slate-300 px-3 py-2">Datos administrativos</p>
            </nav>
          </div>
          <div className="text-xs text-slate-300">
            <p>¿Ya tienes cuenta?</p>
            <Link to="/login/personal" className="text-sky-300 hover:text-sky-200">Iniciar sesion</Link>
          </div>
        </aside>

        <main className="p-6 lg:p-8 flex items-center justify-center">
          <section className="w-full max-w-5xl bg-white rounded-xl shadow-md border border-gray-100 p-6 lg:p-7">
            <div className="mb-4">
              <h2 className="text-2xl font-bold text-gray-800">Crear cuenta de personal</h2>
              <p className="text-sm text-gray-500">Registro operativo para personal_hospitalario.</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded text-sm">
                {error}
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Nombre completo</label>
                <input
                  type="text"
                  name="nombreCompleto"
                  value={formData.nombreCompleto}
                  onChange={handleChange}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  placeholder="Ana Gomez"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Rol</label>
                <select
                  name="rol"
                  value={formData.rol}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                >
                  {HOSPITAL_STAFF_ROLES.map((role) => (
                    <option key={role} value={role}>{role}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Numero colegiado</label>
                <input
                  type="text"
                  name="numeroColegiado"
                  value={formData.numeroColegiado}
                  onChange={handleChange}
                  maxLength={20}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  placeholder="Opcional"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Correo institucional</label>
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
                <label className="block text-xs font-semibold text-gray-600 mb-1">Contrasena</label>
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  placeholder="Minimo 6 caracteres"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Telefono corporativo</label>
                <input
                  type="text"
                  name="telefonoCorporativo"
                  value={formData.telefonoCorporativo}
                  onChange={handleChange}
                  required
                  pattern="^[0-9]{8,15}$"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  placeholder="50212345678"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Especialidad ID</label>
                <input
                  type="number"
                  min={1}
                  name="especialidadIdInput"
                  value={formData.especialidadIdInput}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  placeholder="Opcional"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Unidad atencion ID</label>
                <input
                  type="number"
                  min={1}
                  name="unidadAtencionIdInput"
                  value={formData.unidadAtencionIdInput}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  placeholder="Opcional"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Direccion</label>
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
            </div>

            <div className="flex items-center justify-between gap-3 pt-1">
              <Link to="/login" className="text-sm text-gray-500 hover:text-gray-700">Cambiar tipo de acceso</Link>
              <button
                type="submit"
                disabled={loading}
                className="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-4 rounded-lg transition disabled:opacity-50"
              >
                {loading ? 'Registrando...' : 'Crear cuenta de personal'}
              </button>
            </div>
          </form>
          </section>
        </main>
      </div>
    </div>
  )
}

export default StaffRegister

