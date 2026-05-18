import React from 'react'
import { useNavigate } from 'react-router-dom'
import AdminSidebar from '@/components/ui/AdminSidebar'
import StatusChip from '@/components/ui/StatusChip'
import { authAPI, appointmentAPI, type ScheduleAppointmentResponse } from '@/services/api'
import { useAuth } from '@/context/AuthContext'
import useSidebarPreference from '@/hooks/useSidebarPreference'

const formatDate = (dateValue: string) => {
  const date = new Date(dateValue)
  if (Number.isNaN(date.getTime())) return dateValue
  return new Intl.DateTimeFormat('es-GT', { dateStyle: 'medium' }).format(date)
}

const toneByAdminState = (state: ScheduleAppointmentResponse['estadoAdministrativo']) =>
  state === 'PAGO_VALIDADO' ? 'emerald' : 'amber'

const AppointmentQueue: React.FC = () => {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const { collapsed: sidebarCollapsed, toggleCollapsed } = useSidebarPreference('admin-appointments-queue', true)

  const [loadingLogout, setLoadingLogout] = React.useState(false)
  const [loadingList, setLoadingList] = React.useState(true)
  const [refreshing, setRefreshing] = React.useState(false)
  const [feedback, setFeedback] = React.useState<string | null>(null)
  const [appointments, setAppointments] = React.useState<ScheduleAppointmentResponse[]>([])
  const [search, setSearch] = React.useState('')
  const [adminStateFilter, setAdminStateFilter] = React.useState<'ALL' | 'PAGO_VALIDADO' | 'PAGO_PENDIENTE'>('ALL')

  const handleLogout = async () => {
    setLoadingLogout(true)
    try {
      await authAPI.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      logout()
      navigate('/')
      setLoadingLogout(false)
    }
  }

  const loadAppointments = React.useCallback(async (showRefreshing = false) => {
    if (showRefreshing) setRefreshing(true)
    else setLoadingList(true)

    try {
      const response = await appointmentAPI.list()
      setAppointments(response.data)
      setFeedback(null)
    } catch (error: any) {
      const message = error?.response?.data?.errorMessage || 'No se pudo cargar la cola de citas.'
      setFeedback(message)
    } finally {
      setLoadingList(false)
      setRefreshing(false)
    }
  }, [])

  React.useEffect(() => {
    void loadAppointments()
  }, [loadAppointments])

  const filteredAppointments = React.useMemo(() => {
    const normalized = search.trim().toLowerCase()

    return appointments.filter((item) => {
      const matchesSearch = !normalized || [
        item.citaMedicaId,
        item.pacienteNombre,
        item.pacienteIdentificacion,
        item.medicoNombre,
        item.especialidadNombre,
        item.metodoPago,
        item.estadoCita,
        item.estadoAdministrativo,
        item.transaccionId,
        item.mensajeValidacion,
      ].join(' ').toLowerCase().includes(normalized)

      const matchesState = adminStateFilter === 'ALL' || item.estadoAdministrativo === adminStateFilter
      return matchesSearch && matchesState
    })
  }, [appointments, search, adminStateFilter])

  const total = appointments.length
  const validated = appointments.filter((item) => item.estadoAdministrativo === 'PAGO_VALIDADO').length
  const pending = appointments.filter((item) => item.estadoAdministrativo === 'PAGO_PENDIENTE').length

  return (
    <div className="h-screen bg-gradient-to-br from-blue-100 via-sky-50 to-blue-100 text-slate-800 flex overflow-hidden">
      <AdminSidebar
        email={user?.email}
        role={user?.role}
        loading={loadingLogout}
        activeSection={'appointments' as any}
        collapsed={sidebarCollapsed}
        onToggleCollapse={toggleCollapsed}
        onDashboard={() => navigate('/admin')}
        onTriage={() => navigate('/triage')}
        onUsers={() => navigate('/admin/users')}
        onTriageList={() => navigate('/admin/triages')}
        onAppointments={() => navigate('/admin/appointments')}
        onLogout={() => void handleLogout()}
      />

      <main className="flex-1 min-w-0 p-5 lg:p-6 overflow-y-auto">
        <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4 mb-5">
          <div>
            <h2 className="text-2xl font-bold text-slate-900">Cola de Citas</h2>
            <p className="text-sm text-slate-600 mt-1">Vista operativa interna para seguimiento de programación y solvencia.</p>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <StatusChip label={`Total: ${total}`} tone="blue" />
            <StatusChip label={`Pago validado: ${validated}`} tone="emerald" />
            <StatusChip label={`Pago pendiente: ${pending}`} tone="amber" />
          </div>
        </div>

        {feedback && (
          <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {feedback}
          </div>
        )}

        <section className="rounded-xl border border-blue-200 bg-white shadow-sm p-4 lg:p-5">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mb-4">
            <input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Buscar por IDs, estado o método"
              className="w-full px-3 py-2.5 rounded-lg border border-blue-200 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
            />
            <select
              value={adminStateFilter}
              onChange={(event) => setAdminStateFilter(event.target.value as 'ALL' | 'PAGO_VALIDADO' | 'PAGO_PENDIENTE')}
              className="w-full px-3 py-2.5 rounded-lg border border-blue-200 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
            >
              <option value="ALL">Todos los estados administrativos</option>
              <option value="PAGO_VALIDADO">Pago validado</option>
              <option value="PAGO_PENDIENTE">Pago pendiente</option>
            </select>
            <button
              type="button"
              onClick={() => void loadAppointments(true)}
              disabled={refreshing || loadingList}
              className="px-4 py-2.5 rounded-lg bg-white hover:bg-slate-50 text-slate-700 border border-blue-200 font-semibold text-sm disabled:opacity-60"
            >
              {refreshing ? 'Actualizando...' : 'Actualizar listado'}
            </button>
          </div>

          {loadingList ? (
            <div className="rounded-xl border border-blue-100 bg-blue-50 p-6 text-sm text-slate-600">Cargando cola de citas...</div>
          ) : filteredAppointments.length === 0 ? (
            <div className="rounded-xl border border-blue-100 bg-blue-50 p-6 text-sm text-slate-600">No hay citas que coincidan con los filtros.</div>
          ) : (
            <div className="overflow-x-auto rounded-xl border border-blue-100">
              <table className="min-w-full text-xs">
                <thead className="bg-blue-50 text-slate-700">
                  <tr>
                    <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Cita</th>
                    <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Agenda</th>
                    <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Estado</th>
                    <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Resultado</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-blue-100 bg-white">
                  {filteredAppointments.map((item) => (
                    <tr key={item.citaMedicaId} className="align-top hover:bg-sky-50/60 transition">
                      <td className="px-3 py-3 text-slate-700">
                        <div className="font-semibold text-sm text-slate-900">Cita #{item.citaMedicaId}</div>
                        <div className="text-slate-600 mt-1">Paciente: {item.pacienteNombre || `Paciente #${item.pacienteId}`}</div>
                        <div className="text-slate-500 mt-1">Identificación: {item.pacienteIdentificacion || 'No registrada'}</div>
                        <div className="text-slate-500 mt-1">Transacción: {item.transaccionId || 'No disponible'}</div>
                      </td>
                      <td className="px-3 py-3 text-slate-700">
                        <div>{formatDate(item.fechaCita)}</div>
                        <div className="mt-1">{item.horaCita}</div>
                        <div className="mt-1">Especialidad: {item.especialidadNombre || `#${item.especialidadId ?? 'N/D'}`}</div>
                        <div className="mt-1">Médico: {item.medicoNombre || `#${item.medicoPersonalId}`}</div>
                        <div className="mt-1 font-semibold">Q{item.costoConsulta.toFixed(2)}</div>
                      </td>
                      <td className="px-3 py-3 text-slate-700">
                        <div className="mb-2"><StatusChip label={item.metodoPago} tone="blue" /></div>
                        <div className="mb-2"><StatusChip label={item.estadoCita} tone="slate" /></div>
                        <StatusChip label={item.estadoAdministrativo.replace('_', ' ')} tone={toneByAdminState(item.estadoAdministrativo)} />
                      </td>
                      <td className="px-3 py-3 text-slate-700 max-w-[360px]">
                        <p className="text-xs text-slate-600">{item.mensajeValidacion}</p>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </main>
    </div>
  )
}

export default AppointmentQueue


