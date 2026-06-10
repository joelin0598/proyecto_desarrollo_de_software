import React from 'react'
import { useNavigate } from 'react-router-dom'
import AdminSidebar from '@/components/ui/AdminSidebar'
import StatusChip from '@/components/ui/StatusChip'
import {
  appointmentAttentionAPI,
  authAPI,
  type MedicalAppointmentAttentionResponse,
  type MedicalAppointmentQueueItemResponse,
} from '@/services/api'
import { useAuth } from '@/context/AuthContext'
import useSidebarPreference from '@/hooks/useSidebarPreference'

const formatDate = (value?: string | null) => {
  if (!value) return 'N/D'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('es-GT', { dateStyle: 'medium' }).format(date)
}

const priorityBadgeClass = (priority?: string | null) => {
  switch ((priority || '').toUpperCase()) {
    case 'ROJO':
      return 'bg-red-100 text-red-800 border-red-200'
    case 'NARANJA':
      return 'bg-orange-100 text-orange-800 border-orange-200'
    case 'AMARILLO':
      return 'bg-yellow-100 text-yellow-800 border-yellow-200'
    case 'VERDE':
      return 'bg-emerald-100 text-emerald-800 border-emerald-200'
    default:
      return 'bg-slate-100 text-slate-700 border-slate-200'
  }
}

