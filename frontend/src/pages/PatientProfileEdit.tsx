import React from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { authAPI, catalogAPI, type PatientGender, patientAPI } from '@/services/api'
import HospitalLogo from '@/components/ui/HospitalLogo'

type ProfileForm = {
  nombre: string
  dpi: string
  email: string
  fechaNacimiento: string
  telefono: string
  direccion: string
  genero: PatientGender | ''
}

const PatientProfileEdit: React.FC = () => {
  const navigate = useNavigate()
  const { logout } = useAuth()
  const [loading, setLoading] = React.useState(true)
  const [saving, setSaving] = React.useState(false)
  const [loggingOut, setLoggingOut] = React.useState(false)
  const [error, setError] = React.useState<string | null>(null)
  const [message, setMessage] = React.useState<string | null>(null)
  const [genderOptions, setGenderOptions] = React.useState<Array<{ code: PatientGender; label: string }>>([])
  const [form, setForm] = React.useState<ProfileForm>({
    nombre: '',
    dpi: '',
    email: '',
    fechaNacimiento: '',
    telefono: '',
    direccion: '',
    genero: '',
  })

  const loadProfile = React.useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [profileResponse, gendersResponse] = await Promise.all([
        patientAPI.getMyProfile(),
        catalogAPI.patientGenders(),
      ])

      const profile = profileResponse.data
      setGenderOptions(gendersResponse.data)
      setForm({
        nombre: profile.nombre || '',
        dpi: profile.dpi || '',
        email: profile.email || '',
        fechaNacimiento: profile.fechaNacimiento || '',
        telefono: profile.telefono || '',
        direccion: profile.direccion || '',
        genero: (profile.genero as PatientGender | undefined) || '',
      })
    } catch (requestError: any) {
      setError(requestError?.response?.data?.errorMessage || 'No se pudo cargar el perfil del paciente.')
    } finally {
      setLoading(false)
    }
  }, [])

  React.useEffect(() => {
    void loadProfile()
  }, [loadProfile])

  const handleSave = async (event: React.FormEvent) => {
    event.preventDefault()
    setSaving(true)
    setError(null)
    setMessage(null)

    try {
      await patientAPI.updateProfile({
        telefono: form.telefono.trim() || undefined,
        direccion: form.direccion.trim() || undefined,
        genero: form.genero || undefined,
      })
      setMessage('Perfil actualizado correctamente.')
    } catch (requestError: any) {
      setError(requestError?.response?.data?.errorMessage || 'No se pudo actualizar el perfil.')
    } finally {
      setSaving(false)
    }
  }

  const handleLogout = async () => {
    setLoggingOut(true)
    try {
      await authAPI.logout()
    } catch {
      // Ignorar fallo remoto de logout para priorizar limpieza local.
    } finally {
      logout()
      navigate('/')
      setLoggingOut(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-100 via-sky-50 to-blue-100 text-slate-800 flex">
      <aside className="w-64 bg-blue-100/85 border-r border-blue-200 shadow-sm p-4 flex flex-col justify-between">
        <div>
          <div className="mb-7 flex items-center gap-3">
            <HospitalLogo className="h-10 w-10" alt="Hospital" />
            <div>
              <p className="text-xs uppercase tracking-[0.2em] text-slate-400">HIS</p>
              <h1 className="text-xl font-bold text-slate-900 mt-1">Perfil paciente</h1>
              <p className="text-xs text-slate-600 mt-1">Edición restringida</p>
            </div>
          </div>
          <nav className="space-y-2 text-sm">
            <button type="button" onClick={() => navigate('/portal')} className="w-full text-left px-3 py-2 rounded-lg hover:bg-white/70 text-slate-700 transition">Volver al portal</button>
            <button type="button" className="w-full text-left px-3 py-2 rounded-lg bg-white text-blue-700 border border-blue-200 font-semibold">Editar perfil</button>
          </nav>
        </div>

        <button
          type="button"
          onClick={() => void handleLogout()}
          disabled={loggingOut}
          className="w-full px-4 py-2 rounded-lg bg-white hover:bg-slate-50 text-slate-700 border border-blue-200 font-semibold text-sm disabled:opacity-60"
        >
          {loggingOut ? 'Cerrando...' : 'Cerrar sesión'}
        </button>
      </aside>

      <main className="flex-1 p-5 lg:p-6 overflow-y-auto">
        <section className="max-w-4xl rounded-xl border border-blue-200 bg-white shadow-sm p-6">
          <h2 className="text-2xl font-bold text-slate-900">Editar mi perfil</h2>
          <p className="text-sm text-slate-600 mt-1">Los campos base del expediente son de solo lectura.</p>

          {error && <div className="mt-4 rounded-lg border border-red-300 bg-red-100 px-4 py-3 text-sm text-red-700">{error}</div>}
          {message && <div className="mt-4 rounded-lg border border-emerald-300 bg-emerald-100 px-4 py-3 text-sm text-emerald-700">{message}</div>}

          {loading ? (
            <div className="mt-5 rounded-lg border border-blue-100 bg-blue-50 px-4 py-6 text-sm text-slate-600">Cargando perfil...</div>
          ) : (
            <form onSubmit={handleSave} className="mt-5 space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Nombre completo (solo lectura)</label>
                  <input value={form.nombre} disabled className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-100 text-sm" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">DPI (solo lectura)</label>
                  <input value={form.dpi} disabled className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-100 text-sm" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Correo (solo lectura)</label>
                  <input value={form.email} disabled className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-100 text-sm" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Fecha de nacimiento (solo lectura)</label>
                  <input value={form.fechaNacimiento} disabled className="w-full px-3 py-2 border border-gray-300 rounded-lg bg-gray-100 text-sm" />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Teléfono</label>
                  <input
                    value={form.telefono}
                    onChange={(event) => setForm((prev) => ({ ...prev, telefono: event.target.value.replace(/\D/g, '').slice(0, 15) }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    placeholder="Solo números"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Género</label>
                  <select
                    value={form.genero}
                    onChange={(event) => setForm((prev) => ({ ...prev, genero: event.target.value as PatientGender }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  >
                    <option value="">Seleccionar</option>
                    {genderOptions.map((option) => (
                      <option key={option.code} value={option.code}>{option.label}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Dirección</label>
                  <input
                    value={form.direccion}
                    onChange={(event) => setForm((prev) => ({ ...prev, direccion: event.target.value }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    placeholder="Zona, colonia y referencia"
                  />
                </div>
              </div>

              <div className="pt-2 flex justify-end">
                <button type="submit" disabled={saving} className="px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm disabled:opacity-60">
                  {saving ? 'Guardando...' : 'Guardar cambios'}
                </button>
              </div>
            </form>
          )}
        </section>
      </main>
    </div>
  )
}

export default PatientProfileEdit

