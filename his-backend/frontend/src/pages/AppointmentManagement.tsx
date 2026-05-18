import React from 'react'
import { Link } from 'react-router-dom'
import StatusChip from '@/components/ui/StatusChip'
import { useAuth } from '@/context/AuthContext'
import {
  appointmentAPI,
  catalogAPI,
  type DoctorOption,
  type InsuranceOption,
  type PaymentOption,
  type ScheduleAppointmentRequest,
  type ScheduleAppointmentResponse,
  type SpecialtyOption,
} from '@/services/api'
type FeedbackState = { kind: 'success' | 'error'; message: string } | null
type StepKey = 'AGENDA' | 'SOLVENCIA' | 'CONFIRMACION'
type AppointmentFormState = {
  especialidadId: string
  medicoPersonalId: string
  fechaCita: string
  horaCita: string
  motivoConsulta: string
  metodoPago: PaymentOption
  bancoTarjeta: string
  numeroTarjeta: string
  fechaVencimientoTarjeta: string
  nombreTitularTarjeta: string
  cvc: string
  aseguradoraId: string
  numeroPoliza: string
}
const stepOrder: Array<{ key: StepKey; label: string }> = [
  { key: 'AGENDA', label: '1. Agenda y paciente' },
  { key: 'SOLVENCIA', label: '2. Solvencia administrativa' },
  { key: 'CONFIRMACION', label: '3. Confirmación' },
]
const initialForm: AppointmentFormState = {
  especialidadId: '',
  medicoPersonalId: '',
  fechaCita: '',
  horaCita: '',
  motivoConsulta: '',
  metodoPago: 'TARJETA',
  bancoTarjeta: '',
  numeroTarjeta: '',
  fechaVencimientoTarjeta: '',
  nombreTitularTarjeta: '',
  cvc: '',
  aseguradoraId: '',
  numeroPoliza: '',
}
/** Genera todos los slots de 08:00 a 16:30 con intervalo de 30 min (RN05) */
const generateTimeSlots = (): string[] => {
  const slots: string[] = []
  for (let h = 8; h <= 16; h++) {
    slots.push(`${String(h).padStart(2, '0')}:00`)
    if (h < 16) {
      slots.push(`${String(h).padStart(2, '0')}:30`)
    } else {
      slots.push('16:30')
    }
  }
  return slots
}
const TIME_SLOTS = generateTimeSlots()
/** Fecha mínima válida: 24h desde ahora en formato YYYY-MM-DD */
const getMinDate = (): string => {
  const d = new Date(Date.now() + 24 * 60 * 60 * 1000)
  return d.toISOString().split('T')[0]
}
const formatDate = (value: string) => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return new Intl.DateTimeFormat('es-GT', { dateStyle: 'medium' }).format(date)
}
const appointmentTone = (item: ScheduleAppointmentResponse) => {
  if (item.estadoAdministrativo === 'PAGO_VALIDADO') return 'emerald' as const
  return 'amber' as const
}
const AppointmentManagement: React.FC = () => {
  const { user } = useAuth()
  const [loadingCatalogs, setLoadingCatalogs] = React.useState(true)
  const [saving, setSaving] = React.useState(false)
  const [loadingList, setLoadingList] = React.useState(true)
  const [loadingDoctors, setLoadingDoctors] = React.useState(false)
  const [refreshing, setRefreshing] = React.useState(false)
  const [stepIndex, setStepIndex] = React.useState(0)
  const [form, setForm] = React.useState<AppointmentFormState>(initialForm)
  const [feedback, setFeedback] = React.useState<FeedbackState>(null)
  const [appointments, setAppointments] = React.useState<ScheduleAppointmentResponse[]>([])
  const [insurances, setInsurances] = React.useState<InsuranceOption[]>([])
  const [specialties, setSpecialties] = React.useState<SpecialtyOption[]>([])
  const [doctors, setDoctors] = React.useState<DoctorOption[]>([])
  const [search, setSearch] = React.useState('')
  const currentStep = stepOrder[stepIndex]
  const loadCatalogs = React.useCallback(async () => {
    setLoadingCatalogs(true)
    try {
      const [insRes, spRes] = await Promise.all([
        catalogAPI.insurances(),
        catalogAPI.specialties(),
      ])
      setInsurances(insRes.data)
      setSpecialties(spRes.data)
    } catch {
      setFeedback({ kind: 'error', message: 'No se pudieron cargar los catálogos.' })
    } finally {
      setLoadingCatalogs(false)
    }
  }, [])
  const loadDoctors = React.useCallback(async (especialidadId?: number) => {
    setLoadingDoctors(true)
    setDoctors([])
    setForm((prev) => ({ ...prev, medicoPersonalId: '' }))
    try {
      const res = await catalogAPI.doctorsBySpecialty(especialidadId)
      setDoctors(res.data)
    } catch {
      setFeedback({ kind: 'error', message: 'No se pudieron cargar los médicos.' })
    } finally {
      setLoadingDoctors(false)
    }
  }, [])
  const loadAppointments = React.useCallback(async (showRefreshing = false) => {
    if (showRefreshing) setRefreshing(true)
    else setLoadingList(true)
    try {
      const response = await appointmentAPI.list()
      setAppointments(response.data)
    } catch (error: any) {
      const msg = error?.response?.data?.errorMessage || 'No se pudo cargar el listado de citas.'
      setFeedback({ kind: 'error', message: msg })
    } finally {
      setLoadingList(false)
      setRefreshing(false)
    }
  }, [])
  React.useEffect(() => {
    void loadCatalogs()
    void loadAppointments()
  }, [loadCatalogs, loadAppointments])
  const handleChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = event.target
    if (name === 'especialidadId') {
      setForm((prev) => ({ ...prev, especialidadId: value }))
      void loadDoctors(value ? Number(value) : undefined)
      return
    }
    if (name === 'numeroTarjeta') {
      const sanitized = value.replace(/\D/g, '').slice(0, 19)
      setForm((prev) => ({ ...prev, numeroTarjeta: sanitized }))
      return
    }
    if (name === 'cvc') {
      const sanitized = value.replace(/\D/g, '').slice(0, 4)
      setForm((prev) => ({ ...prev, cvc: sanitized }))
      return
    }
    if (name === 'fechaVencimientoTarjeta') {
      // Solo dígitos del value actual (strip todo lo que no sea dígito)
      const onlyDigits = value.replace(/\D/g, '').slice(0, 4)
      let formatted: string
      if (onlyDigits.length === 0) {
        formatted = ''
      } else if (onlyDigits.length <= 2) {
        // Si el usuario borró y quedó solo 1 dígito, no agregar '/'
        // Si llegó a 2 dígitos y NO venía de borrado (longitud creció), agrega '/'
        const wasDeleting = value.length < form.fechaVencimientoTarjeta.length
        formatted = wasDeleting ? onlyDigits : `${onlyDigits}/`
      } else {
        formatted = `${onlyDigits.slice(0, 2)}/${onlyDigits.slice(2)}`
      }
      setForm((prev) => ({ ...prev, fechaVencimientoTarjeta: formatted }))
      return
    }
    setForm((prev) => ({ ...prev, [name]: value }))
  }
  const validateAgendaStep = (): string | null => {
    if (!form.especialidadId) return 'Debes seleccionar una especialidad.'
    if (!form.medicoPersonalId) return 'Debes seleccionar un médico.'
    if (!form.fechaCita) return 'La fecha de cita es obligatoria.'
    if (!form.horaCita) return 'Debes seleccionar un horario.'
    if (!form.motivoConsulta.trim() || form.motivoConsulta.trim().length < 5) {
      return 'El motivo de consulta debe tener al menos 5 caracteres.'
    }
    const appointment = new Date(`${form.fechaCita}T${form.horaCita}:00`)
    const minDate = new Date(Date.now() + 24 * 60 * 60 * 1000)
    if (appointment.getTime() < minDate.getTime()) {
      return 'La cita debe programarse con al menos 24 horas de anticipación.'
    }
    return null
  }
  const validateSolvenciaStep = (): string | null => {
    if (form.metodoPago === 'TARJETA') {
      if (!form.bancoTarjeta.trim()) return 'Debes indicar el banco de la tarjeta.'
      if (!/^\d{13,19}$/.test(form.numeroTarjeta)) return 'El número de tarjeta debe tener entre 13 y 19 dígitos.'
      if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(form.fechaVencimientoTarjeta)) return 'La fecha de vencimiento debe ser MM/yy.'
      if (!form.nombreTitularTarjeta.trim()) return 'Debes indicar el nombre del titular.'
      if (!/^\d{3,4}$/.test(form.cvc)) return 'El CVC debe tener 3 o 4 dígitos.'
      return null
    }
    if (!form.aseguradoraId) return 'Debes seleccionar la aseguradora.'
    if (!form.numeroPoliza.trim()) return 'Debes ingresar el número de póliza.'
    return null
  }
  const goNext = () => {
    const validationError = currentStep.key === 'AGENDA'
      ? validateAgendaStep()
      : currentStep.key === 'SOLVENCIA'
        ? validateSolvenciaStep()
        : null
    if (validationError) {
      setFeedback({ kind: 'error', message: validationError })
      return
    }
    setFeedback(null)
    setStepIndex((prev) => Math.min(prev + 1, stepOrder.length - 1))
  }
  const goBack = () => {
    setStepIndex((prev) => Math.max(prev - 1, 0))
    setFeedback(null)
  }
  const handleReset = () => {
    setForm(initialForm)
    setStepIndex(0)
    setFeedback(null)
    setDoctors([])
  }
  const submitAppointment = async () => {
    if (currentStep.key !== 'CONFIRMACION') {
      setFeedback({ kind: 'error', message: 'Debes confirmar la información en la fase 3 antes de registrar.' })
      return
    }
    const agendaError = validateAgendaStep()
    const solvenciaError = validateSolvenciaStep()
    if (agendaError || solvenciaError) {
      setFeedback({ kind: 'error', message: agendaError || solvenciaError || 'Datos inválidos.' })
      return
    }
    const payload: ScheduleAppointmentRequest = {
      medicoPersonalId: Number(form.medicoPersonalId),
      especialidadId: Number(form.especialidadId),
      fechaCita: form.fechaCita,
      horaCita: `${form.horaCita}:00`,
      motivoConsulta: form.motivoConsulta.trim(),
      metodoPago: form.metodoPago,
      bancoTarjeta: form.metodoPago === 'TARJETA' ? form.bancoTarjeta.trim() : undefined,
      numeroTarjeta: form.metodoPago === 'TARJETA' ? form.numeroTarjeta : undefined,
      fechaVencimientoTarjeta: form.metodoPago === 'TARJETA' ? form.fechaVencimientoTarjeta : undefined,
      nombreTitularTarjeta: form.metodoPago === 'TARJETA' ? form.nombreTitularTarjeta.trim() : undefined,
      cvc: form.metodoPago === 'TARJETA' ? form.cvc : undefined,
      aseguradoraId: form.metodoPago === 'SEGURO' ? Number(form.aseguradoraId) : undefined,
      numeroPoliza: form.metodoPago === 'SEGURO' ? form.numeroPoliza.trim() : undefined,
    }
    setSaving(true)
    setFeedback(null)
    try {
      const response = await appointmentAPI.schedule(payload)
      const saved = response.data
      setFeedback({
        kind: 'success',
        message: `Cita #${saved.citaMedicaId} registrada. Estado administrativo: ${saved.estadoAdministrativo}.`,
      })
      handleReset()
      await loadAppointments(true)
    } catch (error: any) {
      const msg = error?.response?.data?.errorMessage || error?.message || 'No se pudo registrar la cita.'
      setFeedback({ kind: 'error', message: msg })
    } finally {
      setSaving(false)
    }
  }
  const filteredAppointments = React.useMemo(() => {
    const normalized = search.trim().toLowerCase()
    if (!normalized) return appointments
    return appointments.filter((item) => {
      const blob = [
        item.citaMedicaId,
        item.pacienteId,
        item.medicoPersonalId,
        item.metodoPago,
        item.estadoAdministrativo,
        item.estadoCita,
        item.mensajeValidacion,
      ].join(' ').toLowerCase()
      return blob.includes(normalized)
    })
  }, [appointments, search])
  const selectedSpecialtyName = specialties.find((s) => String(s.id) === form.especialidadId)?.nombre
  const selectedDoctorName = doctors.find((d) => String(d.personalId) === form.medicoPersonalId)?.nombreCompleto
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-100 via-sky-50 to-blue-100 text-slate-800">
      <main className="max-w-6xl mx-auto px-4 sm:px-6 py-6">
        <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4 mb-5">
          <div>
            <p className="text-xs uppercase tracking-[0.2em] text-slate-400">Portal Paciente · CU04</p>
            <h2 className="text-2xl font-bold text-slate-900 mt-1">Agendar cita y validar cobertura</h2>
            <p className="text-sm text-slate-600 mt-1">Programa tu consulta y registra método de pago o seguro médico.</p>
          </div>
          <div className="flex items-center gap-2">
            <Link to="/portal" className="px-4 py-2 rounded-lg border border-blue-200 bg-white hover:bg-slate-50 text-slate-700 text-sm font-semibold">Volver al portal</Link>
            <StatusChip label="Tarifa fija Q175.00" tone="blue" />
          </div>
        </div>
        {feedback && (
          <div className={`mb-4 rounded-lg border px-4 py-3 text-sm ${feedback.kind === 'success' ? 'border-emerald-200 bg-emerald-50 text-emerald-700' : 'border-red-200 bg-red-50 text-red-700'}`}>
            {feedback.message}
          </div>
        )}
        <section className="grid grid-cols-1 xl:grid-cols-[260px_minmax(0,1fr)] gap-4 mb-5">
          <aside className="rounded-xl border border-blue-200 bg-white shadow-sm p-4">
            <p className="text-xs uppercase tracking-[0.16em] text-slate-400 mb-3">Flujo CU04</p>
            <div className="space-y-2">
              {stepOrder.map((step, index) => {
                const active = index === stepIndex
                const visited = index < stepIndex
                return (
                  <button
                    key={step.key}
                    type="button"
                    onClick={() => index <= stepIndex && setStepIndex(index)}
                    className={`w-full text-left px-3 py-2.5 rounded-lg text-sm transition ${active ? 'bg-blue-600 text-white font-semibold shadow-sm' : visited ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-slate-50 text-slate-600 border border-slate-200'}`}
                  >
                    {step.label}
                  </button>
                )
              })}
            </div>
            <div className="mt-5 rounded-lg border border-blue-100 bg-blue-50 p-3">
              <p className="text-xs font-semibold text-blue-700 uppercase mb-1">Paciente</p>
              <p className="text-sm font-semibold text-slate-800">{user?.firstName} {user?.lastName}</p>
              <p className="text-xs text-slate-500 break-all">{user?.email}</p>
            </div>
          </aside>
          <form
            onSubmit={(event) => event.preventDefault()}
            onKeyDown={(event) => {
              if (event.key === 'Enter') {
                event.preventDefault()
              }
            }}
            className="rounded-xl border border-blue-200 bg-white shadow-sm p-4 lg:p-5 space-y-4"
          >
            <div className="flex items-center justify-between gap-3">
              <div>
                <h3 className="text-lg font-bold text-slate-900">{currentStep.label}</h3>
                <p className="text-sm text-slate-600 mt-1">Cumple reglas RN05: 24h mínimo, 08:00–16:30, cada 30 min.</p>
              </div>
              <StatusChip label={`Paso ${stepIndex + 1} de ${stepOrder.length}`} tone="slate" />
            </div>
            {currentStep.key === 'AGENDA' && (
              <div className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">
                    Especialidad <span className="text-red-500">*</span>
                  </label>
                  <select
                    name="especialidadId"
                    value={form.especialidadId}
                    onChange={handleChange}
                    disabled={loadingCatalogs}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-600 focus:outline-none"
                  >
                    <option value="">{loadingCatalogs ? 'Cargando especialidades...' : '-- Selecciona una especialidad --'}</option>
                    {specialties.map((s) => (
                      <option key={s.id} value={s.id}>{s.nombre}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">
                    Médico <span className="text-red-500">*</span>
                  </label>
                  <select
                    name="medicoPersonalId"
                    value={form.medicoPersonalId}
                    onChange={handleChange}
                    disabled={!form.especialidadId || loadingDoctors}
                    required
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-600 focus:outline-none disabled:bg-slate-50 disabled:text-slate-400"
                  >
                    <option value="">
                      {!form.especialidadId
                        ? 'Selecciona primero una especialidad'
                        : loadingDoctors
                          ? 'Cargando médicos...'
                          : doctors.length === 0
                            ? 'No hay médicos disponibles para esta especialidad'
                            : '-- Selecciona un médico --'}
                    </option>
                    {doctors.map((d) => (
                      <option key={d.personalId} value={d.personalId}>
                        {d.nombreCompleto}{d.numeroColegiado ? ` — Colegiado: ${d.numeroColegiado}` : ''}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-semibold text-gray-600 mb-1">
                      Fecha de cita <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="date"
                      name="fechaCita"
                      value={form.fechaCita}
                      onChange={handleChange}
                      min={getMinDate()}
                      required
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-600 focus:outline-none"
                    />
                    <p className="text-xs text-slate-400 mt-1">Mínimo 24 h de anticipación (RN05)</p>
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-600 mb-1">
                      Horario disponible <span className="text-red-500">*</span>
                    </label>
                    <select
                      name="horaCita"
                      value={form.horaCita}
                      onChange={handleChange}
                      required
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-600 focus:outline-none"
                    >
                      <option value="">-- Selecciona un horario --</option>
                      {TIME_SLOTS.map((slot) => (
                        <option key={slot} value={slot}>{slot}</option>
                      ))}
                    </select>
                    <p className="text-xs text-slate-400 mt-1">08:00 – 16:30, intervalos de 30 min (RN05)</p>
                  </div>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">
                    Motivo de consulta <span className="text-red-500">*</span>
                  </label>
                  <textarea
                    name="motivoConsulta"
                    value={form.motivoConsulta}
                    onChange={handleChange}
                    rows={3}
                    required
                    placeholder="Describe brevemente el motivo de tu consulta (mínimo 5 caracteres)..."
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-600 focus:outline-none"
                  />
                </div>
              </div>
            )}
            {currentStep.key === 'SOLVENCIA' && (
              <div className="space-y-4">
                <div className="flex flex-wrap gap-2">
                  <button type="button" onClick={() => setForm((prev) => ({ ...prev, metodoPago: 'TARJETA' }))} className={`px-4 py-2 rounded-lg border text-sm font-semibold ${form.metodoPago === 'TARJETA' ? 'bg-blue-600 border-blue-600 text-white' : 'bg-white border-blue-200 text-slate-700 hover:bg-blue-50'}`}>Tarjeta</button>
                  <button type="button" onClick={() => setForm((prev) => ({ ...prev, metodoPago: 'SEGURO' }))} className={`px-4 py-2 rounded-lg border text-sm font-semibold ${form.metodoPago === 'SEGURO' ? 'bg-blue-600 border-blue-600 text-white' : 'bg-white border-blue-200 text-slate-700 hover:bg-blue-50'}`}>Seguro médico</button>
                </div>
                {form.metodoPago === 'TARJETA' ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">Banco <span className="text-red-500">*</span></label>
                      <input name="bancoTarjeta" value={form.bancoTarjeta} onChange={handleChange} placeholder="Ej. Banrural, BAC, G&T" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-600 focus:outline-none" />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">Número de tarjeta <span className="text-red-500">*</span></label>
                      <input name="numeroTarjeta" value={form.numeroTarjeta} onChange={handleChange} inputMode="numeric" maxLength={19} placeholder="0000 0000 0000 0000" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-600 focus:outline-none" />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">Vencimiento (MM/yy) <span className="text-red-500">*</span></label>
                      <input name="fechaVencimientoTarjeta" value={form.fechaVencimientoTarjeta} onChange={handleChange} inputMode="numeric" maxLength={5} placeholder="MM/yy" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-600 focus:outline-none" />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">Titular <span className="text-red-500">*</span></label>
                      <input name="nombreTitularTarjeta" value={form.nombreTitularTarjeta} onChange={handleChange} placeholder="Nombre como aparece en la tarjeta" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-600 focus:outline-none" />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">CVC <span className="text-red-500">*</span></label>
                      <input name="cvc" value={form.cvc} onChange={handleChange} inputMode="numeric" maxLength={4} placeholder="000" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-600 focus:outline-none" />
                    </div>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">Aseguradora <span className="text-red-500">*</span></label>
                      <select name="aseguradoraId" value={form.aseguradoraId} onChange={handleChange} disabled={loadingCatalogs} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-600 focus:outline-none">
                        <option value="">{loadingCatalogs ? 'Cargando...' : 'Selecciona aseguradora'}</option>
                        {insurances.map((item) => <option key={item.id} value={item.id}>{item.nombre}</option>)}
                      </select>
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">Número de póliza <span className="text-red-500">*</span></label>
                      <input name="numeroPoliza" value={form.numeroPoliza} onChange={handleChange} placeholder="Ej. POL-2024-00123" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-600 focus:outline-none" />
                    </div>
                  </div>
                )}
              </div>
            )}
            {currentStep.key === 'CONFIRMACION' && (
              <div className="rounded-xl border border-blue-100 bg-blue-50 p-4 space-y-3 text-sm text-slate-700">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                  <p><span className="font-semibold">Paciente:</span> {user?.firstName} {user?.lastName}</p>
                  <p><span className="font-semibold">Especialidad:</span> {selectedSpecialtyName}</p>
                  <p><span className="font-semibold">Médico:</span> {selectedDoctorName}</p>
                  <p><span className="font-semibold">Fecha:</span> {formatDate(form.fechaCita)}</p>
                  <p><span className="font-semibold">Hora:</span> {form.horaCita}</p>
                  <p><span className="font-semibold">Método:</span> {form.metodoPago}</p>
                  <p><span className="font-semibold">Tarifa:</span> Q175.00</p>
                </div>
                <p><span className="font-semibold">Motivo:</span> {form.motivoConsulta}</p>
                <p className="text-xs text-slate-500">Nota: la validación administrativa de tarjeta/seguro es simulada en backend.</p>
              </div>
            )}
            <div className="flex justify-between gap-2 pt-1">
              <button type="button" onClick={handleReset} className="px-4 py-2 text-sm border border-gray-300 rounded-lg text-gray-600 hover:bg-gray-50">Limpiar</button>
              <div className="flex gap-2">
                {stepIndex > 0 && (
                  <button type="button" onClick={goBack} className="px-4 py-2 text-sm border border-gray-300 rounded-lg text-gray-600 hover:bg-gray-50">Anterior</button>
                )}
                {stepIndex < stepOrder.length - 1 ? (
                  <button type="button" onClick={goNext} className="px-4 py-2 text-sm rounded-lg bg-blue-600 text-white hover:bg-blue-700">Siguiente</button>
                ) : (
                  <button type="button" onClick={() => void submitAppointment()} disabled={saving} className="px-4 py-2 text-sm rounded-lg bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-60">
                    {saving ? 'Registrando...' : 'Registrar cita'}
                  </button>
                )}
              </div>
            </div>
          </form>
        </section>
        <section className="rounded-xl border border-blue-200 bg-white shadow-sm p-4 lg:p-5">
          <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-3 mb-4">
            <div>
              <h3 className="text-lg font-bold text-slate-900">Citas registradas</h3>
              <p className="text-sm text-slate-600 mt-1">Seguimiento del estado administrativo de tus solicitudes.</p>
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
                  </tr>
                </thead>
                <tbody className="divide-y divide-blue-100 bg-white">
                  {filteredAppointments.map((item) => (
                    <tr key={item.citaMedicaId} className="align-top hover:bg-sky-50/60 transition">
                      <td className="px-3 py-3 text-slate-700">
                        <div className="font-semibold text-sm text-slate-900">Cita #{item.citaMedicaId}</div>
                        <div className="text-slate-500 mt-1">Paciente ID: {item.pacienteId}</div>
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
export default AppointmentManagement






