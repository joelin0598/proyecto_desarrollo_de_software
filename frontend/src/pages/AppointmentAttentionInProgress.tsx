import React from 'react'
import { useNavigate } from 'react-router-dom'
import AdminSidebar from '@/components/ui/AdminSidebar'
import StatusChip from '@/components/ui/StatusChip'
import {
  appointmentAttentionAPI,
  authAPI,
  laboratoryAPI,
  pharmacyAPI,
  type CloseMedicalAppointmentAttentionRequest,
  type MedicineResponse,
  type MedicalAppointmentAttentionResponse,
} from '@/services/api'
import { useAuth } from '@/context/AuthContext'
import useSidebarPreference from '@/hooks/useSidebarPreference'

const formatDate = (value?: string | null) => {
  if (!value) return 'N/D'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('es-GT', { dateStyle: 'medium' }).format(date)
}

const quickTemplates = {
  evaluacionFisica: [
    'Paciente orientado, hemodinamicamente estable.',
    'Sin signos de dificultad respiratoria al examen fisico.',
    'Se evidencia mejoria clinica tras observacion inicial.',
  ],
  diagnostico: [
    'Sindrome febril en estudio.',
    'Infeccion respiratoria alta no complicada.',
    'Cefalea tensional.',
  ],
}

const getCounterTone = (value: string, min = 20) => {
  if (!value.trim()) return 'text-red-700'
  if (value.trim().length < min) return 'text-amber-700'
  return 'text-emerald-700'
}

const DRAFT_KEY_PREFIX = 'attention-draft'

const buildDraftKey = (detalleId?: number | null, userId?: number) =>
  `${DRAFT_KEY_PREFIX}:${userId ?? 'anonymous'}:${detalleId ?? 'none'}`

const formatTime = (date: Date) =>
  new Intl.DateTimeFormat('es-GT', { hour: '2-digit', minute: '2-digit' }).format(date)

