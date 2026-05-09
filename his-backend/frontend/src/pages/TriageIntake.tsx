import React, { useEffect, useMemo, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  catalogAPI,
  triageAPI,
  type InsuranceOption,
  type PatientGender,
  type PatientGenderOption,
  type TriageResponse,
} from '@/services/api'
import StatusChip from '@/components/ui/StatusChip'

type TriageFormData = {
  nombreCompleto: string
  dpi: string
  fechaNacimiento: string
  genero: '' | PatientGender
  telefono: string
  email: string
  direccion: string
  contactoEmergencia: string
  telefonoEmergencia: string
  insuranceMode: 'UNSELECTED' | 'NONE' | 'INSURED'
  aseguradoraId?: number
  polizaSeguro: string
  presionSistolica: string
  presionDiastolica: string
  frecuenciaCardiaca: string
  temperatura: string
  saturacionOxigeno: string
  pesoKg: string
  tallaCm: string
}

type SavedTriageData = TriageResponse & {
  formData: TriageFormData
}

const initialData: TriageFormData = {
  nombreCompleto: '',
  dpi: '',
  fechaNacimiento: '',
  genero: '',
  telefono: '',
  email: '',
  direccion: '',
  contactoEmergencia: '',
  telefonoEmergencia: '',
  insuranceMode: 'UNSELECTED',
  aseguradoraId: undefined,
  polizaSeguro: '',
  presionSistolica: '',
  presionDiastolica: '',
  frecuenciaCardiaca: '',
  temperatura: '',
  saturacionOxigeno: '',
  pesoKg: '',
  tallaCm: '',
}

type TriageStep = 'PERSONAL' | 'EMERGENCY' | 'INSURANCE' | 'VITALS'

const steps: Array<{ key: TriageStep; title: string }> = [
  { key: 'PERSONAL', title: '1. Datos personales' },
  { key: 'EMERGENCY', title: '2. Contacto emergencia' },
  { key: 'INSURANCE', title: '3. Seguro' },
  { key: 'VITALS', title: '4. Signos vitales' },
]

const PHONE_PATTERN = /^[0-9]{8,15}$/
const DPI_PATTERN = /^[0-9]{13}$/

function resolvePriority(form: TriageFormData): string {
  const saturacion = Number(form.saturacionOxigeno)
  const temperatura = Number(form.temperatura)

  if (Number.isNaN(saturacion) || Number.isNaN(temperatura)) {
    return 'PENDIENTE'
  }
  if (saturacion < 85 || temperatura >= 40) {
    return 'ROJO'
  }
  if (saturacion < 92 || temperatura >= 38.5) {
    return 'NARANJA'
  }
  if (saturacion < 95 || temperatura >= 37.5) {
    return 'AMARILLO'
  }
  return 'VERDE'
}

function validateRange(value: number, min: number, max: number): boolean {
  return value >= min && value <= max
}

function sanitizeIntegerInput(rawValue: string, maxDigits: number, maxValue: number): string {
  const digitsOnly = rawValue.replace(/\D/g, '').slice(0, maxDigits)
  if (!digitsOnly) {
    return ''
  }
  const numericValue = Number(digitsOnly)
  if (Number.isNaN(numericValue)) {
    return ''
  }
  return String(Math.min(numericValue, maxValue))
}

function getPriorityStatusChip(priority: string): { label: string; tone: 'blue' | 'emerald' | 'orange' | 'red' | 'slate' | 'yellow' } {
  switch (priority) {
    case 'ROJO':
      return { label: `Prioridad: ${priority}`, tone: 'red' }
    case 'NARANJA':
      return { label: `Prioridad: ${priority}`, tone: 'orange' }
    case 'AMARILLO':
      return { label: `Prioridad: ${priority}`, tone: 'yellow' }
    case 'VERDE':
      return { label: `Prioridad: ${priority}`, tone: 'emerald' }
    case 'PENDIENTE':
      return { label: 'Prioridad en curso de evaluacion', tone: 'slate' }
    default:
      return { label: 'Prioridad en curso de evaluacion', tone: 'slate' }
  }
}

