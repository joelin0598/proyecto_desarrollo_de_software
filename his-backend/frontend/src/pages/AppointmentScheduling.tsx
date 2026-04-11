import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { appointmentAPI, AppointmentResponse } from '@/services/api'
import Header from '@/components/Header'

const SPECIALTIES = [
  'Cardiología',
  'Dermatología',
  'Medicina General',
  'Neurología',
  'Ortopedia',
  'Pediatría',
  'Ginecología',
  'Oftalmología',
]

const DOCTORS: Record<string, string[]> = {
  'Cardiología': ['Dr. García López', 'Dra. Morales Ruiz'],
  'Dermatología': ['Dra. Sánchez Pérez', 'Dr. Ramírez Castillo'],
  'Medicina General': ['Dr. Martínez Pérez', 'Dra. Torres Fuentes'],
  'Neurología': ['Dr. Hernández Vega', 'Dra. Jiménez Cruz'],
  'Ortopedia': ['Dr. López Domínguez', 'Dra. Vargas Salinas'],
  'Pediatría': ['Dra. Castro Mendoza', 'Dr. Rojas Aguilar'],
  'Ginecología': ['Dra. Flores Ríos', 'Dra. Núñez Delgado'],
  'Oftalmología': ['Dr. Reyes Guzmán', 'Dra. Ortega Blanco'],
}

const TIME_SLOTS = [
  '08:00', '08:30', '09:00', '09:30', '10:00', '10:30',
  '11:00', '11:30', '14:00', '14:30', '15:00', '15:30',
  '16:00', '16:30',
]