const AppointmentAttentionInProgress: React.FC = () => {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const { collapsed: sidebarCollapsed, toggleCollapsed } = useSidebarPreference('admin-shell', false)
  const isFinalizingRef = React.useRef(false)
  const formRef = React.useRef<HTMLFormElement | null>(null)
  const evaluacionRef = React.useRef<HTMLTextAreaElement | null>(null)
  const diagnosticoRef = React.useRef<HTMLTextAreaElement | null>(null)

  const [loadingLogout, setLoadingLogout] = React.useState(false)
  const [loadingData, setLoadingData] = React.useState(true)
  const [loadingAction, setLoadingAction] = React.useState(false)
  const [feedback, setFeedback] = React.useState<string | null>(null)
  const [focusMode, setFocusMode] = React.useState(false)
  const [lastDraftSavedAt, setLastDraftSavedAt] = React.useState<Date | null>(null)
  const [draftRecovered, setDraftRecovered] = React.useState(false)
  const [currentAttention, setCurrentAttention] = React.useState<MedicalAppointmentAttentionResponse | null>(null)
  const [medicines, setMedicines] = React.useState<MedicineResponse[]>([])
  const [labOrderDraft, setLabOrderDraft] = React.useState({
    nombreExamen: '',
    tipoMuestra: '',
  })
  const [prescriptionDraft, setPrescriptionDraft] = React.useState({
    medicamentoId: '',
    cantidad: '1',
    dosis: '',
    viaAdministracion: '',
    frecuenciaHoras: '',
    duracionDias: '',
  })
  const [form, setForm] = React.useState<CloseMedicalAppointmentAttentionRequest>({
    evaluacionFisica: '',
    diagnostico: '',
    ordenLaboratorio: '',
    recetaMedica: '',
    medicacionPrescrita: '',
    requiereSeguimiento: false,
  })

  const evaluacionReady = form.evaluacionFisica.trim().length >= 20
  const diagnosticoReady = form.diagnostico.trim().length >= 20
  const completionRatio = (Number(evaluacionReady) + Number(diagnosticoReady)) / 2
  const completionPct = Math.round(completionRatio * 100)
  const draftKey = React.useMemo(
    () => buildDraftKey(currentAttention?.citaMedicaDetalleId, user?.id),
    [currentAttention?.citaMedicaDetalleId, user?.id]
  )

  const clearLocalDraft = React.useCallback(() => {
    window.sessionStorage.removeItem(draftKey)
    setLastDraftSavedAt(null)
    setDraftRecovered(false)
  }, [draftKey])

  const handleLogout = async () => {
    setLoadingLogout(true)
    try {
      await cancelCurrentAttention(true)
      await authAPI.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      logout()
      navigate('/')
      setLoadingLogout(false)
    }
  }

  const syncCurrentAttention = React.useCallback(async () => {
    setLoadingData(true)
    try {
      const response = await appointmentAttentionAPI.current()
      const current = response.status === 204 ? null : response.data
      setCurrentAttention(current)
      setFeedback(null)
    } catch (error: any) {
      const message = error?.response?.data?.errorMessage || 'No se pudo cargar la atención en curso.'
      setFeedback(message)
    } finally {
      setLoadingData(false)
    }
  }, [])

  React.useEffect(() => {
    void syncCurrentAttention()
  }, [syncCurrentAttention])

  React.useEffect(() => {
    if (!currentAttention) {
      setForm({
        evaluacionFisica: '',
        diagnostico: '',
        ordenLaboratorio: '',
        recetaMedica: '',
        medicacionPrescrita: '',
        requiereSeguimiento: false,
      })
      setLastDraftSavedAt(null)
      setDraftRecovered(false)
      return
    }

    const initialForm: CloseMedicalAppointmentAttentionRequest = {
      evaluacionFisica: currentAttention.evaluacionFisica || '',
      diagnostico: currentAttention.diagnostico || '',
      ordenLaboratorio: currentAttention.ordenLaboratorio || '',
      recetaMedica: currentAttention.recetaMedica || '',
      medicacionPrescrita: currentAttention.medicacionPrescrita || '',
      requiereSeguimiento: !!currentAttention.requiereSeguimiento,
    }

    try {
      const persisted = window.sessionStorage.getItem(draftKey)
      if (!persisted) {
        setForm(initialForm)
        return
      }

      const parsed = JSON.parse(persisted) as CloseMedicalAppointmentAttentionRequest
      setForm({
        evaluacionFisica: parsed.evaluacionFisica ?? initialForm.evaluacionFisica,
        diagnostico: parsed.diagnostico ?? initialForm.diagnostico,
        ordenLaboratorio: parsed.ordenLaboratorio ?? initialForm.ordenLaboratorio,
        recetaMedica: parsed.recetaMedica ?? initialForm.recetaMedica,
        medicacionPrescrita: parsed.medicacionPrescrita ?? initialForm.medicacionPrescrita,
        requiereSeguimiento: parsed.requiereSeguimiento ?? initialForm.requiereSeguimiento,
      })
      setDraftRecovered(true)
      setLastDraftSavedAt(new Date())
    } catch {
      setForm(initialForm)
      window.sessionStorage.removeItem(draftKey)
    }
  }, [currentAttention, draftKey])

  React.useEffect(() => {
    if (!currentAttention || isFinalizingRef.current) {
      return
    }

    window.sessionStorage.setItem(draftKey, JSON.stringify(form))
    setLastDraftSavedAt(new Date())
  }, [currentAttention, draftKey, form])

  React.useEffect(() => {
    // Evita cancelar automáticamente por ciclos internos de React (ej. StrictMode en desarrollo).
    // La cancelación se mantiene explícita en: botón "Cancelar proceso", navegación lateral y logout.
    return () => undefined
  }, [])

  const appendTemplate = (field: 'evaluacionFisica' | 'diagnostico', text: string) => {
    setForm((prev) => {
      const base = prev[field]?.trim()
      const nextValue = base ? `${base} ${text}` : text
      return { ...prev, [field]: nextValue }
    })
  }

  const cancelCurrentAttention = React.useCallback(async (silent = false) => {
    if (!currentAttention || isFinalizingRef.current) {
      return true
    }

    try {
      await appointmentAttentionAPI.cancel()
      clearLocalDraft()
      setCurrentAttention(null)
      if (!silent) {
        setFeedback('Proceso cancelado. El paciente regresa a la cola de espera.')
      }
      return true
    } catch (error: any) {
      if (!silent) {
        const message = error?.response?.data?.errorMessage || 'No se pudo cancelar la atención en curso.'
        setFeedback(message)
      }
      return false
    }
  }, [clearLocalDraft, currentAttention])

  const navigateWithCancel = async (target: string) => {
    if (loadingAction || loadingData) {
      return
    }
    const ok = await cancelCurrentAttention(true)
    if (ok) {
      navigate(target)
    }
  }

  const closeAttention = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!currentAttention?.citaMedicaDetalleId) {
      setFeedback('No hay detalle de atención asociado. Actualiza la página y vuelve a intentar.')
      await syncCurrentAttention()
      return
    }

    setLoadingAction(true)
    isFinalizingRef.current = true
    try {
      const response = await appointmentAttentionAPI.close(currentAttention.citaMedicaDetalleId, {
        evaluacionFisica: form.evaluacionFisica,
        diagnostico: form.diagnostico,
        ordenLaboratorio: form.ordenLaboratorio?.trim() || undefined,
        recetaMedica: form.recetaMedica?.trim() || undefined,
        medicacionPrescrita: form.medicacionPrescrita?.trim() || undefined,
        requiereSeguimiento: !!form.requiereSeguimiento,
      })
      clearLocalDraft()
      const seguimientoId = response.data?.citaSeguimientoId
      setFeedback(
        seguimientoId
          ? `Atención cerrada y cita marcada como atendida. Seguimiento tentativo generado (#${seguimientoId}).`
          : 'Atención cerrada y cita marcada como atendida.'
      )
      await syncCurrentAttention()
      navigate('/doctor/appointments/attention')
    } catch (error: any) {
      const message = error?.response?.data?.errorMessage || 'No se pudo cerrar la atención.'
      setFeedback(message)
    } finally {
      isFinalizingRef.current = false
      setLoadingAction(false)
    }
  }

  const loadMedicines = async () => {
    try {
      const response = await pharmacyAPI.listMedicines()
      setMedicines(response.data)
    } catch {
      // Mantener experiencia ligera: si falla, el médico aún puede ingresar el ID manual.
    }
  }

  const createLaboratoryOrder = async () => {
    if (!currentAttention?.citaMedicaDetalleId) {
      setFeedback('No se encontró citaMedicaDetalleId para crear la orden de laboratorio.')
      return
    }
    if (!labOrderDraft.nombreExamen.trim()) {
      setFeedback('Ingresa el nombre del examen para crear la orden de laboratorio.')
      return
    }

    setLoadingAction(true)
    try {
      const { data } = await laboratoryAPI.createOrder({
        citaMedicaDetalleId: currentAttention.citaMedicaDetalleId,
        nombreExamen: labOrderDraft.nombreExamen.trim(),
        tipoMuestra: labOrderDraft.tipoMuestra.trim() || undefined,
      })
      setForm((prev) => ({
        ...prev,
        ordenLaboratorio: prev.ordenLaboratorio?.trim()
          ? prev.ordenLaboratorio
          : `Orden #${data.ordenLaboratorioId} - ${data.nombreExamen}`,
      }))
      setFeedback(`Orden de laboratorio creada (CU07): #${data.ordenLaboratorioId}.`)
      setLabOrderDraft({ nombreExamen: '', tipoMuestra: '' })
    } catch (error: any) {
      const message = error?.response?.data?.errorMessage || 'No se pudo crear la orden de laboratorio.'
      setFeedback(message)
    } finally {
      setLoadingAction(false)
    }
  }

  const createPrescription = async () => {
    if (!currentAttention?.citaMedicaDetalleId) {
      setFeedback('No se encontró citaMedicaDetalleId para crear la receta.')
      return
    }

    const medicamentoId = Number(prescriptionDraft.medicamentoId)
    const cantidad = Number(prescriptionDraft.cantidad)
    if (!Number.isFinite(medicamentoId) || medicamentoId <= 0) {
      setFeedback('Ingresa un medicamentoId válido para la receta.')
      return
    }
    if (!Number.isFinite(cantidad) || cantidad <= 0) {
      setFeedback('Ingresa una cantidad válida para la receta.')
      return
    }

    const frecuenciaHoras = prescriptionDraft.frecuenciaHoras.trim()
      ? Number(prescriptionDraft.frecuenciaHoras)
      : undefined
    const duracionDias = prescriptionDraft.duracionDias.trim()
      ? Number(prescriptionDraft.duracionDias)
      : undefined

    setLoadingAction(true)
    try {
      const { data } = await pharmacyAPI.createPrescription({
        citaMedicaDetalleId: currentAttention.citaMedicaDetalleId,
        items: [
          {
            medicamentoId,
            cantidad,
            dosis: prescriptionDraft.dosis.trim() || undefined,
            viaAdministracion: prescriptionDraft.viaAdministracion.trim() || undefined,
            frecuenciaHoras,
            duracionDias,
          },
        ],
      })

      const detalle = data.items?.[0]
      setForm((prev) => ({
        ...prev,
        recetaMedica: prev.recetaMedica?.trim() ? prev.recetaMedica : `Receta #${data.recetaMedicaId}`,
        medicacionPrescrita: prev.medicacionPrescrita?.trim()
          ? prev.medicacionPrescrita
          : detalle
            ? `${detalle.medicamentoNombre || detalle.medicamentoId} x${detalle.cantidad}`
            : prev.medicacionPrescrita,
      }))
      setFeedback(`Receta creada (CU08): #${data.recetaMedicaId}.`)
      setPrescriptionDraft({
        medicamentoId: '',
        cantidad: '1',
        dosis: '',
        viaAdministracion: '',
        frecuenciaHoras: '',
        duracionDias: '',
      })
    } catch (error: any) {
      const message = error?.response?.data?.errorMessage || 'No se pudo crear la receta.'
      setFeedback(message)
    } finally {
      setLoadingAction(false)
    }
  }

  const handleCancelProcess = async () => {
    setLoadingAction(true)
    const ok = await cancelCurrentAttention(false)
    setLoadingAction(false)
    if (ok) {
      navigate('/doctor/appointments/attention')
    }
  }

  React.useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (!currentAttention) return

      const target = event.target as HTMLElement | null
      const inEditable = !!target && ['INPUT', 'TEXTAREA', 'SELECT'].includes(target.tagName)

      if (event.altKey && event.key === '1') {
        event.preventDefault()
        evaluacionRef.current?.focus()
        return
      }

      if (event.altKey && event.key === '2') {
        event.preventDefault()
        diagnosticoRef.current?.focus()
        return
      }

      if (event.ctrlKey && event.shiftKey && event.key.toLowerCase() === 'x') {
        event.preventDefault()
        void handleCancelProcess()
        return
      }

      if (event.ctrlKey && event.key === 'Enter' && completionPct === 100 && !loadingAction && !loadingData) {
        if (inEditable) {
          event.preventDefault()
        }
        formRef.current?.requestSubmit()
      }
    }

    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [completionPct, currentAttention, loadingAction, loadingData])

  return (
    <div className="h-screen bg-slate-100 text-slate-800 flex overflow-hidden">
      <AdminSidebar
        email={user?.email}
        role={user?.role}
        loading={loadingLogout}
        activeSection="consultation"
        collapsed={sidebarCollapsed}
        onToggleCollapse={toggleCollapsed}
        onDashboard={() => void navigateWithCancel('/admin')}
        onTriage={() => void navigateWithCancel('/triage')}
        onUsers={() => void navigateWithCancel('/admin/users')}
        onTriageList={() => void navigateWithCancel('/admin/triages')}
        onAppointments={() => void navigateWithCancel('/admin/appointments')}
        onConsultation={() => void navigateWithCancel('/doctor/appointments/attention')}
        onLogout={() => void handleLogout()}
      />

      <main className={`flex-1 min-w-0 overflow-y-auto transition-all duration-200 ${focusMode ? 'p-3 lg:p-4' : 'p-4 lg:p-5'}`}>
        <div className="flex items-start justify-between gap-4 mb-4">
          <div>
            <h2 className="text-2xl font-bold text-slate-900">Atención en curso (CU06)</h2>
            <p className="text-sm text-slate-600 mt-1">Registro clínico interactivo. Solo se guarda si el médico finaliza la atención.</p>
            <p className="text-xs text-slate-500 mt-1">Atajos: Alt+1 (Evaluación), Alt+2 (Diagnóstico), Ctrl+Enter (Finalizar), Ctrl+Shift+X (Cancelar).</p>
          </div>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setFocusMode((prev) => !prev)}
              className="px-3 py-2 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 text-xs font-semibold"
            >
              {focusMode ? 'Vista normal' : 'Modo enfoque'}
            </button>
            <button
              type="button"
              onClick={() => void handleCancelProcess()}
              disabled={loadingAction || loadingData}
              className="px-3 py-2 rounded-lg border border-blue-200 bg-white hover:bg-slate-50 text-slate-700 text-xs font-semibold"
            >
              Volver a cola clínica
            </button>
            <StatusChip label={currentAttention ? 'Atención activa' : 'Sin atención activa'} tone={currentAttention ? 'emerald' : 'slate'} />
          </div>
        </div>

        {currentAttention && (
          <div className="mb-4 flex flex-wrap items-center gap-2">
            {draftRecovered && (
              <span className="inline-flex items-center rounded-full border border-blue-200 bg-blue-50 px-2.5 py-1 text-xs text-blue-700">
                Borrador local recuperado
              </span>
            )}
            {lastDraftSavedAt && (
              <span className="inline-flex items-center rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-600">
                Borrador local: {formatTime(lastDraftSavedAt)}
              </span>
            )}
            <button
              type="button"
              onClick={clearLocalDraft}
              className="inline-flex items-center rounded-full border border-slate-200 bg-white px-2.5 py-1 text-xs text-slate-700 hover:bg-slate-50"
            >
              Limpiar borrador local
            </button>
          </div>
        )}

        {currentAttention && (
          <section className="mb-4 rounded-xl border border-slate-300 bg-white shadow-sm p-4">
            <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-3">
              <div>
                <p className="text-xs uppercase tracking-wide text-slate-500">Progreso de documentación clínica</p>
                <p className="text-sm font-semibold text-slate-800 mt-1">
                  {completionPct === 100 ? 'Listo para finalizar atención' : 'Completa evaluación física y diagnóstico para finalizar'}
                </p>
              </div>
              <div className="min-w-[220px]">
                <div className="h-2.5 rounded-full bg-slate-200 overflow-hidden">
                  <div
                    className={`h-full transition-all duration-300 ${completionPct === 100 ? 'bg-emerald-500' : 'bg-blue-500'}`}
                    style={{ width: `${completionPct}%` }}
                  />
                </div>
                <div className="flex justify-between mt-2 text-xs text-slate-600">
                  <span>Completado</span>
                  <span>{completionPct}%</span>
                </div>
              </div>
            </div>
          </section>
        )}

        {feedback && (
          <div className="mb-4 rounded-lg border border-blue-200 bg-blue-50 px-4 py-3 text-sm text-blue-900">
            {feedback}
          </div>
        )}

        {loadingData ? (
          <div className="rounded-xl border border-blue-100 bg-blue-50 p-6 text-sm text-slate-600">Cargando atención en curso...</div>
        ) : !currentAttention ? (
          <div className="rounded-xl border border-slate-300 bg-white shadow-sm p-8">
            <div className="rounded-xl border border-blue-100 bg-blue-50 px-6 py-8 text-center">
              <p className="text-lg font-semibold text-slate-700">No hay atención activa</p>
              <p className="text-sm text-slate-600 mt-2">Inicia una consulta desde la cola clínica para comenzar el registro médico.</p>
              <button
                type="button"
                onClick={() => navigate('/doctor/appointments/attention')}
                className="mt-4 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold"
              >
                Ir a cola clínica
              </button>
            </div>
          </div>
        ) : (
          <form ref={formRef} onSubmit={closeAttention} className="space-y-4">
            <div className={`grid grid-cols-1 gap-4 ${focusMode ? 'xl:grid-cols-1' : '2xl:grid-cols-12'}`}>
              <section className={`${focusMode ? '' : '2xl:col-span-8'} rounded-xl border border-slate-300 bg-white shadow-sm p-4`}>
                <div className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700 mb-4 grid grid-cols-1 lg:grid-cols-2 gap-2">
                  <p><span className="font-semibold">Paciente:</span> {currentAttention.pacienteNombre}</p>
                  <p><span className="font-semibold">DPI:</span> {currentAttention.pacienteDpi || 'N/D'}</p>
                  <p><span className="font-semibold">Cita:</span> #{currentAttention.citaMedicaId}</p>
                  <p><span className="font-semibold">Detalle técnico:</span> #{currentAttention.citaMedicaDetalleId}</p>
                  <p><span className="font-semibold">Fecha:</span> {formatDate(currentAttention.fechaCita)} {currentAttention.horaCita || ''}</p>
                  <p><span className="font-semibold">Prioridad:</span> {currentAttention.prioridad || 'SIN_TRIAJE'}</p>
                </div>

                <div className="space-y-4">
                  <div className={`rounded-lg p-3 transition border ${evaluacionReady ? 'border-emerald-300 bg-emerald-50' : 'border-amber-300 bg-amber-50'}`}>
                    <div className="flex items-center justify-between mb-1">
                      <label className="text-sm font-semibold text-slate-900">Evaluación física *</label>
                      <span className={`text-xs font-semibold ${getCounterTone(form.evaluacionFisica)}`}>{form.evaluacionFisica.trim().length} / 20 mínimo</span>
                    </div>
                    <textarea
                      ref={evaluacionRef}
                      value={form.evaluacionFisica}
                      onChange={(event) => setForm((prev) => ({ ...prev, evaluacionFisica: event.target.value }))}
                      placeholder="Describe hallazgos clínicos relevantes..."
                      required
                      className={`w-full px-3 py-2.5 rounded-lg border border-white/70 bg-white focus:outline-none focus:ring-2 focus:ring-emerald-300 ${focusMode ? 'min-h-[220px] text-base' : 'min-h-[160px] text-sm'}`}
                    />
                    <p className="text-xs text-slate-600 mt-1">Incluye hallazgos objetivos, estado general y respuesta clínica observada.</p>
                    <div className="mt-2 flex flex-wrap gap-2">
                      {quickTemplates.evaluacionFisica.map((template) => (
                        <button
                          key={template}
                          type="button"
                          onClick={() => appendTemplate('evaluacionFisica', template)}
                          className="px-2.5 py-1 rounded-full border border-emerald-300 bg-white hover:bg-emerald-100 text-xs text-emerald-800"
                        >
                          + Sugerencia
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className={`rounded-lg p-3 transition border ${diagnosticoReady ? 'border-blue-300 bg-blue-50' : 'border-amber-300 bg-amber-50'}`}>
                    <div className="flex items-center justify-between mb-1">
                      <label className="text-sm font-semibold text-slate-900">Diagnóstico *</label>
                      <span className={`text-xs font-semibold ${getCounterTone(form.diagnostico)}`}>{form.diagnostico.trim().length} / 20 mínimo</span>
                    </div>
                    <textarea
                      ref={diagnosticoRef}
                      value={form.diagnostico}
                      onChange={(event) => setForm((prev) => ({ ...prev, diagnostico: event.target.value }))}
                      placeholder="Registra diagnóstico principal y/o diferencial..."
                      required
                      className={`w-full px-3 py-2.5 rounded-lg border border-white/70 bg-white focus:outline-none focus:ring-2 focus:ring-blue-300 ${focusMode ? 'min-h-[220px] text-base' : 'min-h-[160px] text-sm'}`}
                    />
                    <p className="text-xs text-slate-600 mt-1">Registra diagnóstico principal y, si aplica, diagnóstico diferencial breve.</p>
                    <div className="mt-2 flex flex-wrap gap-2">
                      {quickTemplates.diagnostico.map((template) => (
                        <button
                          key={template}
                          type="button"
                          onClick={() => appendTemplate('diagnostico', template)}
                          className="px-2.5 py-1 rounded-full border border-blue-300 bg-white hover:bg-blue-100 text-xs text-blue-800"
                        >
                          + Sugerencia
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              </section>

              <aside className={`${focusMode ? '' : '2xl:col-span-4'} rounded-xl border border-slate-300 bg-gradient-to-b from-white to-slate-50 shadow-sm p-4 space-y-3`}>
                <h3 className="text-base font-semibold text-slate-900">Complementos clínicos</h3>
                <p className="text-xs text-slate-600">Completa estos campos solo cuando aplique a la consulta actual.</p>

                <div className="rounded-lg border border-violet-200 bg-violet-50 p-3 space-y-2">
                  <p className="text-xs font-semibold text-violet-900">CU07 · Generar orden de laboratorio</p>
                  <input
                    value={labOrderDraft.nombreExamen}
                    onChange={(event) => setLabOrderDraft((prev) => ({ ...prev, nombreExamen: event.target.value }))}
                    placeholder="Nombre del examen"
                    className="w-full px-3 py-2 rounded-lg border border-violet-200 bg-white text-sm"
                  />
                  <input
                    value={labOrderDraft.tipoMuestra}
                    onChange={(event) => setLabOrderDraft((prev) => ({ ...prev, tipoMuestra: event.target.value }))}
                    placeholder="Tipo de muestra (opcional)"
                    className="w-full px-3 py-2 rounded-lg border border-violet-200 bg-white text-sm"
                  />
                  <button
                    type="button"
                    onClick={() => void createLaboratoryOrder()}
                    disabled={loadingAction}
                    className="w-full px-3 py-2 rounded-lg bg-violet-700 hover:bg-violet-800 text-white text-xs font-semibold disabled:opacity-60"
                  >
                    Crear orden CU07
                  </button>
                </div>

                <div className="rounded-lg border border-amber-200 bg-amber-50 p-3 space-y-2">
                  <div className="flex items-center justify-between gap-2">
                    <p className="text-xs font-semibold text-amber-900">CU08 · Crear receta médica</p>
                    <button
                      type="button"
                      onClick={() => void loadMedicines()}
                      className="px-2 py-1 rounded border border-amber-300 bg-white text-[11px] text-amber-800 hover:bg-amber-100"
                    >
                      Ver inventario
                    </button>
                  </div>
                  <input
                    value={prescriptionDraft.medicamentoId}
                    onChange={(event) => setPrescriptionDraft((prev) => ({ ...prev, medicamentoId: event.target.value }))}
                    placeholder="medicamentoId"
                    className="w-full px-3 py-2 rounded-lg border border-amber-200 bg-white text-sm"
                  />
                  <input
                    value={prescriptionDraft.cantidad}
                    onChange={(event) => setPrescriptionDraft((prev) => ({ ...prev, cantidad: event.target.value }))}
                    placeholder="Cantidad"
                    className="w-full px-3 py-2 rounded-lg border border-amber-200 bg-white text-sm"
                  />
                  <input
                    value={prescriptionDraft.dosis}
                    onChange={(event) => setPrescriptionDraft((prev) => ({ ...prev, dosis: event.target.value }))}
                    placeholder="Dosis (opcional)"
                    className="w-full px-3 py-2 rounded-lg border border-amber-200 bg-white text-sm"
                  />
                  <input
                    value={prescriptionDraft.viaAdministracion}
                    onChange={(event) => setPrescriptionDraft((prev) => ({ ...prev, viaAdministracion: event.target.value }))}
                    placeholder="Vía administración (opcional)"
                    className="w-full px-3 py-2 rounded-lg border border-amber-200 bg-white text-sm"
                  />
                  <div className="grid grid-cols-2 gap-2">
                    <input
                      value={prescriptionDraft.frecuenciaHoras}
                      onChange={(event) => setPrescriptionDraft((prev) => ({ ...prev, frecuenciaHoras: event.target.value }))}
                      placeholder="Frecuencia h"
                      className="w-full px-3 py-2 rounded-lg border border-amber-200 bg-white text-sm"
                    />
                    <input
                      value={prescriptionDraft.duracionDias}
                      onChange={(event) => setPrescriptionDraft((prev) => ({ ...prev, duracionDias: event.target.value }))}
                      placeholder="Duración días"
                      className="w-full px-3 py-2 rounded-lg border border-amber-200 bg-white text-sm"
                    />
                  </div>
                  {medicines.length > 0 && (
                    <p className="text-[11px] text-amber-800">
                      Inventario cargado: {medicines.slice(0, 3).map((m) => `${m.medicamentoId}-${m.nombre}`).join(' · ')}
                      {medicines.length > 3 ? ' ...' : ''}
                    </p>
                  )}
                  <button
                    type="button"
                    onClick={() => void createPrescription()}
                    disabled={loadingAction}
                    className="w-full px-3 py-2 rounded-lg bg-amber-600 hover:bg-amber-700 text-white text-xs font-semibold disabled:opacity-60"
                  >
                    Crear receta CU08
                  </button>
                </div>

                <textarea
                  value={form.ordenLaboratorio}
                  onChange={(event) => setForm((prev) => ({ ...prev, ordenLaboratorio: event.target.value }))}
                  placeholder="Orden de laboratorio (opcional)"
                  className="w-full min-h-[110px] px-3 py-2.5 rounded-lg border border-slate-300 bg-white focus:outline-none focus:ring-2 focus:ring-slate-300 text-sm"
                />

                <textarea
                  value={form.recetaMedica}
                  onChange={(event) => setForm((prev) => ({ ...prev, recetaMedica: event.target.value }))}
                  placeholder="Receta medica (opcional)"
                  className="w-full min-h-[110px] px-3 py-2.5 rounded-lg border border-slate-300 bg-white focus:outline-none focus:ring-2 focus:ring-slate-300 text-sm"
                />

                <textarea
                  value={form.medicacionPrescrita}
                  onChange={(event) => setForm((prev) => ({ ...prev, medicacionPrescrita: event.target.value }))}
                  placeholder="Medicacion prescrita (opcional)"
                  className="w-full min-h-[110px] px-3 py-2.5 rounded-lg border border-slate-300 bg-white focus:outline-none focus:ring-2 focus:ring-slate-300 text-sm"
                />

                <label className="inline-flex items-center gap-2 text-sm text-slate-700 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 w-full">
                  <input
                    type="checkbox"
                    checked={!!form.requiereSeguimiento}
                    onChange={(event) => setForm((prev) => ({ ...prev, requiereSeguimiento: event.target.checked }))}
                  />
                  Requiere seguimiento clínico
                </label>
              </aside>
            </div>

            <div className="sticky bottom-0 rounded-xl border border-slate-300 bg-white/95 backdrop-blur px-4 py-3 shadow-sm flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
              <p className="text-xs text-slate-600">
                Si sales sin finalizar, la atención se cancela y el paciente regresa a la cola.
              </p>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => void handleCancelProcess()}
                  disabled={loadingAction}
                  className="px-4 py-2 rounded-lg border border-slate-300 bg-white hover:bg-slate-50 text-slate-700 text-sm font-semibold disabled:opacity-60"
                >
                  Cancelar proceso
                </button>
                <button
                  type="submit"
                  disabled={loadingAction || !evaluacionReady || !diagnosticoReady}
                  className="px-4 py-2 rounded-lg bg-emerald-700 hover:bg-emerald-800 text-white font-semibold text-sm disabled:opacity-60 disabled:cursor-not-allowed"
                >
                  {loadingAction ? 'Guardando...' : 'Finalizar Atención'}
                </button>
              </div>
            </div>
          </form>
        )}
      </main>
    </div>
  )
}

export default AppointmentAttentionInProgress





