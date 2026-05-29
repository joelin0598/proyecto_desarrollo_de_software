import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { pharmacyAPI, type MedicationReminderResponse } from '@/services/api'
import StatusChip from '@/components/ui/StatusChip'

const PatientReminders: React.FC = () => {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reminders, setReminders] = useState<MedicationReminderResponse[]>([])

  useEffect(() => {
    const loadReminders = async () => {
      setLoading(true)
      setError('')
      try {
        const { data } = await pharmacyAPI.getMyReminders()
        setReminders(data)
      } catch (err: any) {
        setError(err.response?.data?.errorMessage || 'No se pudieron cargar los recordatorios de medicamentos.')
      } finally {
        setLoading(false)
      }
    }

    void loadReminders()
  }, [])

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-100 via-sky-50 to-blue-100 p-5 lg:p-6 text-slate-800">
      <div className="max-w-5xl mx-auto">
        <div className="flex items-start justify-between gap-4 mb-5">
          <div>
            <h2 className="text-2xl font-bold text-slate-900">Recordatorios de tratamiento · CU08</h2>
            <p className="text-sm text-slate-600 mt-1">Seguimiento de medicamentos despachados en farmacia.</p>
          </div>
          <StatusChip label="Portal paciente" tone="emerald" />
        </div>

        {loading && <div className="rounded-lg border border-blue-200 bg-white p-4 text-sm">Cargando recordatorios...</div>}
        {error && <div className="rounded-lg border border-red-300 bg-red-100 p-4 text-sm text-red-700">{error}</div>}

        {!loading && !error && reminders.length === 0 && (
          <div className="rounded-lg border border-amber-300 bg-amber-50 p-4 text-sm text-amber-800">
            No hay recordatorios activos por ahora.
          </div>
        )}

        {!loading && !error && reminders.length > 0 && (
          <section className="rounded-xl border border-blue-200 bg-white shadow-sm p-4 overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="text-left text-slate-600 border-b border-slate-200">
                  <th className="py-2 pr-4">Medicamento</th>
                  <th className="py-2 pr-4">Dosis</th>
                  <th className="py-2 pr-4">Frecuencia</th>
                  <th className="py-2 pr-4">Duración</th>
                  <th className="py-2 pr-4">Vía</th>
                  <th className="py-2 pr-4">Próximo recordatorio</th>
                  <th className="py-2 pr-4">Estado</th>
                </tr>
              </thead>
              <tbody>
                {reminders.map((item) => (
                  <tr key={item.recordatorioId} className="border-b border-slate-100">
                    <td className="py-2 pr-4">{item.medicamentoNombre}</td>
                    <td className="py-2 pr-4">{item.dosis || 'N/D'}</td>
                    <td className="py-2 pr-4">{item.frecuenciaHoras ? `${item.frecuenciaHoras}h` : 'N/D'}</td>
                    <td className="py-2 pr-4">{item.duracionDias ? `${item.duracionDias} días` : 'N/D'}</td>
                    <td className="py-2 pr-4">{item.viaAdministracion || 'N/D'}</td>
                    <td className="py-2 pr-4">{item.proximoRecordatorio || 'N/D'}</td>
                    <td className="py-2 pr-4">{item.activo ? 'Activo' : 'Inactivo'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>
        )}

        <div className="mt-4">
          <Link to="/portal" className="text-sm font-semibold text-blue-700 hover:text-blue-800">
            ← Volver al portal
          </Link>
        </div>
      </div>
    </div>
  )
}

export default PatientReminders