const AppointmentScheduling: React.FC = () => {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [step, setStep] = useState(1)
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState<AppointmentResponse | null>(null)
  const [error, setError] = useState('')
  const [myAppointments, setMyAppointments] = useState<AppointmentResponse[]>([])

  const [form, setForm] = useState({
    specialty: '',
    doctorName: '',
    appointmentDate: '',
    appointmentTime: '',
    reason: '',
    insurerName: '',
    policyNumber: '',
    holderDpi: '',
  })

  useEffect(() => {
    appointmentAPI.getMyAppointments()
      .then(res => setMyAppointments(res.data))
      .catch(() => {})
  }, [])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
    if (name === 'specialty') {
      setForm(prev => ({ ...prev, specialty: value, doctorName: '' }))
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const response = await appointmentAPI.schedule({
        specialty: form.specialty,
        doctorName: form.doctorName,
        appointmentDate: form.appointmentDate,
        appointmentTime: form.appointmentTime,
        reason: form.reason,
        insurerName: form.insurerName || undefined,
        policyNumber: form.policyNumber || undefined,
        holderDpi: form.holderDpi || undefined,
      })
      setResult(response.data)
      setStep(4)
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'Error al programar la cita. Intente nuevamente.')
    } finally {
      setLoading(false)
    }
  }

  const statusLabel = (status: string) => {
    if (status === 'VALIDATED') return { text: 'Validada ✅', className: 'bg-green-100 text-green-800' }
    if (status === 'PENDING_PAYMENT') return { text: 'Pendiente de Pago ⚠️', className: 'bg-yellow-100 text-yellow-800' }
    return { text: 'Cancelada ❌', className: 'bg-red-100 text-red-800' }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />

      <main className="flex-1 max-w-4xl mx-auto px-4 py-8 w-full">
        {/* Page Title */}
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-800">📅 Programar Nueva Cita</h1>
          <p className="text-gray-600 mt-1">Complete los pasos para agendar su cita médica.</p>
        </div>

        {/* Step Indicator */}
        {step < 4 && (
          <div className="flex items-center mb-8 gap-2">
            {[
              { n: 1, label: 'Especialidad' },
              { n: 2, label: 'Detalles' },
              { n: 3, label: 'Seguro' },
            ].map(({ n, label }) => (
              <React.Fragment key={n}>
                <div className={`flex items-center gap-2 ${step >= n ? 'text-blue-600' : 'text-gray-400'}`}>
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold border-2 
                    ${step > n ? 'bg-blue-600 border-blue-600 text-white' : step === n ? 'border-blue-600 text-blue-600' : 'border-gray-300 text-gray-400'}`}>
                    {step > n ? '✓' : n}
                  </div>
                  <span className="text-sm font-medium hidden sm:block">{label}</span>
                </div>
                {n < 3 && <div className={`flex-1 h-0.5 ${step > n ? 'bg-blue-600' : 'bg-gray-200'}`} />}
              </React.Fragment>
            ))}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* Step 1: Specialty & Doctor */}
          {step === 1 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-lg font-bold text-gray-800 mb-4">Paso 1: Seleccione Especialidad y Médico</h2>
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Especialidad *</label>
                  <select
                    name="specialty"
                    value={form.specialty}
                    onChange={handleChange}
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    required
                  >
                    <option value="">Seleccione una especialidad</option>
                    {SPECIALTIES.map(s => (
                      <option key={s} value={s}>{s}</option>
                    ))}
                  </select>
                </div>

                {form.specialty && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Médico disponible *</label>
                    <select
                      name="doctorName"
                      value={form.doctorName}
                      onChange={handleChange}
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                      required
                    >
                      <option value="">Seleccione un médico</option>
                      {(DOCTORS[form.specialty] || []).map(d => (
                        <option key={d} value={d}>{d}</option>
                      ))}
                    </select>
                  </div>
                )}

                {form.specialty && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Horario disponible *</label>
                    <div className="grid grid-cols-4 sm:grid-cols-7 gap-2">
                      {TIME_SLOTS.map(t => (
                        <button
                          key={t}
                          type="button"
                          onClick={() => setForm(prev => ({ ...prev, appointmentTime: t }))}
                          className={`py-2 px-1 rounded text-sm font-medium border transition
                            ${form.appointmentTime === t
                              ? 'bg-blue-600 text-white border-blue-600'
                              : 'bg-white text-gray-700 border-gray-300 hover:border-blue-400'}`}
                        >
                          {t}
                        </button>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              <div className="mt-6 flex justify-end">
                <button
                  type="button"
                  onClick={() => setStep(2)}
                  disabled={!form.specialty || !form.doctorName || !form.appointmentTime}
                  className="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-300 text-white px-6 py-2 rounded-lg font-semibold transition"
                >
                  Siguiente →
                </button>
              </div>
            </div>
          )}

          {/* Step 2: Date & Reason */}
          {step === 2 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-lg font-bold text-gray-800 mb-4">Paso 2: Fecha y Motivo de Consulta</h2>
              <div className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Fecha de la cita *</label>
                    <input
                      type="date"
                      name="appointmentDate"
                      value={form.appointmentDate}
                      onChange={handleChange}
                      min={new Date().toISOString().split('T')[0]}
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Hora seleccionada</label>
                    <input
                      type="text"
                      value={form.appointmentTime}
                      readOnly
                      className="w-full border border-gray-200 bg-gray-50 rounded-lg px-3 py-2 text-gray-600"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Motivo de consulta *</label>
                  <textarea
                    name="reason"
                    value={form.reason}
                    onChange={handleChange}
                    rows={3}
                    placeholder="Describa brevemente el motivo de su consulta..."
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    required
                  />
                </div>

                <div className="bg-blue-50 rounded-lg p-4 text-sm text-blue-800">
                  <p className="font-semibold">Resumen de su selección:</p>
                  <p>🏥 Especialidad: <strong>{form.specialty}</strong></p>
                  <p>👨‍⚕️ Médico: <strong>{form.doctorName}</strong></p>
                  <p>⏰ Horario: <strong>{form.appointmentTime}</strong></p>
                </div>
              </div>

              <div className="mt-6 flex justify-between">
                <button type="button" onClick={() => setStep(1)} className="text-gray-600 hover:text-gray-800 font-semibold px-4 py-2">
                  ← Anterior
                </button>
                <button
                  type="button"
                  onClick={() => setStep(3)}
                  disabled={!form.appointmentDate || !form.reason}
                  className="bg-blue-600 hover:bg-blue-700 disabled:bg-gray-300 text-white px-6 py-2 rounded-lg font-semibold transition"
                >
                  Siguiente →
                </button>
              </div>
            </div>
          )}

          {/* Step 3: Insurance */}
          {step === 3 && (
            <div className="bg-white rounded-lg shadow p-6">
              <h2 className="text-lg font-bold text-gray-800 mb-2">Paso 3: Información de Cobertura de Seguro</h2>
              <p className="text-gray-500 text-sm mb-4">Opcional. Si tiene seguro médico, ingrese sus datos para validar la cobertura en tiempo real.</p>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Nombre de la Aseguradora</label>
                  <input
                    type="text"
                    name="insurerName"
                    value={form.insurerName}
                    onChange={handleChange}
                    placeholder="Ej. Seguros Universales, AsisemGuatemala..."
                    className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Número de Póliza / Carné</label>
                    <input
                      type="text"
                      name="policyNumber"
                      value={form.policyNumber}
                      onChange={handleChange}
                      placeholder="Ej. POL-2024-001234"
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">DPI del Titular</label>
                    <input
                      type="text"
                      name="holderDpi"
                      value={form.holderDpi}
                      onChange={handleChange}
                      placeholder="Ej. 1234567890101"
                      className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                </div>

                <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 text-sm text-yellow-800">
                  <p className="font-semibold">ℹ️ Información sobre validación de seguro:</p>
                  <ul className="mt-1 list-disc list-inside space-y-1">
                    <li>El sistema consultará automáticamente con su aseguradora.</li>
                    <li>Si la cobertura es aprobada, se aplicará el deducible correspondiente.</li>
                    <li>Si no se puede validar, se aplicará la tarifa base provisional (Q350.00).</li>
                    <li>Sin seguro, la cita queda pendiente de pago en caja.</li>
                  </ul>
                </div>

                {error && (
                  <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 text-sm">
                    {error}
                  </div>
                )}
              </div>

              <div className="mt-6 flex justify-between">
                <button type="button" onClick={() => setStep(2)} className="text-gray-600 hover:text-gray-800 font-semibold px-4 py-2">
                  ← Anterior
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="bg-green-600 hover:bg-green-700 disabled:bg-gray-300 text-white px-8 py-2 rounded-lg font-semibold transition"
                >
                  {loading ? '⏳ Procesando...' : '✅ Confirmar Cita'}
                </button>
              </div>
            </div>
          )}
        </form>

        {/* Step 4: Confirmation */}
        {step === 4 && result && (
          <div className="bg-white rounded-lg shadow p-6">
            <div className="text-center mb-6">
              <div className="text-5xl mb-3">
                {result.status === 'VALIDATED' ? '✅' : '⚠️'}
              </div>
              <h2 className="text-xl font-bold text-gray-800">
                {result.status === 'VALIDATED' ? '¡Cita Programada Exitosamente!' : 'Cita Programada – Pendiente de Pago'}
              </h2>
              <p className="text-gray-600 mt-2">{result.message}</p>
            </div>

            <div className="bg-gray-50 rounded-lg p-4 space-y-2 text-sm mb-6">
              <div className="flex justify-between">
                <span className="text-gray-500">Número de Cita:</span>
                <span className="font-semibold"># {result.id}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Especialidad:</span>
                <span className="font-semibold">{result.specialty}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Médico:</span>
                <span className="font-semibold">{result.doctorName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Fecha:</span>
                <span className="font-semibold">{result.appointmentDate}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Hora:</span>
                <span className="font-semibold">{result.appointmentTime}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Estado:</span>
                <span className={`px-2 py-0.5 rounded text-xs font-semibold ${statusLabel(result.status).className}`}>
                  {statusLabel(result.status).text}
                </span>
              </div>
              {result.baseTariff && (
                <div className="flex justify-between">
                  <span className="text-gray-500">Tarifa Base:</span>
                  <span className="font-semibold">Q{result.baseTariff.toFixed(2)}</span>
                </div>
              )}
              {result.deductible != null && result.deductible > 0 && (
                <div className="flex justify-between">
                  <span className="text-gray-500">Deducible Seguro:</span>
                  <span className="font-semibold text-green-700">Q{result.deductible.toFixed(2)}</span>
                </div>
              )}
            </div>

            {result.auditNote && (
              <div className="bg-blue-50 rounded-lg p-3 text-xs text-blue-700 mb-4">
                <span className="font-semibold">Nota de auditoría: </span>{result.auditNote}
              </div>
            )}

            <div className="flex gap-3">
              <button
                onClick={() => { setStep(1); setForm({ specialty: '', doctorName: '', appointmentDate: '', appointmentTime: '', reason: '', insurerName: '', policyNumber: '', holderDpi: '' }); setResult(null) }}
                className="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg font-semibold transition"
              >
                Programar otra cita
              </button>
              <button
                onClick={() => navigate('/user')}
                className="flex-1 bg-gray-600 hover:bg-gray-700 text-white py-2 rounded-lg font-semibold transition"
              >
                Volver al Portal
              </button>
            </div>
          </div>
        )}

        {/* My Appointments */}
        {myAppointments.length > 0 && step < 4 && (
          <div className="mt-8 bg-white rounded-lg shadow overflow-hidden">
            <div className="bg-blue-50 px-6 py-4 border-b">
              <h2 className="text-lg font-bold text-gray-800">📋 Mis Citas Registradas</h2>
            </div>
            <div className="divide-y">
              {myAppointments.map(apt => {
                const sl = statusLabel(apt.status)
                return (
                  <div key={apt.id} className="px-6 py-4 flex justify-between items-center">
                    <div>
                      <p className="font-semibold text-gray-800">{apt.specialty} – {apt.doctorName}</p>
                      <p className="text-sm text-gray-500">{apt.appointmentDate} a las {apt.appointmentTime}</p>
                      <p className="text-xs text-gray-400 mt-1">{apt.reason}</p>
                    </div>
                    <span className={`text-xs font-semibold px-2 py-1 rounded ${sl.className}`}>
                      {sl.text}
                    </span>
                  </div>
                )
              })}
            </div>
          </div>
        )}
      </main>
    </div>
  )
}

export default AppointmentScheduling