function validateStep(formData: TriageFormData, step: TriageStep): string | null {
  if (step === 'PERSONAL') {
    if (!formData.nombreCompleto.trim()) return 'El nombre completo es obligatorio.'
    if (!DPI_PATTERN.test(formData.dpi.trim())) return 'El DPI debe tener exactamente 13 digitos.'
    if (!formData.fechaNacimiento) return 'La fecha de nacimiento es obligatoria.'
    if (!formData.genero) return 'Debes seleccionar un genero.'
    if (!PHONE_PATTERN.test(formData.telefono.trim())) return 'El telefono debe tener entre 8 y 15 digitos.'
    if (!formData.email.trim()) return 'El correo electronico es obligatorio.'
    if (!formData.direccion.trim()) return 'La direccion es obligatoria.'
    return null
  }

  if (step === 'EMERGENCY') {
    if (!formData.contactoEmergencia.trim()) return 'El nombre del contacto de emergencia es obligatorio.'
    if (!PHONE_PATTERN.test(formData.telefonoEmergencia.trim())) {
      return 'El telefono de emergencia debe tener entre 8 y 15 digitos.'
    }
    return null
  }

  if (step === 'INSURANCE') {
    if (formData.insuranceMode === 'UNSELECTED') {
      return 'Debes seleccionar una opcion de seguro: aseguradora o sin seguro.'
    }
    if (formData.insuranceMode === 'INSURED' && !formData.aseguradoraId) {
      return 'Debes seleccionar una aseguradora disponible.'
    }
    if (formData.insuranceMode === 'INSURED' && !formData.polizaSeguro.trim()) {
      return 'El numero de poliza es obligatorio cuando aplica seguro.'
    }
    return null
  }

  const presionSistolica = Number(formData.presionSistolica)
  const presionDiastolica = Number(formData.presionDiastolica)
  const frecuenciaCardiaca = Number(formData.frecuenciaCardiaca)
  const temperatura = Number(formData.temperatura)
  const saturacionOxigeno = Number(formData.saturacionOxigeno)
  const pesoKg = Number(formData.pesoKg)
  const tallaCm = Number(formData.tallaCm)

  if (!validateRange(presionSistolica, 50, 300)) return 'La presion sistolica esta fuera de rango clinico.'
  if (!validateRange(presionDiastolica, 30, 200)) return 'La presion diastolica esta fuera de rango clinico.'
  if (presionDiastolica >= presionSistolica) return 'La presion diastolica no puede ser mayor o igual a la sistolica.'
  if (!validateRange(frecuenciaCardiaca, 20, 250)) return 'La frecuencia cardiaca esta fuera de rango clinico.'
  if (!validateRange(temperatura, 10, 50)) return 'La temperatura debe estar entre 10 y 50 grados.'
  if (!validateRange(saturacionOxigeno, 0, 100)) return 'La saturacion de oxigeno debe estar entre 0 y 100.'
  if (!validateRange(pesoKg, 1, 999)) return 'El peso debe estar entre 1 y 999 kg.'
  if (!validateRange(tallaCm, 30, 300)) return 'La talla esta fuera de rango clinico.'

  return null
}

