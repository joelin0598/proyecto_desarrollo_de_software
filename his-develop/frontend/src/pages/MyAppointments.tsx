import React from 'react'
import { Link } from 'react-router-dom'
import StatusChip from '@/components/ui/StatusChip'
import { appointmentAPI, type ScheduleAppointmentResponse } from '@/services/api'

const formatDate = (value: string) => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('es-GT', { dateStyle: 'medium' }).format(date)
}

const appointmentTone = (item: ScheduleAppointmentResponse) => {
  if (item.estadoAdministrativo === 'PAGO_VALIDADO') return 'emerald' as const
  return 'amber' as const
}

const buildQrImageUrl = (qrContenido?: string | null) => {
  if (!qrContenido) return null
  return `https://api.qrserver.com/v1/create-qr-code/?size=140x140&data=${encodeURIComponent(qrContenido)}`
}

const MyAppointments: React.FC = () => {
  const [loadingList, setLoadingList] = React.useState(true)
  const [refreshing, setRefreshing] = React.useState(false)
  const [feedback, setFeedback] = React.useState<string | null>(null)
  const [appointments, setAppointments] = React.useState<ScheduleAppointmentResponse[]>([])
  const [search, setSearch] = React.useState('')

  const loadAppointments = React.useCallback(async (showRefreshing = false) => {
    if (showRefreshing) setRefreshing(true)
    else setLoadingList(true)

    try {
      const response = await appointmentAPI.list()
      setAppointments(response.data)
      setFeedback(null)
    } catch (error: any) {
      const msg = error?.response?.data?.errorMessage || 'No se pudo cargar el listado de citas.'
      setFeedback(msg)
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
    if (!normalized) return appointments

    return appointments.filter((item) => {
      const blob = [
        item.citaMedicaId,
        item.codigoCita,
        item.metodoPago,
        item.estadoAdministrativo,
        item.estadoCita,
        item.mensajeValidacion,
      ].join(' ').toLowerCase()
      return blob.includes(normalized)
    })
  }, [appointments, search])

  const downloadSupport = (item: ScheduleAppointmentResponse) => {
    const lines = [
      `Cita ID: ${item.citaMedicaId}`,
      `Codigo: ${item.codigoCita || `CITA-${item.citaMedicaId}`}`,
      `Fecha: ${item.fechaCita}`,
      `Hora: ${item.horaCita}`,
      `Paciente: ${item.pacienteNombre || `ID ${item.pacienteId}`}`,
      `DPI paciente: ${item.pacienteIdentificacion || 'N/D'}`,
      `Medico ID: ${item.medicoPersonalId}`,
      `QR: ${item.qrContenido || 'N/D'}`,
    ]

    const blob = new Blob([lines.join('\n')], { type: 'text/plain;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = `soporte-cita-${item.citaMedicaId}.txt`
    anchor.click()
    URL.revokeObjectURL(url)
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-100 via-sky-50 to-blue-100 text-slate-800">
      <main className="max-w-6xl mx-auto px-4 sm:px-6 py-6">
        <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4 mb-5">
          <div>
            <p className="text-xs uppercase tracking-[0.2em] text-slate-400">Portal Paciente</p>
            <h2 className="text-2xl font-bold text-slate-900 mt-1">Mis Citas</h2>
            <p className="text-sm text-slate-600 mt-1">Consulta y seguimiento de tus citas registradas.</p>
          </div>
          <div className="flex items-center gap-2">
            <Link to="/portal" className="px-4 py-2 rounded-lg border border-blue-200 bg-white hover:bg-slate-50 text-slate-700 text-sm font-semibold">Volver al portal</Link>
            <Link to="/portal/appointments" className="px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold">Agendar cita</Link>
          </div>
        </div>

        {feedback && (
          <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {feedback}
          </div>
        )}

        <section className="rounded-xl border border-blue-200 bg-white shadow-sm p-4 lg:p-5">
          <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-3 mb-4">
            <div>
              <h3 className="text-lg font-bold text-slate-900">Citas registradas</h3>
              <p className="text-sm text-slate-600 mt-1">Solo se muestran citas asociadas a tu usuario.</p>
            </div>
            <div className="flex items-center gap-2">
              <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Buscar por IDs o estado" className="px-3 py-2 rounded-lg border border-blue-200 bg-white text-sm focus:ring-2 focus:ring-blue-500 focus:outline-none" />
              <button type="button" onClick={() => void loadAppointments(true)} disabled={refreshing || loadingList} className="px-4 py-2 rounded-lg bg-white hover:bg-slate-50 text-slate-700 border border-blue-200 font-semibold text-sm disabled:opacity-60">
                {refreshing ? 'Actualizando...' : 'Actualizar'}
              </button>
            </div>
          </div>

          {loadingList ? (
            <div className="rounded-xl border border-blue-100 bg-blue-50 p-6 text-sm text-slate-600">Cargando citas...</div>
          ) : filteredAppointments.length === 0 ? (
            <div className="rounded-xl border border-blue-100 bg-blue-50 p-6 text-sm text-slate-600">No hay citas para mostrar.</div>
          ) : (
            <div className="overflow-x-auto rounded-xl border border-blue-100">
              <table className="min-w-full text-xs">
                <thead className="bg-blue-50 text-slate-700">
                  <tr>
                    <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Cita</th>
                    <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Agenda</th>
                    <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Pago</th>
                    <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Resultado</th>
                    <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Soporte</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-blue-100 bg-white">
                  {filteredAppointments.map((item) => (
                    <tr key={item.citaMedicaId} className="align-top hover:bg-sky-50/60 transition">
                      <td className="px-3 py-3 text-slate-700">
                        <div className="font-semibold text-sm text-slate-900">Cita #{item.citaMedicaId}</div>
                        <div className="text-slate-500 mt-1">Paciente: {item.pacienteNombre || `ID ${item.pacienteId}`}</div>
                        <div className="text-slate-500 mt-1">Médico ID: {item.medicoPersonalId}</div>
                      </td>
                      <td className="px-3 py-3 text-slate-700">
                        <div>{formatDate(item.fechaCita)}</div>
                        <div className="mt-1">{item.horaCita}</div>
                        <div className="mt-1">Q{item.costoConsulta.toFixed(2)}</div>
                      </td>
                      <td className="px-3 py-3 text-slate-700">
                        <div className="mb-2"><StatusChip label={item.metodoPago} tone="blue" /></div>
                        <StatusChip label={item.estadoAdministrativo.replace('_', ' ')} tone={appointmentTone(item)} />
                      </td>
                      <td className="px-3 py-3 text-slate-700">
                        <div className="mb-2"><StatusChip label={item.estadoCita} tone="slate" /></div>
                        <p className="text-xs text-slate-600 max-w-[320px]">{item.mensajeValidacion}</p>
                      </td>
                      <td className="px-3 py-3 text-slate-700">
                        <div className="text-xs text-slate-600">ID: {item.citaMedicaId}</div>
                        <div className="text-xs text-slate-600 mt-1">Codigo: {item.codigoCita || `CITA-${item.citaMedicaId}`}</div>
                        {buildQrImageUrl(item.qrContenido) && (
                          <img src={buildQrImageUrl(item.qrContenido) || ''} alt={`QR cita ${item.citaMedicaId}`} className="mt-2 h-20 w-20 border border-blue-100 rounded" />
                        )}
                        <button
                          type="button"
                          onClick={() => downloadSupport(item)}
                          className="mt-2 px-2.5 py-1.5 rounded border border-blue-200 bg-white hover:bg-blue-50 text-xs font-semibold"
                        >
                          Descargar soporte
                        </button>
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

export default MyAppointments
