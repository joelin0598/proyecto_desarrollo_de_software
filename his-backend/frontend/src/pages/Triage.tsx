import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Header from '@/components/Header'
import {
  patientAPI,
  triageAPI,
  PatientRequest,
  PatientResponse,
  TriageRequest,
  TriageResponse,
  TriagePriority,
} from '@/services/api'

const PRIORITY_LABELS: Record<TriagePriority, string> = {
  RED: '🔴 Código Rojo – Emergencia extrema',
  ORANGE: '🟠 Naranja – Urgente',
  GREEN: '🟢 Verde – No urgente',
}

const PRIORITY_CLASSES: Record<TriagePriority, string> = {
  RED: 'bg-red-100 border-red-600 text-red-800',
  ORANGE: 'bg-orange-100 border-orange-500 text-orange-800',
  GREEN: 'bg-green-100 border-green-500 text-green-800',
}

const TriagePage: React.FC = () => {
  const navigate = useNavigate()

  // Step 1: Find/register patient
  const [dpiSearch, setDpiSearch] = useState('')
  const [patient, setPatient] = useState<PatientResponse | null>(null)
  const [patientForm, setPatientForm] = useState<PatientRequest>({
    fullName: '',
    dpi: '',
    birthDate: '',
    gender: '',
    phone: '',
    email: '',
    address: '',
    emergencyContactName: '',
    emergencyContactPhone: '',
    insurancePolicyNumber: '',
    insuranceProvider: '',
  })
  const [showPatientForm, setShowPatientForm] = useState(false)
  const [patientError, setPatientError] = useState('')

  // Step 2: Vital signs
  const [vitalSigns, setVitalSigns] = useState<Omit<TriageRequest, 'patientId'>>({
    systolicPressure: undefined,
    diastolicPressure: undefined,
    heartRate: undefined,
    temperature: undefined,
    oxygenSaturation: undefined,
    weight: undefined,
    notes: '',
  })

  // Result
  const [triageResult, setTriageResult] = useState<TriageResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSearchDpi = async () => {
    if (!dpiSearch.trim()) return
    setPatientError('')
    setPatient(null)
    setShowPatientForm(false)
    try {
      const { data } = await patientAPI.findByDpi(dpiSearch.trim())
      setPatient(data)
    } catch {
      setShowPatientForm(true)
      setPatientForm((f) => ({ ...f, dpi: dpiSearch.trim() }))
      setPatientError('Paciente no encontrado. Complete el formulario para registrarlo.')
    }
  }

  const handleRegisterPatient = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setPatientError('')
    try {
      const { data } = await patientAPI.register(patientForm)
      setPatient(data)
      setShowPatientForm(false)
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { errorMessage?: string } } })
        ?.response?.data?.errorMessage ?? 'Error al registrar paciente'
      setPatientError(message)
    } finally {
      setLoading(false)
    }
  }

  const handleRecordTriage = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!patient) return
    setLoading(true)
    setError('')
    try {
      const { data } = await triageAPI.record({
        patientId: patient.patientId,
        ...vitalSigns,
      })
      setTriageResult(data)
    } catch (err: unknown) {
      const message = (err as { response?: { data?: { errorMessage?: string } } })
        ?.response?.data?.errorMessage ?? 'Error al registrar triaje'
      setError(message)
    } finally {
      setLoading(false)
    }
  }

  const handleReset = () => {
    setDpiSearch('')
    setPatient(null)
    setShowPatientForm(false)
    setPatientError('')
    setVitalSigns({
      systolicPressure: undefined,
      diastolicPressure: undefined,
      heartRate: undefined,
      temperature: undefined,
      oxygenSaturation: undefined,
      weight: undefined,
      notes: '',
    })
    setTriageResult(null)
    setError('')
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />

      <main className="flex-1 max-w-3xl mx-auto px-4 py-8 w-full">
        <div className="flex items-center gap-4 mb-6">
          <button
            onClick={() => navigate('/admin')}
            className="text-sm text-blue-600 hover:underline"
          >
            ← Volver al Dashboard
          </button>
          <h1 className="text-2xl font-bold text-gray-800">
            Registro y Triaje de Paciente (CU-2)
          </h1>
        </div>

        {/* ── Resultado de triaje ─────────────────────────── */}
        {triageResult && (
          <div
            className={`rounded-lg border-l-4 p-6 mb-6 ${PRIORITY_CLASSES[triageResult.priority]}`}
          >
            <h2 className="text-xl font-bold mb-2">
              Triaje registrado – {PRIORITY_LABELS[triageResult.priority]}
            </h2>
            <p>
              <strong>Paciente:</strong> {triageResult.patient.fullName}
            </p>
            <p>
              <strong>Hora de llegada:</strong>{' '}
              {new Date(triageResult.arrivalTime).toLocaleString()}
            </p>
            {triageResult.priority === 'RED' && (
              <p className="mt-3 font-bold text-red-700">
                ⚠️ Notificación enviada a médicos de turno. Traslado inmediato a atención.
              </p>
            )}
            <button
              onClick={handleReset}
              className="mt-4 bg-white border border-current px-4 py-2 rounded-lg text-sm font-semibold hover:bg-gray-50 transition"
            >
              Registrar nuevo paciente
            </button>
          </div>
        )}

        {!triageResult && (
          <>
            {/* ── Paso 1: Buscar / registrar paciente ──────── */}
            <section className="bg-white rounded-lg shadow p-6 mb-6">
              <h2 className="text-lg font-bold text-gray-800 mb-4">
                Paso 1 – Identificar paciente
              </h2>

              <div className="flex gap-2 mb-4">
                <input
                  type="text"
                  placeholder="Buscar por DPI / CUI"
                  value={dpiSearch}
                  onChange={(e) => setDpiSearch(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handleSearchDpi()}
                  className="flex-1 border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <button
                  onClick={handleSearchDpi}
                  className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg text-sm font-semibold transition"
                >
                  Buscar
                </button>
              </div>

              {patientError && (
                <p className="text-sm text-orange-600 mb-3">{patientError}</p>
              )}

              {patient && (
                <div className="bg-blue-50 rounded-lg p-4 border border-blue-200">
                  <p className="font-bold text-blue-900">{patient.fullName}</p>
                  <p className="text-sm text-blue-700">DPI: {patient.dpi}</p>
                  {patient.phone && (
                    <p className="text-sm text-blue-700">Tel: {patient.phone}</p>
                  )}
                </div>
              )}

              {/* Formulario de nuevo paciente (FA01) */}
              {showPatientForm && (
                <form onSubmit={handleRegisterPatient} className="mt-4 space-y-3">
                  <p className="text-sm font-semibold text-gray-700">
                    FA01 – Registrar nuevo paciente
                  </p>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">Nombre completo *</label>
                      <input
                        required
                        type="text"
                        value={patientForm.fullName}
                        onChange={(e) =>
                          setPatientForm((f) => ({ ...f, fullName: e.target.value }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">DPI / CUI</label>
                      <input
                        type="text"
                        value={patientForm.dpi}
                        onChange={(e) =>
                          setPatientForm((f) => ({ ...f, dpi: e.target.value }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">Fecha de nacimiento</label>
                      <input
                        type="date"
                        value={patientForm.birthDate}
                        onChange={(e) =>
                          setPatientForm((f) => ({ ...f, birthDate: e.target.value }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">Género</label>
                      <select
                        value={patientForm.gender}
                        onChange={(e) =>
                          setPatientForm((f) => ({ ...f, gender: e.target.value }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      >
                        <option value="">Seleccionar</option>
                        <option value="Masculino">Masculino</option>
                        <option value="Femenino">Femenino</option>
                        <option value="Otro">Otro</option>
                      </select>
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">Teléfono</label>
                      <input
                        type="text"
                        value={patientForm.phone}
                        onChange={(e) =>
                          setPatientForm((f) => ({ ...f, phone: e.target.value }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">Correo electrónico</label>
                      <input
                        type="email"
                        value={patientForm.email}
                        onChange={(e) =>
                          setPatientForm((f) => ({ ...f, email: e.target.value }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div className="md:col-span-2">
                      <label className="block text-xs text-gray-500 mb-1">Dirección</label>
                      <input
                        type="text"
                        value={patientForm.address}
                        onChange={(e) =>
                          setPatientForm((f) => ({ ...f, address: e.target.value }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">Contacto de emergencia</label>
                      <input
                        type="text"
                        placeholder="Nombre"
                        value={patientForm.emergencyContactName}
                        onChange={(e) =>
                          setPatientForm((f) => ({
                            ...f,
                            emergencyContactName: e.target.value,
                          }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">Tel. emergencia</label>
                      <input
                        type="text"
                        value={patientForm.emergencyContactPhone}
                        onChange={(e) =>
                          setPatientForm((f) => ({
                            ...f,
                            emergencyContactPhone: e.target.value,
                          }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">Aseguradora</label>
                      <input
                        type="text"
                        value={patientForm.insuranceProvider}
                        onChange={(e) =>
                          setPatientForm((f) => ({
                            ...f,
                            insuranceProvider: e.target.value,
                          }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">Número de póliza</label>
                      <input
                        type="text"
                        value={patientForm.insurancePolicyNumber}
                        onChange={(e) =>
                          setPatientForm((f) => ({
                            ...f,
                            insurancePolicyNumber: e.target.value,
                          }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                  </div>

                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-green-600 hover:bg-green-700 text-white py-2 rounded-lg text-sm font-semibold transition disabled:opacity-60"
                  >
                    {loading ? 'Registrando...' : 'Registrar Paciente'}
                  </button>
                </form>
              )}
            </section>

            {/* ── Paso 2: Signos vitales ─────────────────── */}
            {patient && (
              <section className="bg-white rounded-lg shadow p-6">
                <h2 className="text-lg font-bold text-gray-800 mb-4">
                  Paso 2 – Signos Vitales
                </h2>
                <p className="text-sm text-gray-600 mb-4">
                  Paciente: <strong>{patient.fullName}</strong>
                </p>

                <form onSubmit={handleRecordTriage} className="space-y-4">
                  <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">
                        Presión sistólica (mmHg)
                      </label>
                      <input
                        type="number"
                        step="0.1"
                        value={vitalSigns.systolicPressure ?? ''}
                        onChange={(e) =>
                          setVitalSigns((v) => ({
                            ...v,
                            systolicPressure: e.target.value ? Number(e.target.value) : undefined,
                          }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">
                        Presión diastólica (mmHg)
                      </label>
                      <input
                        type="number"
                        step="0.1"
                        value={vitalSigns.diastolicPressure ?? ''}
                        onChange={(e) =>
                          setVitalSigns((v) => ({
                            ...v,
                            diastolicPressure: e.target.value ? Number(e.target.value) : undefined,
                          }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">
                        Frecuencia cardíaca (bpm)
                      </label>
                      <input
                        type="number"
                        step="0.1"
                        value={vitalSigns.heartRate ?? ''}
                        onChange={(e) =>
                          setVitalSigns((v) => ({
                            ...v,
                            heartRate: e.target.value ? Number(e.target.value) : undefined,
                          }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">
                        Temperatura (°C)
                      </label>
                      <input
                        type="number"
                        step="0.1"
                        value={vitalSigns.temperature ?? ''}
                        onChange={(e) =>
                          setVitalSigns((v) => ({
                            ...v,
                            temperature: e.target.value ? Number(e.target.value) : undefined,
                          }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">
                        Saturación O₂ (%)
                      </label>
                      <input
                        type="number"
                        step="0.1"
                        value={vitalSigns.oxygenSaturation ?? ''}
                        onChange={(e) =>
                          setVitalSigns((v) => ({
                            ...v,
                            oxygenSaturation: e.target.value ? Number(e.target.value) : undefined,
                          }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">
                        Peso (kg)
                      </label>
                      <input
                        type="number"
                        step="0.1"
                        value={vitalSigns.weight ?? ''}
                        onChange={(e) =>
                          setVitalSigns((v) => ({
                            ...v,
                            weight: e.target.value ? Number(e.target.value) : undefined,
                          }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div className="col-span-2 md:col-span-3">
                      <label className="block text-xs text-gray-500 mb-1">
                        Notas adicionales
                      </label>
                      <textarea
                        rows={2}
                        value={vitalSigns.notes}
                        onChange={(e) =>
                          setVitalSigns((v) => ({ ...v, notes: e.target.value }))
                        }
                        className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                  </div>

                  {error && (
                    <p className="text-sm text-red-600">{error}</p>
                  )}

                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg text-sm font-semibold transition disabled:opacity-60"
                  >
                    {loading ? 'Procesando...' : 'Calcular Prioridad y Registrar Triaje'}
                  </button>
                </form>
              </section>
            )}
          </>
        )}
      </main>
    </div>
  )
}

export default TriagePage