const TriageIntake: React.FC = () => {
  const [formData, setFormData] = useState<TriageFormData>(initialData)
  const [currentStepIndex, setCurrentStepIndex] = useState(0)
  const [genderOptions, setGenderOptions] = useState<PatientGenderOption[]>([])
  const [insuranceOptions, setInsuranceOptions] = useState<InsuranceOption[]>([])
  const [catalogLoading, setCatalogLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [savedTriageData, setSavedTriageData] = useState<SavedTriageData | null>(null)
  const submitLockRef = useRef(false)

  const currentStep = steps[currentStepIndex]
  const priority = useMemo(() => resolvePriority(formData), [formData])
  const genderLabelMap = useMemo(
    () => Object.fromEntries(genderOptions.map((option) => [option.code, option.label])) as Record<PatientGender, string>,
    [genderOptions]
  )
  const insuranceNameMap = useMemo(
    () => Object.fromEntries(insuranceOptions.map((option) => [String(option.id), option.nombre])) as Record<string, string>,
    [insuranceOptions]
  )

  useEffect(() => {
    const loadCatalogs = async () => {
      try {
        const [gendersResponse, insurancesResponse] = await Promise.all([
          catalogAPI.patientGenders(),
          catalogAPI.insurances(),
        ])
        setGenderOptions(gendersResponse.data)
        setInsuranceOptions(insurancesResponse.data)
      } catch {
        setError('No se pudieron cargar los catalogos de genero y aseguradoras.')
      } finally {
        setCatalogLoading(false)
      }
    }

    void loadCatalogs()
  }, [])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
    setError('')
  }

  const handleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
    setError('')
  }

  const handleLimitedIntegerChange =
    (
      name:
        | 'frecuenciaCardiaca'
        | 'presionDiastolica'
        | 'presionSistolica'
        | 'saturacionOxigeno'
        | 'pesoKg'
        | 'tallaCm'
        | 'temperatura',
      maxDigits: number,
      maxValue: number
    ) =>
      (e: React.ChangeEvent<HTMLInputElement>) => {
        const sanitizedValue = sanitizeIntegerInput(e.target.value, maxDigits, maxValue)
        setFormData((prev) => ({ ...prev, [name]: sanitizedValue }))
        setError('')
      }

  const selectInsurance = (insuranceId: number) => {
    setFormData((prev) => ({
      ...prev,
      insuranceMode: 'INSURED',
      aseguradoraId: insuranceId,
    }))
    setError('')
  }

  const selectNoInsurance = () => {
    setFormData((prev) => ({
      ...prev,
      insuranceMode: 'NONE',
      aseguradoraId: undefined,
      polizaSeguro: '',
    }))
    setError('')
  }

  const goNextStep = () => {
    const validationError = validateStep(formData, currentStep.key)
    if (validationError) {
      setError(validationError)
      return
    }

    if (currentStepIndex < steps.length - 1) {
      setCurrentStepIndex((prev) => prev + 1)
      setError('')
    }
  }

  const goPreviousStep = () => {
    if (currentStepIndex > 0) {
      setCurrentStepIndex((prev) => prev - 1)
      setError('')
    }
  }

  // Permite regresar a cualquier paso ya visitado sin perder lo capturado.
  const goToStep = (targetIndex: number) => {
    if (targetIndex <= currentStepIndex) {
      setCurrentStepIndex(targetIndex)
      setError('')
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (submitLockRef.current || submitting || savedTriageData) {
      return
    }

    const validationError = validateStep(formData, 'PERSONAL')
      ?? validateStep(formData, 'EMERGENCY')
      ?? validateStep(formData, 'INSURANCE')
      ?? validateStep(formData, 'VITALS')

    if (validationError) {
      setError(validationError)
      return
    }

    submitLockRef.current = true
    setSubmitting(true)
    setError('')
    let savedSuccessfully = false

    try {
      const payload = {
        nombreCompleto: formData.nombreCompleto,
        dpi: formData.dpi,
        fechaNacimiento: formData.fechaNacimiento || undefined,
        genero: formData.genero as PatientGender,
        emailContacto: formData.email || undefined,
        telefono: formData.telefono || undefined,
        direccion: formData.direccion || undefined,
        contactoEmergencia: formData.contactoEmergencia,
        telefonoEmergencia: formData.telefonoEmergencia,
        aseguradoraId: formData.insuranceMode === 'INSURED' ? formData.aseguradoraId : undefined,
        polizaSeguro: formData.insuranceMode === 'INSURED' ? formData.polizaSeguro || undefined : undefined,
        presionSistolica: Number(formData.presionSistolica),
        presionDiastolica: Number(formData.presionDiastolica),
        frecuenciaCardiaca: Number(formData.frecuenciaCardiaca),
        temperatura: Number(formData.temperatura),
        saturacionOxigeno: Number(formData.saturacionOxigeno),
        pesoKg: Number(formData.pesoKg),
        tallaCm: Number(formData.tallaCm),
      }

      const { data } = await triageAPI.create(payload)
      const result = data as TriageResponse
      setSavedTriageData({
        ...result,
        formData: { ...formData },
      })
      savedSuccessfully = true
    } catch (err: any) {
      const backendMsg = err?.response?.data?.errorMessage || err?.response?.data?.message
      setError(backendMsg || 'Error al registrar triaje. Verifica la conexión y los datos.')
    } finally {
      if (!savedSuccessfully) {
        submitLockRef.current = false
      }
      setSubmitting(false)
    }
  }

  const handleReset = () => {
    submitLockRef.current = false
    setFormData(initialData)
    setCurrentStepIndex(0)
    setError('')
    setSavedTriageData(null)
  }

  const priorityChip = getPriorityStatusChip(priority)

  return (
    <div className="h-screen bg-gray-100 overflow-hidden flex">
      {/* Sidebar reutilizable con patrón azul claro */}
      <aside className="w-64 bg-blue-100/85 border-r border-blue-200 shadow-sm p-4 flex flex-col justify-between shrink-0">
        <div>
          <div className="mb-7">
            <p className="text-xs uppercase tracking-[0.2em] text-slate-400">HIS</p>
            <h1 className="text-xl font-bold text-slate-900 mt-1">Triaje</h1>
            <p className="text-xs text-slate-600 mt-1">Ingreso y clasificación</p>
          </div>

          {/* Navegación de fases */}
          <nav className="space-y-2">
            {steps.map((step, index) => {
              const isActive = index === currentStepIndex
              const isVisited = index < currentStepIndex
              const isClickable = index <= currentStepIndex
              const label = step.title.replace(/^\d+\.\s/, '')

              return (
                <button
                  key={step.key}
                  type="button"
                  onClick={() => goToStep(index)}
                  disabled={!isClickable}
                  className={`w-full text-left px-3 py-2.5 rounded-lg text-sm transition ${
                    isActive
                      ? 'bg-white text-blue-700 border border-blue-200 font-semibold shadow-sm'
                      : isVisited
                        ? 'hover:bg-white/50 text-slate-700'
                        : isClickable
                          ? 'hover:bg-white/50 text-slate-700'
                          : 'text-slate-400 cursor-not-allowed'
                  }`}
                >
                  <div className="flex items-center gap-2">
                    <span className={`w-5 h-5 rounded-full flex items-center justify-center text-xs font-bold shrink-0 ${
                      isActive
                        ? 'bg-blue-600 text-white'
                        : isVisited
                          ? 'bg-emerald-500 text-white'
                          : 'bg-slate-300 text-slate-600'
                    }`}>
                      {isVisited ? '✓' : index + 1}
                    </span>
                    <span>{label}</span>
                  </div>
                </button>
              )
            })}
          </nav>
        </div>

        {/* Footer de sidebar */}
        <div className="space-y-3">
          <div className="rounded-lg border border-blue-200 bg-blue-50/70 p-3">
            <p className="text-xs text-slate-500">Fase actual</p>
            <p className="font-semibold text-slate-800">{currentStep.title}</p>
          </div>
          <Link
            to="/admin"
            className="w-full px-4 py-2 rounded-lg bg-white hover:bg-slate-50 text-slate-700 border border-blue-200 font-semibold text-sm text-center"
          >
            Volver al dashboard
          </Link>
        </div>
      </aside>

      <main className="flex-1 p-6 lg:p-8 flex items-center justify-center overflow-auto">
        <section className="w-full max-w-4xl bg-white rounded-xl shadow-md border border-gray-100 p-6 lg:p-8">
          {/* Pantalla de confirmación después de guardar */}
          {savedTriageData && (
            <div className="space-y-6">
              {/* Encabezado de confirmación */}
              <div className="flex items-start justify-between pb-6 border-b-2 border-emerald-200">
                <div>
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-emerald-100 flex items-center justify-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="w-6 h-6 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                      </svg>
                    </div>
                    <div>
                      <h2 className="text-2xl font-bold text-emerald-700">Triaje Registrado Correctamente</h2>
                      <p className="text-sm text-gray-600 mt-1">El paciente ha sido ingresado al sistema exitosamente</p>
                    </div>
                  </div>
                </div>
                <StatusChip
                  label={getPriorityStatusChip(savedTriageData.prioridad).label}
                  tone={getPriorityStatusChip(savedTriageData.prioridad).tone}
                />
              </div>

              {/* Ficha del paciente */}
              <div className="space-y-4">
                {/* IDs de registro */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="rounded-lg bg-blue-50 border border-blue-200 p-4">
                    <p className="text-xs text-blue-600 font-semibold uppercase mb-1">Paciente ID</p>
                    <p className="text-xl font-bold text-blue-900">{savedTriageData.pacienteId}</p>
                  </div>
                  <div className="rounded-lg bg-purple-50 border border-purple-200 p-4">
                    <p className="text-xs text-purple-600 font-semibold uppercase mb-1">Signos Vitales ID</p>
                    <p className="text-xl font-bold text-purple-900">{savedTriageData.signosVitalesId}</p>
                  </div>
                  <div className="rounded-lg bg-amber-50 border border-amber-200 p-4">
                    <p className="text-xs text-amber-600 font-semibold uppercase mb-1">Alerta Emergencia</p>
                    <p className="text-xl font-bold text-amber-900">{savedTriageData.alertaEmergencia ? '🔴 SÍ' : '✓ No'}</p>
                  </div>
                </div>

                {/* Datos del paciente */}
                <div className="rounded-lg border border-gray-200 overflow-hidden">
                  <div className="bg-gray-50 px-6 py-3 border-b border-gray-200">
                    <h3 className="font-semibold text-gray-900">Información del Paciente</h3>
                  </div>
                  <div className="divide-y divide-gray-200">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 px-6 py-4">
                      <div>
                        <p className="text-xs font-semibold text-gray-500 uppercase">Nombre completo</p>
                        <p className="text-sm text-gray-900 font-medium mt-1">{savedTriageData.formData.nombreCompleto}</p>
                      </div>
                      <div>
                        <p className="text-xs font-semibold text-gray-500 uppercase">DPI</p>
                        <p className="text-sm text-gray-900 font-medium mt-1">{savedTriageData.formData.dpi}</p>
                      </div>
                      <div>
                        <p className="text-xs font-semibold text-gray-500 uppercase">Fecha de nacimiento</p>
                        <p className="text-sm text-gray-900 font-medium mt-1">{savedTriageData.formData.fechaNacimiento || 'No especificada'}</p>
                      </div>
                      <div>
                        <p className="text-xs font-semibold text-gray-500 uppercase">Género</p>
                        <p className="text-sm text-gray-900 font-medium mt-1">{genderLabelMap[savedTriageData.formData.genero as PatientGender] || savedTriageData.formData.genero}</p>
                      </div>
                      <div>
                        <p className="text-xs font-semibold text-gray-500 uppercase">Teléfono</p>
                        <p className="text-sm text-gray-900 font-medium mt-1">{savedTriageData.formData.telefono}</p>
                      </div>
                      <div>
                        <p className="text-xs font-semibold text-gray-500 uppercase">Correo</p>
                        <p className="text-sm text-gray-900 font-medium mt-1">{savedTriageData.formData.email}</p>
                      </div>
                      <div className="md:col-span-2">
                        <p className="text-xs font-semibold text-gray-500 uppercase">Dirección</p>
                        <p className="text-sm text-gray-900 font-medium mt-1">{savedTriageData.formData.direccion}</p>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Contacto de emergencia */}
                <div className="rounded-lg border border-gray-200 overflow-hidden">
                  <div className="bg-gray-50 px-6 py-3 border-b border-gray-200">
                    <h3 className="font-semibold text-gray-900">Contacto de Emergencia</h3>
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 px-6 py-4">
                    <div>
                      <p className="text-xs font-semibold text-gray-500 uppercase">Nombre</p>
                      <p className="text-sm text-gray-900 font-medium mt-1">{savedTriageData.formData.contactoEmergencia}</p>
                    </div>
                    <div>
                      <p className="text-xs font-semibold text-gray-500 uppercase">Teléfono</p>
                      <p className="text-sm text-gray-900 font-medium mt-1">{savedTriageData.formData.telefonoEmergencia}</p>
                    </div>
                  </div>
                </div>

                {/* Información de seguro */}
                <div className="rounded-lg border border-gray-200 overflow-hidden">
                  <div className="bg-gray-50 px-6 py-3 border-b border-gray-200">
                    <h3 className="font-semibold text-gray-900">Seguro</h3>
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 px-6 py-4">
                    <div>
                      <p className="text-xs font-semibold text-gray-500 uppercase">Estado</p>
                      <p className="text-sm text-gray-900 font-medium mt-1">{savedTriageData.formData.insuranceMode === 'NONE' ? 'Sin seguro' : savedTriageData.formData.insuranceMode === 'INSURED' ? 'Con seguro' : 'No especificado'}</p>
                    </div>
                    {savedTriageData.formData.insuranceMode === 'INSURED' && savedTriageData.formData.aseguradoraId && (
                      <div>
                        <p className="text-xs font-semibold text-gray-500 uppercase">Aseguradora</p>
                        <p className="text-sm text-gray-900 font-medium mt-1">{insuranceNameMap[String(savedTriageData.formData.aseguradoraId)] || `ID ${savedTriageData.formData.aseguradoraId}`}</p>
                      </div>
                    )}
                    {savedTriageData.formData.insuranceMode === 'INSURED' && (
                      <div>
                        <p className="text-xs font-semibold text-gray-500 uppercase">Póliza</p>
                        <p className="text-sm text-gray-900 font-medium mt-1">{savedTriageData.formData.polizaSeguro}</p>
                      </div>
                    )}
                  </div>
                </div>

                {/* Signos vitales */}
                <div className="rounded-lg border border-gray-200 overflow-hidden">
                  <div className="bg-gray-50 px-6 py-3 border-b border-gray-200">
                    <h3 className="font-semibold text-gray-900">Signos Vitales</h3>
                  </div>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 px-6 py-4">
                    <div>
                      <p className="text-xs font-semibold text-gray-500 uppercase">P. Sistólica</p>
                      <p className="text-lg font-bold text-blue-600 mt-1">{savedTriageData.presionSistolica} mmHg</p>
                    </div>
                    <div>
                      <p className="text-xs font-semibold text-gray-500 uppercase">P. Diastólica</p>
                      <p className="text-lg font-bold text-blue-600 mt-1">{savedTriageData.presionDiastolica} mmHg</p>
                    </div>
                    <div>
                      <p className="text-xs font-semibold text-gray-500 uppercase">F. Cardíaca</p>
                      <p className="text-lg font-bold text-green-600 mt-1">{savedTriageData.frecuenciaCardiaca} bpm</p>
                    </div>
                    <div>
                      <p className="text-xs font-semibold text-gray-500 uppercase">Temperatura</p>
                      <p className="text-lg font-bold text-orange-600 mt-1">{savedTriageData.temperatura}°C</p>
                    </div>
                    <div>
                      <p className="text-xs font-semibold text-gray-500 uppercase">O2</p>
                      <p className="text-lg font-bold text-purple-600 mt-1">{savedTriageData.saturacionOxigeno}%</p>
                    </div>
                    <div>
                      <p className="text-xs font-semibold text-gray-500 uppercase">Peso</p>
                      <p className="text-lg font-bold text-gray-600 mt-1">{savedTriageData.pesoKg} kg</p>
                    </div>
                    <div>
                      <p className="text-xs font-semibold text-gray-500 uppercase">Talla</p>
                      <p className="text-lg font-bold text-gray-600 mt-1">{savedTriageData.tallaCm} cm</p>
                    </div>
                  </div>
                </div>
              </div>

              {/* Botones de acción */}
              <div className="flex gap-3 pt-4">
                <button
                  onClick={handleReset}
                  className="flex-1 px-4 py-3 rounded-lg bg-blue-600 text-white font-semibold hover:bg-blue-700 transition"
                >
                  Registrar Nuevo Triaje
                </button>
                <Link
                  to="/admin"
                  className="flex-1 px-4 py-3 rounded-lg border border-gray-300 text-gray-700 font-semibold hover:bg-gray-50 transition text-center"
                >
                  Volver al Dashboard
                </Link>
              </div>
            </div>
          )}

          {/* Formulario normal si aún no se ha guardado */}
          {!savedTriageData && (
            <>
              <div className="mb-6 flex items-start justify-between">
                <div>
                  <h2 className="text-2xl font-bold text-gray-900">Clasificación de Urgencia</h2>
                  <p className="text-sm text-gray-500 mt-1">Fase: {currentStep.title}</p>
                </div>
                <StatusChip label={priorityChip.label} tone={priorityChip.tone} />
              </div>

              <form onSubmit={handleSubmit} className="space-y-4">
                {error && <div className="text-sm bg-red-100 text-red-700 border border-red-200 rounded px-3 py-2">{error}</div>}

              {currentStep.key === 'PERSONAL' && (
                <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                  <div>
                    <label className="block text-xs font-semibold text-gray-600 mb-1">Nombre completo</label>
                    <input name="nombreCompleto" value={formData.nombreCompleto} onChange={handleChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600" />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-600 mb-1">DPI (CUI)</label>
                    <input name="dpi" value={formData.dpi} onChange={handleChange} pattern="^[0-9]{13}$" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600" />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-600 mb-1">Fecha de nacimiento</label>
                    <input type="date" name="fechaNacimiento" value={formData.fechaNacimiento} onChange={handleChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600" />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-600 mb-1">Genero</label>
                    <select
                      name="genero"
                      value={formData.genero}
                      onChange={handleSelectChange}
                      disabled={catalogLoading}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
                    >
                      <option value="">{catalogLoading ? 'Cargando...' : 'Selecciona genero'}</option>
                      {genderOptions.map((option) => (
                        <option key={option.code} value={option.code}>{option.label}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-600 mb-1">Telefono</label>
                    <input name="telefono" value={formData.telefono} onChange={handleChange} pattern="^[0-9]{8,15}$" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600" />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-600 mb-1">Correo electronico</label>
                    <input type="email" name="email" value={formData.email} onChange={handleChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600" />
                  </div>
                  <div className="md:col-span-3">
                    <label className="block text-xs font-semibold text-gray-600 mb-1">Direccion de residencia</label>
                    <input name="direccion" value={formData.direccion} onChange={handleChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600" />
                  </div>
                </div>
              )}

              {currentStep.key === 'EMERGENCY' && (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-semibold text-gray-600 mb-1">Nombre contacto emergencia</label>
                    <input
                      name="contactoEmergencia"
                      value={formData.contactoEmergencia}
                      onChange={handleChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-gray-600 mb-1">Telefono contacto emergencia</label>
                    <input
                      name="telefonoEmergencia"
                      value={formData.telefonoEmergencia}
                      onChange={handleChange}
                      pattern="^[0-9]{8,15}$"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
                    />
                  </div>
                </div>
              )}

              {currentStep.key === 'INSURANCE' && (
                <div className="space-y-4">
                  <div>
                    <p className="block text-xs font-semibold text-gray-600 mb-2">Selecciona aseguradora (si aplica)</p>
                    <div className="flex flex-wrap gap-2">
                      <button
                        type="button"
                        onClick={selectNoInsurance}
                        className={`px-3 py-2 text-sm rounded-lg border ${
                          formData.insuranceMode === 'NONE'
                            ? 'bg-slate-900 text-white border-slate-900'
                            : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
                        }`}
                      >
                        Sin seguro
                      </button>
                      {insuranceOptions.map((insurance) => {
                        const selected = formData.insuranceMode === 'INSURED' && formData.aseguradoraId === insurance.id
                        return (
                          <button
                            key={insurance.id}
                            type="button"
                            onClick={() => selectInsurance(insurance.id)}
                            className={`px-3 py-2 text-sm rounded-lg border ${
                              selected
                                ? 'bg-blue-600 text-white border-blue-600'
                                : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
                            }`}
                          >
                            {insurance.nombre}
                          </button>
                        )
                      })}
                    </div>
                  </div>

                  {formData.insuranceMode === 'INSURED' && (
                    <div className="max-w-md">
                      <label className="block text-xs font-semibold text-gray-600 mb-1">Numero de poliza</label>
                      <input
                        name="polizaSeguro"
                        value={formData.polizaSeguro}
                        onChange={handleChange}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
                        placeholder="Ingresa numero de poliza"
                      />
                    </div>
                  )}
                </div>
              )}

              {currentStep.key === 'VITALS' && (
                <div className="space-y-3">
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">Presion sistolica</label>
                      <input
                        type="number"
                        min={50}
                        max={300}
                        step={1}
                        inputMode="numeric"
                        name="presionSistolica"
                        value={formData.presionSistolica}
                        onChange={handleLimitedIntegerChange('presionSistolica', 3, 300)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">Presion diastolica</label>
                      <input
                        type="number"
                        min={30}
                        max={200}
                        step={1}
                        inputMode="numeric"
                        name="presionDiastolica"
                        value={formData.presionDiastolica}
                        onChange={handleLimitedIntegerChange('presionDiastolica', 3, 200)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">Frecuencia cardiaca</label>
                      <input
                        type="number"
                        min={20}
                        max={250}
                        step={1}
                        inputMode="numeric"
                        name="frecuenciaCardiaca"
                        value={formData.frecuenciaCardiaca}
                        onChange={handleLimitedIntegerChange('frecuenciaCardiaca', 3, 250)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">Temperatura</label>
                      <input
                        type="number"
                        step={1}
                        min={10}
                        max={50}
                        inputMode="numeric"
                        name="temperatura"
                        value={formData.temperatura}
                        onChange={handleLimitedIntegerChange('temperatura', 2, 50)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">Saturacion O2</label>
                      <input
                        type="number"
                        min={0}
                        max={100}
                        step={1}
                        inputMode="numeric"
                        name="saturacionOxigeno"
                        value={formData.saturacionOxigeno}
                        onChange={handleLimitedIntegerChange('saturacionOxigeno', 3, 100)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">Peso (kg)</label>
                      <input
                        type="number"
                        step={1}
                        min={1}
                        max={999}
                        inputMode="numeric"
                        name="pesoKg"
                        value={formData.pesoKg}
                        onChange={handleLimitedIntegerChange('pesoKg', 3, 999)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-gray-600 mb-1">Talla (cm)</label>
                      <input
                        type="number"
                        step={1}
                        min={30}
                        max={300}
                        inputMode="numeric"
                        name="tallaCm"
                        value={formData.tallaCm}
                        onChange={handleLimitedIntegerChange('tallaCm', 3, 300)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600"
                      />
                    </div>
                  </div>
                </div>
              )}

              <div className="flex justify-between gap-2 pt-1">
                <div className="text-xs text-gray-500 flex items-center">
                  Paso {currentStepIndex + 1} de {steps.length}
                </div>
                <div className="flex gap-2">
                  {currentStepIndex > 0 && (
                    <button type="button" onClick={goPreviousStep} disabled={submitting} className="px-4 py-2 text-sm border border-gray-300 rounded-lg text-gray-600 hover:bg-gray-50 disabled:opacity-60 disabled:cursor-not-allowed">
                      Anterior
                    </button>
                  )}
                <button type="button" onClick={handleReset} disabled={submitting} className="px-4 py-2 text-sm border border-gray-300 rounded-lg text-gray-600 hover:bg-gray-50 disabled:opacity-60 disabled:cursor-not-allowed">Limpiar</button>
                  {currentStepIndex < steps.length - 1 ? (
                    <button type="button" onClick={goNextStep} className="px-4 py-2 text-sm rounded-lg bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-60 disabled:cursor-not-allowed" disabled={submitting || (catalogLoading && (currentStep.key === 'PERSONAL' || currentStep.key === 'INSURANCE'))}>
                      Siguiente fase
                    </button>
                  ) : (
                    <button type="submit" disabled={submitting || !!savedTriageData} className="px-4 py-2 text-sm rounded-lg bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-60 disabled:cursor-not-allowed">
                      {submitting ? 'Guardando...' : 'Guardar triaje'}
                    </button>
                  )}
                </div>
               </div>
             </form>
            </>
          )}
        </section>
      </main>
    </div>
  )
}

export default TriageIntake

