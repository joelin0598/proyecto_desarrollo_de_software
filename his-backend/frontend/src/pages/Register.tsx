import React, { useEffect, useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { getDefaultRouteForRole, useAuth } from '@/context/AuthContext'
import { authAPI, catalogAPI, InsuranceOption, PatientGender, PatientGenderOption, RegisterRequest } from '@/services/api'

type RegisterFormData = Omit<RegisterRequest, 'genero'> & {
  genero: '' | PatientGender
}

const Register: React.FC = () => {
  const navigate = useNavigate()
  const { login } = useAuth()
  const [formData, setFormData] = useState<RegisterFormData>({
    nombreCompleto: '',
    email: '',
    password: '',
    dpi: '',
    genero: '',
    fechaNacimiento: '',
    direccion: '',
    telefono: '',
    contactoEmergencia: '',
    telefonoEmergencia: '',
  })
  const [genderOptions, setGenderOptions] = useState<PatientGenderOption[]>([])
  const [insuranceOptions, setInsuranceOptions] = useState<InsuranceOption[]>([])
  const [catalogLoading, setCatalogLoading] = useState(true)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    const loadCatalogs = async () => {
      try {
        const [gendersResponse, insuranceResponse] = await Promise.all([
          catalogAPI.patientGenders(),
          catalogAPI.insurances(),
        ])

        setGenderOptions(gendersResponse.data)
        setInsuranceOptions(insuranceResponse.data)
        setFormData((prev) => ({
          ...prev,
          genero: prev.genero || (gendersResponse.data[0]?.code ?? ''),
        }))
      } catch (catalogError: any) {
        setError(catalogError.response?.data?.errorMessage || 'No se pudieron cargar los catalogos de registro')
      } finally {
        setCatalogLoading(false)
      }
    }

    void loadCatalogs()
  }, [])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: name === 'aseguradoraId' ? (value ? Number(value) : undefined) : value,
    }))
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    try {
      if (!formData.genero) {
        setError('Debes seleccionar un genero')
        return
      }

      const payload: RegisterRequest = {
        ...formData,
        genero: formData.genero,
        nombreCompleto: formData.nombreCompleto.trim(),
        dpi: formData.dpi.trim(),
        fechaNacimiento: formData.fechaNacimiento || undefined,
        direccion: formData.direccion?.trim() || undefined,
        telefono: formData.telefono?.trim() || undefined,
        contactoEmergencia: formData.contactoEmergencia?.trim() || undefined,
        telefonoEmergencia: formData.telefonoEmergencia?.trim() || undefined,
        aseguradoraId: formData.aseguradoraId || undefined,
      }

      const response = await authAPI.register(payload)
      const { token, user } = response.data

      login(user, token)
      navigate(getDefaultRouteForRole(user.role))
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'Error al registrarse')
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
            <h1 className="text-lg font-semibold mt-1">Registro de Pacientes</h1>
            <nav className="mt-8 space-y-2 text-sm">
              <p className="bg-slate-800 px-3 py-2 rounded">Ficha del paciente</p>
              <p className="text-slate-300 px-3 py-2">Datos de contacto</p>
              <p className="text-slate-300 px-3 py-2">Credenciales</p>
            </nav>
          </div>
          <div className="text-xs text-slate-300">
            <p>¿Ya tienes cuenta?</p>
            <Link to="/login/paciente" className="text-sky-300 hover:text-sky-200">Iniciar sesion</Link>
          </div>
        </aside>

        <main className="p-6 lg:p-8 flex items-center justify-center">
          <section className="w-full max-w-5xl bg-white rounded-xl shadow-md border border-gray-100 p-6 lg:p-7">
            <div className="mb-4">
              <h2 className="text-2xl font-bold text-gray-800">Crear cuenta de paciente</h2>
              <p className="text-sm text-gray-500">Formulario compacto para registro en linea.</p>
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
                  placeholder="Juan Perez"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">DPI</label>
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

              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Fecha de nacimiento</label>
                <input
                  type="date"
                  name="fechaNacimiento"
                  value={formData.fechaNacimiento}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Genero</label>
                <select
                  name="genero"
                  value={formData.genero}
                  onChange={handleChange}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  disabled={catalogLoading}
                >
                  {catalogLoading && <option value="">Cargando...</option>}
                  {genderOptions.map((option) => (
                    <option key={option.code} value={option.code}>{option.label}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Telefono</label>
                <input
                  type="tel"
                  name="telefono"
                  value={formData.telefono}
                  onChange={handleChange}
                  pattern="^[0-9]{8,15}$"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  placeholder="50212345678"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Contacto de emergencia</label>
                <input
                  type="text"
                  name="contactoEmergencia"
                  value={formData.contactoEmergencia}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  placeholder="Nombre del contacto"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Telefono emergencia</label>
                <input
                  type="tel"
                  name="telefonoEmergencia"
                  value={formData.telefonoEmergencia}
                  onChange={handleChange}
                  pattern="^[0-9]{8,15}$"
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  placeholder="50276543210"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-600 mb-1">Direccion</label>
              <input
                type="text"
                name="direccion"
                value={formData.direccion}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                placeholder="Zona, colonia y referencia"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-600 mb-1">Aseguradora (opcional)</label>
              <select
                name="aseguradoraId"
                value={formData.aseguradoraId ?? ''}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                disabled={catalogLoading}
              >
                <option value="">Sin seguro</option>
                {insuranceOptions.map((option) => (
                  <option key={option.id} value={option.id}>{option.nombre}</option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">Correo electronico</label>
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

            <div className="flex items-center justify-between gap-3 pt-1">
              <Link to="/login" className="text-sm text-gray-500 hover:text-gray-700">Cambiar tipo de acceso</Link>
              <button
                type="submit"
                disabled={loading}
                className="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-4 rounded-lg transition disabled:opacity-50"
              >
                {loading ? 'Registrando...' : 'Crear cuenta'}
              </button>
            </div>
          </form>
          </section>
        </main>
      </div>
    </div>
  )
}

export default Register

