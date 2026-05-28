import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { patientAPI, type PatientLookupResponse } from '@/services/api'
import useSidebarPreference from '@/hooks/useSidebarPreference'

type ConsultationType = '' | 'WITH_APPOINTMENT' | 'WALK_IN'

function mapPatientToFormData(patient: PatientLookupResponse) {
  return {
    nombreCompleto: patient.nombreCompleto,
    dpi: patient.dpi,
    fechaNacimiento: patient.fechaNacimiento || '',
    genero: patient.genero || '',
    telefono: patient.telefono || '',
    email: patient.emailContacto || '',
    direccion: patient.direccion || '',
    contactoEmergencia: patient.contactoEmergencia || '',
    telefonoEmergencia: patient.telefonoEmergencia || '',
  }
}

const TriageConsultationSelection: React.FC = () => {
  const navigate = useNavigate()
  const { collapsed: sidebarCollapsed, toggleCollapsed } = useSidebarPreference(
    'admin-triage-consultation-selection',
    false,
  )

  const [consultationType, setConsultationType] = useState<ConsultationType>('')
  const [dpiSearch, setDpiSearch] = useState('')
  const [searching, setSearching] = useState(false)
  const [error, setError] = useState('')
  const [patientLookup, setPatientLookup] = useState<PatientLookupResponse | null>(null)
  const [lookupState, setLookupState] = useState<'IDLE' | 'FOUND' | 'NOT_FOUND'>('IDLE')

  const handleSelectWithAppointment = () => {
    setConsultationType('WITH_APPOINTMENT')
    setDpiSearch('')
    setError('')
    setPatientLookup(null)
    setLookupState('IDLE')
  }

  const handleSelectWalkIn = () => {
    setConsultationType('WALK_IN')
    setError('')
    setPatientLookup(null)
    setLookupState('IDLE')
  }

  const handleBackToOptions = () => {
    setConsultationType('')
    setDpiSearch('')
    setError('')
    setPatientLookup(null)
    setLookupState('IDLE')
  }

  const handleDpiChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const sanitized = e.target.value.replace(/\D/g, '').slice(0, 13)
    setDpiSearch(sanitized)
    setError('')
    setLookupState('IDLE')
    setPatientLookup(null)
  }

  const goToTriageIntake = (params: Record<string, string>) => {
    const search = new URLSearchParams(params)
    navigate(`/triage/intake?${search.toString()}`)
  }

  const handleSearchByDpi = async () => {
    if (dpiSearch.length !== 13) {
      setError('El DPI debe tener exactamente 13 digitos.')
      return
    }

    setSearching(true)
    setError('')
    setPatientLookup(null)
    setLookupState('IDLE')

    try {
      const { data } = await patientAPI.lookupByDpi(dpiSearch)
      setPatientLookup(data)
      setLookupState('FOUND')
      setError('Paciente con registro previo encontrado. Presiona Continuar para saltar a Validación de pago.')
    } catch (err: any) {
      const status = err?.response?.status
      if (status === 404) {
        setLookupState('NOT_FOUND')
        setError('No se encontró registro previo con este DPI. Puedes crear un registro nuevo.')
      } else {
        const backendMsg = err?.response?.data?.errorMessage || err?.response?.data?.message
        setLookupState('IDLE')
        setError(backendMsg || 'No se pudo buscar el paciente por DPI. Intenta nuevamente.')
      }
    } finally {
      setSearching(false)
    }
  }

  const handleContinueWithFoundPatient = () => {
    if (!patientLookup) return

    goToTriageIntake({
      mode: 'WALK_IN',
      skipToInsurance: 'true',
      patientDataJson: JSON.stringify(mapPatientToFormData(patientLookup)),
    })
  }

  const handleCreateNewRecord = () => {
    goToTriageIntake({
      mode: 'WALK_IN',
      dpi: dpiSearch,
    })
  }

  return (
    <div className="h-screen bg-gray-100 overflow-hidden flex">
      <aside
        className={`bg-blue-100/85 border-r border-blue-200 shadow-sm p-4 flex flex-col justify-between shrink-0 transition-all duration-300 ${
          sidebarCollapsed ? 'w-20' : 'w-64'
        }`}
      >
        <div>
          <div className={`mb-7 ${sidebarCollapsed ? 'flex flex-col items-center gap-3' : ''}`}>
            {!sidebarCollapsed && (
              <>
                <p className="text-xs uppercase tracking-[0.2em] text-slate-400">HIS</p>
                <h1 className="text-xl font-bold text-slate-900 mt-1">Triaje</h1>
                <p className="text-xs text-slate-600 mt-1">Selección de consulta</p>
              </>
            )}
            <button
              type="button"
              onClick={toggleCollapsed}
              className="px-2.5 py-1.5 rounded-lg bg-white hover:bg-slate-50 text-slate-700 border border-blue-200 font-semibold text-xs"
              title={sidebarCollapsed ? 'Expandir menú' : 'Ocultar menú'}
            >
              {sidebarCollapsed ? '>>' : '<<'}
            </button>
          </div>
        </div>

        <div className="space-y-3">
          <Link
            to="/admin"
            className="w-full px-4 py-2.5 rounded-xl border border-blue-200 bg-gradient-to-r from-white to-blue-50 hover:from-blue-50 hover:to-white text-blue-700 font-semibold text-sm text-center shadow-sm transition"
            title="Regresar al dashboard"
          >
            {sidebarCollapsed ? '<<' : '<< Regresar al Dashboard'}
          </Link>
        </div>
      </aside>

      <main className="flex-1 p-6 lg:p-8 flex justify-center items-center overflow-auto">
        <section className="w-full max-w-2xl bg-white rounded-xl shadow-md border border-gray-100 p-6 lg:p-8">
          {!consultationType ? (
            <div className="space-y-6">
              <div className="text-center pb-6 border-b border-gray-200">
                <h2 className="text-2xl font-bold text-gray-900">Tipo de Ingreso</h2>
                <p className="text-sm text-gray-600 mt-2">Selecciona cómo ingresa el paciente al sistema</p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <button
                  type="button"
                  onClick={handleSelectWithAppointment}
                  className="p-6 rounded-lg border-2 border-gray-200 bg-white hover:border-indigo-400 hover:bg-indigo-50 transition group"
                >
                  <div className="text-4xl mb-3 group-hover:scale-110 transition">📅</div>
                  <h3 className="font-semibold text-gray-900 text-lg">Llega con cita programada</h3>
                  <p className="text-sm text-gray-600 mt-2">Paciente con cita previamente agendada</p>
                </button>

                <button
                  type="button"
                  onClick={handleSelectWalkIn}
                  className="p-6 rounded-lg border-2 border-gray-200 bg-white hover:border-slate-900 hover:bg-slate-50 transition group"
                >
                  <div className="text-4xl mb-3 group-hover:scale-110 transition">⚕️</div>
                  <h3 className="font-semibold text-gray-900 text-lg">Ingreso por Triaje (sin cita)</h3>
                  <p className="text-sm text-gray-600 mt-2">Paciente sin cita previa - búsqueda por DPI</p>
                </button>
              </div>
            </div>
          ) : consultationType === 'WITH_APPOINTMENT' ? (
            <div className="space-y-6">
              <div className="text-center pb-6 border-b border-gray-200">
                <div className="flex items-center justify-center gap-3 mb-4">
                  <div className="px-3 py-2 rounded-lg bg-indigo-100">
                    <span className="text-2xl">📅</span>
                  </div>
                  <div className="text-left">
                    <h2 className="text-2xl font-bold text-gray-900">Con Cita Programada</h2>
                    <p className="text-sm text-gray-600 mt-1">El flujo continuará con la cita médica en fase 1</p>
                  </div>
                </div>
              </div>

              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <p className="text-sm text-blue-900">
                  <span className="font-semibold">👉 Próximo paso:</span> ingresarás al formulario de triaje
                  donde deberás escribir el ID de la cita programada.
                </p>
              </div>

              <div className="flex gap-3">
                <button
                  type="button"
                  onClick={handleBackToOptions}
                  className="px-4 py-3 rounded-lg border border-gray-300 text-gray-700 font-semibold hover:bg-gray-50 transition"
                >
                  Atrás
                </button>
                <button
                  type="button"
                  onClick={() => goToTriageIntake({ mode: 'WITH_APPOINTMENT' })}
                  className="flex-1 px-4 py-3 rounded-lg bg-indigo-600 text-white font-semibold hover:bg-indigo-700 transition"
                >
                  Continuar con Cita Programada
                </button>
              </div>
            </div>
          ) : (
            <div className="space-y-6">
              <div className="text-center pb-6 border-b border-gray-200">
                <div className="flex items-center justify-center gap-3 mb-4">
                  <div className="px-3 py-2 rounded-lg bg-slate-100">
                    <span className="text-2xl">⚕️</span>
                  </div>
                  <div className="text-left">
                    <h2 className="text-2xl font-bold text-gray-900">Ingreso por Triaje (sin cita)</h2>
                    <p className="text-sm text-gray-600 mt-1">Buscar por DPI para verificar registro previo</p>
                  </div>
                </div>
              </div>

              {error && (
                <div className={`text-sm rounded-lg px-4 py-3 border ${lookupState === 'FOUND' ? 'bg-emerald-50 border-emerald-200 text-emerald-700' : 'bg-red-100 border-red-200 text-red-700'}`}>
                  {error}
                </div>
              )}

              <div className="space-y-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-2">DPI del Paciente (13 dígitos)</label>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={dpiSearch}
                      onChange={handleDpiChange}
                      inputMode="numeric"
                      maxLength={13}
                      placeholder="Ej. 1234567890101"
                      disabled={searching}
                      className="flex-1 px-4 py-3 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600 disabled:bg-gray-50"
                    />
                    <button
                      type="button"
                      onClick={handleSearchByDpi}
                      disabled={searching || dpiSearch.length !== 13}
                      className="px-4 py-3 rounded-lg bg-slate-900 text-white font-semibold hover:bg-slate-800 disabled:bg-gray-400 disabled:cursor-not-allowed transition"
                    >
                      {searching ? 'Buscando...' : 'Buscar'}
                    </button>
                  </div>
                </div>

                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <p className="text-sm text-blue-900">
                    <span className="font-semibold">ℹ️ Qué sucede:</span>
                  </p>
                  <ul className="list-disc list-inside text-sm text-blue-800 mt-2 space-y-1">
                    <li>Si encontramos un registro previo, podrás continuar y saltar a validación de pago.</li>
                    <li>Si no encontramos registro, podrás crear un registro nuevo.</li>
                  </ul>
                </div>

                {lookupState === 'FOUND' && patientLookup && (
                  <div className="rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-900 space-y-1">
                    <p className="font-semibold">Paciente con registro previo encontrado</p>
                    <p>{patientLookup.nombreCompleto}</p>
                    <p>DPI: {patientLookup.dpi}</p>
                  </div>
                )}

                {lookupState === 'NOT_FOUND' && (
                  <div className="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
                    No existe registro previo para este DPI. Puedes crear un registro nuevo.
                  </div>
                )}
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  type="button"
                  onClick={handleBackToOptions}
                  disabled={searching}
                  className="px-4 py-3 rounded-lg border border-gray-300 text-gray-700 font-semibold hover:bg-gray-50 disabled:opacity-60 transition"
                >
                  Atrás
                </button>

                {lookupState === 'FOUND' ? (
                  <button
                    type="button"
                    onClick={handleContinueWithFoundPatient}
                    className="flex-1 px-4 py-3 rounded-lg bg-emerald-600 text-white font-semibold hover:bg-emerald-700 transition"
                  >
                    Continuar
                  </button>
                ) : (
                  <button
                    type="button"
                    onClick={handleCreateNewRecord}
                    disabled={searching}
                    className="flex-1 px-4 py-3 rounded-lg bg-blue-600 text-white font-semibold hover:bg-blue-700 disabled:opacity-60 transition"
                  >
                    Crear registro Nuevo
                  </button>
                )}
              </div>
            </div>
          )}
        </section>
      </main>
    </div>
  )
}

export default TriageConsultationSelection