const AppointmentAttentionWorkspace: React.FC = () => {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const { collapsed: sidebarCollapsed, toggleCollapsed } = useSidebarPreference('admin-shell', false)

  const lastAnnouncedCitaIdRef = React.useRef<number | null>(null)
  const calloutTimerRef = React.useRef<number | null>(null)

  const [loadingLogout, setLoadingLogout] = React.useState(false)
  const [loadingData, setLoadingData] = React.useState(true)
  const [loadingAction, setLoadingAction] = React.useState(false)
  const [feedback, setFeedback] = React.useState<string | null>(null)
  const [queue, setQueue] = React.useState<MedicalAppointmentQueueItemResponse[]>([])
  const [currentAttention, setCurrentAttention] = React.useState<MedicalAppointmentAttentionResponse | null>(null)
  const [activeCallout, setActiveCallout] = React.useState<string | null>(null)

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

  const syncData = React.useCallback(async () => {
    setLoadingData(true)
    try {
      const [queueRes, currentRes] = await Promise.all([
        appointmentAttentionAPI.queue(),
        appointmentAttentionAPI.current(),
      ])

      setQueue(queueRes.data)
      setCurrentAttention(currentRes.status === 204 ? null : currentRes.data)
      setFeedback(null)
    } catch (error: any) {
      const message = error?.response?.data?.errorMessage || 'No se pudo cargar la estación de atención.'
      setFeedback(message)
    } finally {
      setLoadingData(false)
    }
  }, [])

  React.useEffect(() => {
    void syncData()
  }, [syncData])

  React.useEffect(() => {
    return () => {
      if (calloutTimerRef.current !== null) {
        window.clearTimeout(calloutTimerRef.current)
      }
    }
  }, [])

  const announceAttentionCall = React.useCallback((item: MedicalAppointmentQueueItemResponse) => {
    if (lastAnnouncedCitaIdRef.current === item.citaMedicaId) {
      return
    }

    const doctorName = `${user?.firstName || ''} ${user?.lastName || ''}`.trim() || user?.email || 'médico asignado'
    const clinicName = (item.especialidadNombre || 'Consulta general').trim()
    const patientName = (item.pacienteNombre || 'Paciente').trim()

    const message = `Paciente ${patientName}, pasar con el doctor ${doctorName} a la clínica ${clinicName}.`

    lastAnnouncedCitaIdRef.current = item.citaMedicaId
    setActiveCallout(message)

    if (calloutTimerRef.current !== null) {
      window.clearTimeout(calloutTimerRef.current)
    }
    calloutTimerRef.current = window.setTimeout(() => {
      setActiveCallout(null)
      calloutTimerRef.current = null
    }, 10000)

    if (typeof window === 'undefined' || !('speechSynthesis' in window)) {
      console.warn('speechSynthesis no disponible en este navegador.')
      return
    }

    try {
      window.speechSynthesis.cancel()
      const utterance = new SpeechSynthesisUtterance(message)
      utterance.lang = 'es-GT'
      utterance.rate = 1
      utterance.pitch = 1
      utterance.volume = 1
      utterance.onerror = () => {
        console.warn('No fue posible reproducir el llamado por voz (bloqueo o error del navegador).')
      }
      window.speechSynthesis.speak(utterance)
    } catch {
      console.warn('No fue posible inicializar speechSynthesis para el llamado de paciente.')
    }
  }, [user?.email, user?.firstName, user?.lastName])

  const openAttention = async (item: MedicalAppointmentQueueItemResponse) => {
    setLoadingAction(true)
    try {
      await appointmentAttentionAPI.open(item.citaMedicaId)
      announceAttentionCall(item)
      setFeedback('Atención iniciada. Redirigiendo a la vista clínica...')
      navigate('/doctor/appointments/attention/current')
    } catch (error: any) {
      const message = error?.response?.data?.errorMessage || 'No se pudo iniciar la atención.'
      setFeedback(message)
    } finally {
      setLoadingAction(false)
    }
  }

  return (
    <div className="h-screen bg-slate-100 text-slate-800 flex overflow-hidden">
      <AdminSidebar
        email={user?.email}
        role={user?.role}
        loading={loadingLogout}
        activeSection="consultation"
        collapsed={sidebarCollapsed}
        onToggleCollapse={toggleCollapsed}
        onDashboard={() => navigate('/admin')}
        onTriage={() => navigate('/triage')}
        onUsers={() => navigate('/admin/users')}
        onTriageList={() => navigate('/admin/triages')}
        onAppointments={() => navigate('/admin/appointments')}
        onConsultation={() => navigate('/doctor/appointments/attention')}
        onLogout={() => void handleLogout()}
      />

      <main className="flex-1 min-w-0 p-4 lg:p-5 overflow-y-auto">
        <div className="flex items-start justify-between gap-4 mb-4">
          <div>
            <h2 className="text-2xl font-bold text-slate-900">Cola de Atención Clínica</h2>
            <p className="text-sm text-slate-600 mt-1">Listado de pacientes listos para atención médica. Usa Iniciar para abrir la vista de atención en curso.</p>
          </div>
          <StatusChip label={currentAttention ? 'Atención en curso' : 'Sin atención activa'} tone={currentAttention ? 'emerald' : 'slate'} />
        </div>

        {activeCallout && (
          <div className="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-900">
            <p className="font-semibold">Llamado en curso</p>
            <p className="mt-1">{activeCallout}</p>
          </div>
        )}

        {feedback && (
          <div className="mb-4 rounded-lg border border-blue-200 bg-blue-50 px-4 py-3 text-sm text-blue-900">
            {feedback}
          </div>
        )}

        {loadingData ? (
          <div className="rounded-xl border border-blue-100 bg-blue-50 p-6 text-sm text-slate-600">Cargando datos clínicos...</div>
        ) : (
          <section className="rounded-xl border border-slate-300 bg-white shadow-sm p-4">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-lg font-semibold text-slate-900">Pacientes en espera</h3>
              <div className="flex items-center gap-2">
                {currentAttention && (
                  <button
                    type="button"
                    onClick={() => navigate('/doctor/appointments/attention/current')}
                    className="px-3 py-2 rounded-lg border border-emerald-300 bg-emerald-50 hover:bg-emerald-100 text-emerald-800 font-semibold text-xs"
                  >
                    Ir a atención en curso
                  </button>
                )}
                <button
                  type="button"
                  onClick={() => void syncData()}
                  disabled={loadingAction}
                  className="px-3 py-2 rounded-lg bg-white hover:bg-slate-50 text-slate-700 border border-slate-300 font-semibold text-xs disabled:opacity-60"
                >
                  Actualizar
                </button>
              </div>
            </div>

            {queue.length === 0 ? (
              <div className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-4 text-sm text-slate-600 mt-3">No hay pacientes pendientes con pago validado.</div>
            ) : (
              <div className="mt-3 overflow-x-auto rounded-lg border border-slate-200">
                <table className="min-w-full text-xs">
                  <thead className="bg-slate-100 text-slate-600 uppercase tracking-wide">
                    <tr>
                      <th className="px-3 py-2 text-left">Paciente</th>
                      <th className="px-3 py-2 text-left">DPI</th>
                      <th className="px-3 py-2 text-left">Prioridad</th>
                      <th className="px-3 py-2 text-left">Alerta</th>
                      <th className="px-3 py-2 text-left">Signos vitales</th>
                      <th className="px-3 py-2 text-left">Tipo</th>
                      <th className="px-3 py-2 text-left">Cita</th>
                      <th className="px-3 py-2 text-right">Acción</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-200 bg-white">
                    {queue.map((item) => (
                      <tr key={item.citaMedicaId} className="hover:bg-slate-50 align-top">
                        <td className="px-3 py-2.5 font-semibold text-slate-900">{item.pacienteNombre}</td>
                        <td className="px-3 py-2.5 text-slate-700">{item.pacienteDpi || 'N/D'}</td>
                        <td className="px-3 py-2.5">
                          <span className={`inline-flex rounded border px-2 py-0.5 font-semibold ${priorityBadgeClass(item.prioridad)}`}>
                            {item.prioridad || 'SIN_TRIAJE'}
                          </span>
                        </td>
                        <td className="px-3 py-2.5">
                          {item.alertaEmergencia ? (
                            <span className="inline-flex rounded border border-red-200 bg-red-100 px-2 py-0.5 font-semibold text-red-800">ROJA</span>
                          ) : (
                            <span className="inline-flex rounded border border-emerald-200 bg-emerald-100 px-2 py-0.5 font-semibold text-emerald-800">NORMAL</span>
                          )}
                        </td>
                        <td className="px-3 py-2.5 text-slate-700">
                          PA {item.presionSistolica ?? '-'} / {item.presionDiastolica ?? '-'}
                          <br />
                          FC {item.frecuenciaCardiaca ?? '-'} · T {item.temperatura ?? '-'} C · SpO2 {item.saturacionOxigeno ?? '-'}%
                        </td>
                        <td className="px-3 py-2.5 text-slate-700">
                          {item.tipoAtencion === 'CITA_PROGRAMADA' ? 'Cita Programada' : 'Sin cita previa'}
                        </td>
                        <td className="px-3 py-2.5 text-slate-700">
                          #{item.citaMedicaId}
                          <br />
                          {formatDate(item.fechaCita)} {item.horaCita || ''}
                        </td>
                        <td className="px-3 py-2.5 text-right">
                          <button
                            type="button"
                            onClick={() => void openAttention(item)}
                            disabled={loadingAction || !!currentAttention}
                            className="px-3 py-1.5 rounded-lg bg-slate-900 hover:bg-slate-800 text-white font-semibold disabled:opacity-60"
                          >
                            Iniciar
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        )}
      </main>
    </div>
  )
}

export default AppointmentAttentionWorkspace

