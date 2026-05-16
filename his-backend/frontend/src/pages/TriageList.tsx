import React from 'react'
import { useNavigate } from 'react-router-dom'
import AdminSidebar from '@/components/ui/AdminSidebar'
import StatusChip from '@/components/ui/StatusChip'
import { useAuth } from '@/context/AuthContext'
import { authAPI, triageAPI, type TriageListItemResponse, type TriagePriority } from '@/services/api'
import useSidebarPreference from '@/hooks/useSidebarPreference'

type StatusTone = 'amber' | 'blue' | 'emerald' | 'ghost' | 'orange' | 'red' | 'slate' | 'yellow'

const priorityToneMap: Record<TriagePriority, StatusTone> = {
  ROJO: 'red',
  NARANJA: 'orange',
  AMARILLO: 'yellow',
  VERDE: 'emerald',
}

const formatDateTime = (value: string) => {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return 'Fecha no disponible'
  }

  return new Intl.DateTimeFormat('es-GT', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

const TriageList: React.FC = () => {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const [loadingLogout, setLoadingLogout] = React.useState(false)
  const { collapsed: sidebarCollapsed, toggleCollapsed } = useSidebarPreference('admin-triage-list', true)
  const [loadingList, setLoadingList] = React.useState(true)
  const [refreshing, setRefreshing] = React.useState(false)
  const [error, setError] = React.useState<string | null>(null)
  const [triages, setTriages] = React.useState<TriageListItemResponse[]>([])

  const handleLogout = async () => {
    setLoadingLogout(true)
    try {
      await authAPI.logout()
    } catch (logoutError) {
      console.error('Logout error:', logoutError)
    } finally {
      logout()
      navigate('/')
      setLoadingLogout(false)
    }
  }

  const loadTriages = React.useCallback(async (showRefreshing = false) => {
    if (showRefreshing) {
      setRefreshing(true)
    } else {
      setLoadingList(true)
    }

    setError(null)

    try {
      const response = await triageAPI.listRecent()
      setTriages(response.data)
    } catch (requestError) {
      const message = (requestError as { response?: { data?: { errorMessage?: string } } })
        ?.response?.data?.errorMessage || 'No se pudo cargar el listado de triajes.'
      setError(message)
    } finally {
      setLoadingList(false)
      setRefreshing(false)
    }
  }, [])

  React.useEffect(() => {
    void loadTriages()
  }, [loadTriages])

  const summary = React.useMemo(() => {
    const total = triages.length
    const rojos = triages.filter((item) => item.prioridad === 'ROJO').length
    const alertas = triages.filter((item) => item.alertaEmergencia).length

    return { total, rojos, alertas }
  }, [triages])

  return (
    <div className="h-screen bg-gradient-to-br from-blue-100 via-sky-50 to-blue-100 text-slate-800 flex overflow-hidden">
      <AdminSidebar
        email={user?.email}
        role={user?.role}
        loading={loadingLogout}
        activeSection="triage-list"
        collapsed={sidebarCollapsed}
        onToggleCollapse={toggleCollapsed}
        onDashboard={() => navigate('/admin')}
        onTriage={() => navigate('/triage')}
        onTriageList={() => navigate('/admin/triages')}
        onLogout={() => void handleLogout()}
      />

      <main className="flex-1 p-5 lg:p-6 overflow-y-auto">
        <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4 mb-6">
          <div>
            <h2 className="text-2xl font-bold text-slate-900">Listado de Triajes</h2>
            <p className="text-sm text-slate-600 mt-1">
              Consulta consolidada de todos los triajes registrados, ordenados del más reciente al más antiguo.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <StatusChip label="CU02 · Trazabilidad clínica" tone="blue" />
            <button
              type="button"
              onClick={() => void loadTriages(true)}
              disabled={loadingList || refreshing}
              className="px-4 py-2 rounded-lg bg-white hover:bg-slate-50 text-slate-700 border border-blue-200 font-semibold text-sm disabled:opacity-60 transition"
            >
              {refreshing ? 'Actualizando...' : 'Actualizar'}
            </button>
          </div>
        </div>

        <section className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-5">
          <article className="rounded-xl border border-blue-200 bg-white shadow-sm p-4">
            <p className="text-xs uppercase tracking-wide text-slate-500">Total de triages</p>
            <p className="text-2xl font-bold text-slate-900 mt-1">{summary.total}</p>
            <div className="mt-3">
              <StatusChip label="Registros cargados" tone="blue" />
            </div>
          </article>

          <article className="rounded-xl border border-blue-200 bg-white shadow-sm p-4">
            <p className="text-xs uppercase tracking-wide text-slate-500">Prioridad roja</p>
            <p className="text-2xl font-bold text-slate-900 mt-1">{summary.rojos}</p>
            <div className="mt-3">
              <StatusChip label="Casos críticos" tone="red" />
            </div>
          </article>

          <article className="rounded-xl border border-blue-200 bg-white shadow-sm p-4">
            <p className="text-xs uppercase tracking-wide text-slate-500">Alertas de emergencia</p>
            <p className="text-2xl font-bold text-slate-900 mt-1">{summary.alertas}</p>
            <div className="mt-3">
              <StatusChip label="FA03 monitoreada" tone="amber" />
            </div>
          </article>
        </section>

        <section className="rounded-xl border border-blue-200 bg-white shadow-sm p-4 lg:p-5">
          <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4 mb-5">
            <div>
              <h3 className="text-lg font-bold text-slate-900">Triajes recientes</h3>
              <p className="text-sm text-slate-600 mt-1">
                Cada fila resume identificación del paciente, criticidad y signos vitales del ingreso.
              </p>
            </div>
            <StatusChip label="Ordenados por fecha de registro" tone="slate" />
          </div>

          {error && (
            <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
              {error}
            </div>
          )}

          {loadingList ? (
            <div className="rounded-xl border border-blue-100 bg-blue-50 p-6 text-sm text-slate-600">
              Cargando triajes recientes...
            </div>
          ) : triages.length === 0 ? (
            <div className="rounded-xl border border-blue-100 bg-blue-50 p-6 text-sm text-slate-600">
              Aún no existen triajes registrados para mostrar en el tablero.
            </div>
          ) : (
            <>
              <div className="hidden xl:block overflow-x-auto rounded-xl border border-blue-100">
                <table className="min-w-full text-xs">
                  <thead className="bg-blue-50 text-slate-700">
                    <tr>
                      <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Fecha y hora</th>
                      <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Paciente</th>
                      <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Prioridad</th>
                      <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Alerta</th>
                      <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Signos vitales</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-blue-100 bg-white">
                    {triages.map((item) => (
                      <tr key={item.signosVitalesId} className="align-top hover:bg-sky-50/60 transition">
                        <td className="px-3 py-3 text-slate-700 whitespace-nowrap">{formatDateTime(item.fechaHoraRegistro)}</td>
                        <td className="px-3 py-3">
                          <div className="font-semibold text-sm text-slate-900">{item.nombreCompleto}</div>
                          <div className="text-slate-500 mt-1">DPI: {item.dpi}</div>
                          <div className="text-slate-500 mt-1">Paciente ID: {item.pacienteId} · SV ID: {item.signosVitalesId}</div>
                        </td>
                        <td className="px-3 py-3">
                          <StatusChip label={`Prioridad: ${item.prioridad}`} tone={priorityToneMap[item.prioridad]} />
                        </td>
                        <td className="px-3 py-3">
                          <StatusChip
                            label={item.alertaEmergencia ? 'Alerta activa' : 'Sin alerta'}
                            tone={item.alertaEmergencia ? priorityToneMap[item.prioridad] : 'slate'}
                          />
                        </td>
                        <td className="px-3 py-3 text-slate-700">
                          <div className="grid grid-cols-2 gap-x-3 gap-y-1.5 min-w-[390px]">
                            <span>PA: {item.presionSistolica}/{item.presionDiastolica} mmHg</span>
                            <span>FC: {item.frecuenciaCardiaca} lpm</span>
                            <span>Temp: {item.temperatura} °C</span>
                            <span>O₂: {item.saturacionOxigeno}%</span>
                            <span>Peso: {item.pesoKg} kg</span>
                            <span>Talla: {item.tallaCm} cm</span>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="xl:hidden grid grid-cols-1 lg:grid-cols-2 gap-4 text-xs">
                {triages.map((item) => (
                  <article key={item.signosVitalesId} className="rounded-xl border border-blue-200 bg-blue-50 p-3.5">
                    <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
                      <div>
                        <p className="text-xs uppercase tracking-wide text-slate-500">{formatDateTime(item.fechaHoraRegistro)}</p>
                        <h4 className="text-sm font-semibold text-slate-900 mt-1">{item.nombreCompleto}</h4>
                        <p className="text-xs text-slate-600 mt-1">DPI: {item.dpi}</p>
                        <p className="text-xs text-slate-500 mt-1">Paciente ID: {item.pacienteId} · Signos ID: {item.signosVitalesId}</p>
                      </div>
                      <div className="flex flex-wrap gap-2">
                        <StatusChip label={`Prioridad: ${item.prioridad}`} tone={priorityToneMap[item.prioridad]} />
                        <StatusChip
                          label={item.alertaEmergencia ? 'Alerta activa' : 'Sin alerta'}
                          tone={item.alertaEmergencia ? priorityToneMap[item.prioridad] : 'slate'}
                        />
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-2 mt-3 text-xs text-slate-700">
                      <div className="rounded-lg border border-blue-100 bg-white px-3 py-2">PA: {item.presionSistolica}/{item.presionDiastolica}</div>
                      <div className="rounded-lg border border-blue-100 bg-white px-3 py-2">FC: {item.frecuenciaCardiaca} lpm</div>
                      <div className="rounded-lg border border-blue-100 bg-white px-3 py-2">Temp: {item.temperatura} °C</div>
                      <div className="rounded-lg border border-blue-100 bg-white px-3 py-2">O₂: {item.saturacionOxigeno}%</div>
                      <div className="rounded-lg border border-blue-100 bg-white px-3 py-2">Peso: {item.pesoKg} kg</div>
                      <div className="rounded-lg border border-blue-100 bg-white px-3 py-2">Talla: {item.tallaCm} cm</div>
                    </div>
                  </article>
                ))}
              </div>
            </>
          )}
        </section>
      </main>
    </div>
  )
}

export default TriageList


