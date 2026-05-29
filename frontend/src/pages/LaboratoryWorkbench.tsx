import React, { useState } from 'react'
import AdminSidebar from '@/components/ui/AdminSidebar'
import useSidebarPreference from '@/hooks/useSidebarPreference'
import StatusChip from '@/components/ui/StatusChip'
import { useAuth } from '@/context/AuthContext'
import { authAPI, laboratoryAPI, type LaboratoryOrderResponse } from '@/services/api'
import { useNavigate } from 'react-router-dom'

const LaboratoryWorkbench: React.FC = () => {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const { collapsed: sidebarCollapsed, toggleCollapsed } = useSidebarPreference('admin-laboratory', false)
  const [loadingLogout, setLoadingLogout] = useState(false)

  const [createPayload, setCreatePayload] = useState({
    citaMedicaDetalleId: '',
    nombreExamen: '',
    tipoMuestra: '',
  })

  const [resultPayload, setResultPayload] = useState({
    ordenLaboratorioId: '',
    nombreExamen: '',
    valorResultado: '',
    unidadResultado: '',
    referenciaMinima: '',
    referenciaMaxima: '',
    observaciones: '',
    resumen: '',
    conclusion: '',
  })

  const [orderIdInput, setOrderIdInput] = useState('')
  const [rejectReason, setRejectReason] = useState('')
  const [currentOrder, setCurrentOrder] = useState<LaboratoryOrderResponse | null>(null)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)

  const resetFeedback = () => {
    setError('')
    setMessage('')
  }

  const parseOptionalNumber = (value: string): number | undefined => {
    if (!value.trim()) return undefined
    const parsed = Number(value)
    return Number.isNaN(parsed) ? undefined : parsed
  }

  const handleCreateOrder = async (e: React.FormEvent) => {
    e.preventDefault()
    resetFeedback()
    setBusy(true)
    try {
      const { data } = await laboratoryAPI.createOrder({
        citaMedicaDetalleId: Number(createPayload.citaMedicaDetalleId),
        nombreExamen: createPayload.nombreExamen.trim(),
        tipoMuestra: createPayload.tipoMuestra.trim() || undefined,
      })
      setCurrentOrder(data)
      setOrderIdInput(String(data.ordenLaboratorioId))
      setMessage(`Orden creada correctamente (ID ${data.ordenLaboratorioId}).`)
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'No se pudo crear la orden de laboratorio.')
    } finally {
      setBusy(false)
    }
  }

  const handleGetOrder = async () => {
    resetFeedback()
    if (!orderIdInput.trim()) {
      setError('Ingresa un ordenLaboratorioId para consultar.')
      return
    }
    setBusy(true)
    try {
      const { data } = await laboratoryAPI.getOrder(Number(orderIdInput))
      setCurrentOrder(data)
      setMessage(`Orden ${data.ordenLaboratorioId} cargada.`)
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'No se pudo consultar la orden.')
      setCurrentOrder(null)
    } finally {
      setBusy(false)
    }
  }

  const handleReceive = async () => {
    resetFeedback()
    if (!orderIdInput.trim()) {
      setError('Ingresa un ordenLaboratorioId antes de recibir muestra.')
      return
    }
    setBusy(true)
    try {
      const { data } = await laboratoryAPI.receiveSample(Number(orderIdInput))
      setCurrentOrder(data)
      setMessage(`Muestra recibida. Estado actual: ${data.estado}.`)
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'No se pudo recibir la muestra.')
    } finally {
      setBusy(false)
    }
  }

  const handleReject = async () => {
    resetFeedback()
    if (!orderIdInput.trim()) {
      setError('Ingresa un ordenLaboratorioId antes de rechazar muestra.')
      return
    }
    if (!rejectReason.trim()) {
      setError('Ingresa un motivo de rechazo.')
      return
    }
    setBusy(true)
    try {
      const { data } = await laboratoryAPI.rejectSample(Number(orderIdInput), rejectReason.trim())
      setCurrentOrder(data)
      setMessage(`Muestra rechazada. Estado actual: ${data.estado}.`)
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'No se pudo rechazar la muestra.')
    } finally {
      setBusy(false)
    }
  }

  const handleAddResult = async (e: React.FormEvent) => {
    e.preventDefault()
    resetFeedback()
    setBusy(true)
    try {
      const { data } = await laboratoryAPI.addResult({
        ordenLaboratorioId: Number(resultPayload.ordenLaboratorioId),
        nombreExamen: resultPayload.nombreExamen.trim(),
        valorResultado: parseOptionalNumber(resultPayload.valorResultado),
        unidadResultado: resultPayload.unidadResultado.trim() || undefined,
        referenciaMinima: parseOptionalNumber(resultPayload.referenciaMinima),
        referenciaMaxima: parseOptionalNumber(resultPayload.referenciaMaxima),
        observaciones: resultPayload.observaciones.trim() || undefined,
        resumen: resultPayload.resumen.trim() || undefined,
        conclusion: resultPayload.conclusion.trim(),
      })
      setCurrentOrder(data)
      setOrderIdInput(String(data.ordenLaboratorioId))
      setMessage(`Resultado registrado. Estado actual: ${data.estado}.`)
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'No se pudo registrar el resultado.')
    } finally {
      setBusy(false)
    }
  }

  const handleLogout = async () => {
    setLoadingLogout(true)
    try {
      await authAPI.logout()
    } catch {
      // Ignorar fallo de logout remoto y limpiar sesión local.
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
            <h2 className="text-2xl font-bold text-slate-900">Laboratorio · CU07</h2>
            <p className="text-sm text-slate-600 mt-1">Recepción de muestra, rechazo técnico y publicación de resultados.</p>
          </div>
          <StatusChip label="CU07 activo" tone="blue" />
        </div>

        {error && <div className="mb-4 rounded-lg border border-red-300 bg-red-100 px-4 py-3 text-sm text-red-700">{error}</div>}
        {message && <div className="mb-4 rounded-lg border border-emerald-300 bg-emerald-100 px-4 py-3 text-sm text-emerald-700">{message}</div>}

        <section className="grid grid-cols-1 xl:grid-cols-2 gap-5">
          <form onSubmit={handleCreateOrder} className="rounded-xl border border-blue-200 bg-white p-5 shadow-sm space-y-3">
            <h3 className="font-semibold text-slate-900">1) Crear orden de laboratorio</h3>
            <input
              value={createPayload.citaMedicaDetalleId}
              onChange={(e) => setCreatePayload((prev) => ({ ...prev, citaMedicaDetalleId: e.target.value }))}
              placeholder="citaMedicaDetalleId"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
              required
            />
            <input
              value={createPayload.nombreExamen}
              onChange={(e) => setCreatePayload((prev) => ({ ...prev, nombreExamen: e.target.value }))}
              placeholder="Nombre del examen"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
              required
            />
            <input
              value={createPayload.tipoMuestra}
              onChange={(e) => setCreatePayload((prev) => ({ ...prev, tipoMuestra: e.target.value }))}
              placeholder="Tipo de muestra (opcional)"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
            />
            <button type="submit" disabled={busy} className="px-4 py-2 rounded-lg bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 disabled:opacity-60">
              Crear orden
            </button>
          </form>

          <div className="rounded-xl border border-blue-200 bg-white p-5 shadow-sm space-y-3">
            <h3 className="font-semibold text-slate-900">2) Gestionar muestra</h3>
            <div className="flex gap-2">
              <input
                value={orderIdInput}
                onChange={(e) => setOrderIdInput(e.target.value)}
                placeholder="ordenLaboratorioId"
                className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm"
              />
              <button type="button" onClick={() => void handleGetOrder()} disabled={busy} className="px-3 py-2 rounded-lg border border-blue-300 text-blue-700 text-sm hover:bg-blue-50 disabled:opacity-60">
                Consultar
              </button>
            </div>
            <div className="flex flex-wrap gap-2">
              <button type="button" onClick={() => void handleReceive()} disabled={busy} className="px-3 py-2 rounded-lg bg-emerald-600 text-white text-sm font-semibold hover:bg-emerald-700 disabled:opacity-60">
                Recibir muestra
              </button>
              <input
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                placeholder="Motivo rechazo"
                className="px-3 py-2 border border-gray-300 rounded-lg text-sm min-w-[220px]"
              />
              <button type="button" onClick={() => void handleReject()} disabled={busy} className="px-3 py-2 rounded-lg bg-amber-600 text-white text-sm font-semibold hover:bg-amber-700 disabled:opacity-60">
                Rechazar muestra
              </button>
            </div>
          </div>

          <form onSubmit={handleAddResult} className="rounded-xl border border-blue-200 bg-white p-5 shadow-sm space-y-3 xl:col-span-2">
            <h3 className="font-semibold text-slate-900">3) Registrar resultado</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <input value={resultPayload.ordenLaboratorioId} onChange={(e) => setResultPayload((p) => ({ ...p, ordenLaboratorioId: e.target.value }))} placeholder="ordenLaboratorioId" className="px-3 py-2 border border-gray-300 rounded-lg text-sm" required />
              <input value={resultPayload.nombreExamen} onChange={(e) => setResultPayload((p) => ({ ...p, nombreExamen: e.target.value }))} placeholder="Nombre examen" className="px-3 py-2 border border-gray-300 rounded-lg text-sm" required />
              <input value={resultPayload.conclusion} onChange={(e) => setResultPayload((p) => ({ ...p, conclusion: e.target.value }))} placeholder="Conclusión" className="px-3 py-2 border border-gray-300 rounded-lg text-sm" required />
              <input value={resultPayload.valorResultado} onChange={(e) => setResultPayload((p) => ({ ...p, valorResultado: e.target.value }))} placeholder="Valor resultado (opcional)" className="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              <input value={resultPayload.referenciaMinima} onChange={(e) => setResultPayload((p) => ({ ...p, referenciaMinima: e.target.value }))} placeholder="Referencia mínima" className="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              <input value={resultPayload.referenciaMaxima} onChange={(e) => setResultPayload((p) => ({ ...p, referenciaMaxima: e.target.value }))} placeholder="Referencia máxima" className="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              <input value={resultPayload.unidadResultado} onChange={(e) => setResultPayload((p) => ({ ...p, unidadResultado: e.target.value }))} placeholder="Unidad" className="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              <input value={resultPayload.resumen} onChange={(e) => setResultPayload((p) => ({ ...p, resumen: e.target.value }))} placeholder="Resumen" className="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              <input value={resultPayload.observaciones} onChange={(e) => setResultPayload((p) => ({ ...p, observaciones: e.target.value }))} placeholder="Observaciones" className="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
            </div>
            <button type="submit" disabled={busy} className="px-4 py-2 rounded-lg bg-violet-600 text-white text-sm font-semibold hover:bg-violet-700 disabled:opacity-60">
              Guardar resultado
            </button>
          </form>
        </section>

        {currentOrder && (
          <section className="mt-5 rounded-xl border border-blue-200 bg-white p-5 shadow-sm">
            <h3 className="font-semibold text-slate-900">Orden actual</h3>
            <p className="text-sm text-slate-700 mt-2">ID: {currentOrder.ordenLaboratorioId} · Estado: <span className="font-semibold">{currentOrder.estado}</span></p>
            <p className="text-sm text-slate-700">Examen: {currentOrder.nombreExamen}</p>
            <p className="text-sm text-slate-700">Etiqueta: {currentOrder.etiquetaId || 'N/D'}</p>
            {currentOrder.resultado && (
              <p className="text-sm text-slate-700">Conclusión: {currentOrder.resultado.conclusion} {currentOrder.resultado.critico ? '(CRÍTICO)' : ''}</p>
            )}
          </section>
        )}
      </main>
    </div>
  )
}

export default LaboratoryWorkbench

