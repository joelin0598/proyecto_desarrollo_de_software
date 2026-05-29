import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { authAPI, pharmacyAPI, type MedicineResponse, type PrescriptionResponse } from '@/services/api'
import AdminSidebar from '@/components/ui/AdminSidebar'
import useSidebarPreference from '@/hooks/useSidebarPreference'
import StatusChip from '@/components/ui/StatusChip'

const PharmacyWorkbench: React.FC = () => {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const { collapsed: sidebarCollapsed, toggleCollapsed } = useSidebarPreference('admin-pharmacy', false)
  const [loadingLogout, setLoadingLogout] = useState(false)

  const [citaDetalleId, setCitaDetalleId] = useState('')
  const [recetaDetalleId, setRecetaDetalleId] = useState('')
  const [prescription, setPrescription] = useState<PrescriptionResponse | null>(null)
  const [medicines, setMedicines] = useState<MedicineResponse[]>([])
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)

  const resetFeedback = () => {
    setError('')
    setMessage('')
  }

  const handleLoadMedicines = async () => {
    resetFeedback()
    setBusy(true)
    try {
      const { data } = await pharmacyAPI.listMedicines()
      setMedicines(data)
      setMessage(`Inventario cargado (${data.length} medicamentos activos).`)
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'No se pudo cargar inventario de farmacia.')
    } finally {
      setBusy(false)
    }
  }

  const handleLoadPrescription = async () => {
    resetFeedback()
    if (!citaDetalleId.trim()) {
      setError('Ingresa citaMedicaDetalleId para buscar la receta.')
      return
    }
    setBusy(true)
    try {
      const { data } = await pharmacyAPI.getPrescriptionByDetalle(Number(citaDetalleId))
      setPrescription(data)
      setMessage(`Receta ${data.recetaMedicaId} cargada correctamente.`)
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'No se pudo cargar la receta.')
      setPrescription(null)
    } finally {
      setBusy(false)
    }
  }

  const handleDispense = async () => {
    resetFeedback()
    if (!recetaDetalleId.trim()) {
      setError('Ingresa recetaMedicaDetalleId para despachar medicamento.')
      return
    }
    setBusy(true)
    try {
      const { data } = await pharmacyAPI.dispense({ recetaMedicaDetalleId: Number(recetaDetalleId) })
      setPrescription(data)
      setMessage('Despacho registrado correctamente y recordatorio programado (si aplica).')
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'No se pudo completar el despacho.')
    } finally {
      setBusy(false)
    }
  }

  const handleLogout = async () => {
    setLoadingLogout(true)
    try {
      await authAPI.logout()
    } catch {
      // Ignorar error de logout remoto y limpiar sesión local.
    } finally {
      logout()
      navigate('/')
      setLoadingLogout(false)
    }
  }

  return (
    <div className="h-screen bg-gradient-to-br from-blue-100 via-sky-50 to-blue-100 text-slate-800 flex overflow-hidden">
      <AdminSidebar
        email={user?.email}
        role={user?.role}
        loading={loadingLogout}
        activeSection="dashboard"
        collapsed={sidebarCollapsed}
        showSessionDetails
        onToggleCollapse={toggleCollapsed}
        onDashboard={() => navigate('/admin')}
        onTriage={() => navigate('/triage')}
        onUsers={() => navigate('/admin/users')}
        onTriageList={() => navigate('/admin/triages')}
        onAppointments={() => navigate('/admin/appointments')}
        onConsultation={() => navigate('/doctor/appointments/attention')}
        onLogout={() => void handleLogout()}
      />

      <main className="flex-1 p-5 lg:p-6 overflow-y-auto">
        <div className="flex items-start justify-between gap-4 mb-6">
          <div>
            <h2 className="text-2xl font-bold text-slate-900">Farmacia · CU08</h2>
            <p className="text-sm text-slate-600 mt-1">Consulta de receta, validación de inventario y despacho de medicamento.</p>
          </div>
          <StatusChip label="CU08 activo" tone="emerald" />
        </div>

        {error && <div className="mb-4 rounded-lg border border-red-300 bg-red-100 px-4 py-3 text-sm text-red-700">{error}</div>}
        {message && <div className="mb-4 rounded-lg border border-emerald-300 bg-emerald-100 px-4 py-3 text-sm text-emerald-700">{message}</div>}

        <section className="grid grid-cols-1 xl:grid-cols-2 gap-5">
          <div className="rounded-xl border border-blue-200 bg-white p-5 shadow-sm space-y-3">
            <h3 className="font-semibold text-slate-900">1) Buscar receta activa</h3>
            <div className="flex gap-2">
              <input
                value={citaDetalleId}
                onChange={(e) => setCitaDetalleId(e.target.value)}
                placeholder="citaMedicaDetalleId"
                className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm"
              />
              <button type="button" onClick={() => void handleLoadPrescription()} disabled={busy} className="px-3 py-2 rounded-lg bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 disabled:opacity-60">
                Buscar
              </button>
            </div>
          </div>

          <div className="rounded-xl border border-blue-200 bg-white p-5 shadow-sm space-y-3">
            <h3 className="font-semibold text-slate-900">2) Despachar medicamento</h3>
            <div className="flex gap-2">
              <input
                value={recetaDetalleId}
                onChange={(e) => setRecetaDetalleId(e.target.value)}
                placeholder="recetaMedicaDetalleId"
                className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm"
              />
              <button type="button" onClick={() => void handleDispense()} disabled={busy} className="px-3 py-2 rounded-lg bg-emerald-600 text-white text-sm font-semibold hover:bg-emerald-700 disabled:opacity-60">
                Despachar
              </button>
            </div>
            <button type="button" onClick={() => void handleLoadMedicines()} disabled={busy} className="px-3 py-2 rounded-lg border border-blue-300 text-blue-700 text-sm hover:bg-blue-50 disabled:opacity-60">
              Cargar inventario
            </button>
          </div>
        </section>

        {prescription && (
          <section className="mt-5 rounded-xl border border-blue-200 bg-white p-5 shadow-sm">
            <h3 className="font-semibold text-slate-900">Receta activa</h3>
            <p className="text-sm text-slate-700 mt-2">Receta ID: {prescription.recetaMedicaId} · Fecha emisión: {prescription.fechaEmision}</p>
            <div className="mt-3 overflow-x-auto">
              <table className="min-w-full text-sm">
                <thead>
                  <tr className="text-left text-slate-600 border-b border-slate-200">
                    <th className="py-2 pr-4">Detalle ID</th>
                    <th className="py-2 pr-4">Medicamento</th>
                    <th className="py-2 pr-4">Cantidad</th>
                    <th className="py-2 pr-4">Frecuencia</th>
                    <th className="py-2 pr-4">Duración</th>
                    <th className="py-2 pr-4">Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {prescription.items.map((item) => (
                    <tr key={item.recetaMedicaDetalleId} className="border-b border-slate-100">
                      <td className="py-2 pr-4">{item.recetaMedicaDetalleId}</td>
                      <td className="py-2 pr-4">{item.medicamentoNombre || item.medicamentoId}</td>
                      <td className="py-2 pr-4">{item.cantidad}</td>
                      <td className="py-2 pr-4">{item.frecuenciaHoras ?? 'N/D'}h</td>
                      <td className="py-2 pr-4">{item.duracionDias ?? 'N/D'} días</td>
                      <td className="py-2 pr-4">{item.despachado ? 'Atendida' : 'Pendiente'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        )}

        {medicines.length > 0 && (
          <section className="mt-5 rounded-xl border border-blue-200 bg-white p-5 shadow-sm">
            <h3 className="font-semibold text-slate-900">Inventario activo</h3>
            <div className="mt-3 overflow-x-auto">
              <table className="min-w-full text-sm">
                <thead>
                  <tr className="text-left text-slate-600 border-b border-slate-200">
                    <th className="py-2 pr-4">ID</th>
                    <th className="py-2 pr-4">Medicamento</th>
                    <th className="py-2 pr-4">Presentación</th>
                    <th className="py-2 pr-4">Stock</th>
                  </tr>
                </thead>
                <tbody>
                  {medicines.map((medicine) => (
                    <tr key={medicine.medicamentoId} className="border-b border-slate-100">
                      <td className="py-2 pr-4">{medicine.medicamentoId}</td>
                      <td className="py-2 pr-4">{medicine.nombre}</td>
                      <td className="py-2 pr-4">{medicine.presentacion || 'N/D'}</td>
                      <td className="py-2 pr-4">{medicine.stockActual}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        )}
      </main>
    </div>
  )
}

export default PharmacyWorkbench

