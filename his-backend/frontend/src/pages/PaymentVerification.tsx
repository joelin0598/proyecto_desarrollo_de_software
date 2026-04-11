import React, { useState, useEffect, useCallback } from 'react'
import { paymentAPI, appointmentAPI, AppointmentResponse, PaymentResponse } from '@/services/api'
import Header from '@/components/Header'

const PaymentVerification: React.FC = () => {
  const [pendingPatients, setPendingPatients] = useState<AppointmentResponse[]>([])
  const [selectedAppointment, setSelectedAppointment] = useState<AppointmentResponse | null>(null)
  const [paymentResult, setPaymentResult] = useState<PaymentResponse | null>(null)
  const [loading, setLoading] = useState(false)
  const [loadingList, setLoadingList] = useState(false)
  const [error, setError] = useState('')

  const [form, setForm] = useState({
    paymentMethod: 'CASH' as 'CASH' | 'CARD' | 'INSURANCE',
    authorizationNumber: '',
    totalAmount: '',
    insuranceCoverage: '',
    pendingBalance: '',
    invoiceNumber: '',
    emergencyBypass: false,
  })

  const loadPendingPatients = useCallback(async () => {
    setLoadingList(true)
    try {
      const res = await paymentAPI.getPending()
      setPendingPatients(res.data)
    } catch {
      setError('Error al cargar la lista de pacientes pendientes.')
    } finally {
      setLoadingList(false)
    }
  }, [])

  useEffect(() => {
    loadPendingPatients()
  }, [loadPendingPatients])

  const handleSelectAppointment = (apt: AppointmentResponse) => {
    setSelectedAppointment(apt)
    setPaymentResult(null)
    setError('')
    setForm({
      paymentMethod: 'CASH',
      authorizationNumber: '',
      totalAmount: apt.baseTariff ? String(apt.baseTariff) : '',
      insuranceCoverage: apt.deductible ? String(apt.deductible) : '',
      pendingBalance: '',
      invoiceNumber: `REC-${Date.now()}`,
      emergencyBypass: false,
    })
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target
    if (type === 'checkbox') {
      setForm(prev => ({ ...prev, [name]: (e.target as HTMLInputElement).checked }))
    } else {
      setForm(prev => ({ ...prev, [name]: value }))
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedAppointment) return
    setLoading(true)
    setError('')
    try {
      const res = await paymentAPI.registerPayment({
        appointmentId: selectedAppointment.id,
        paymentMethod: form.paymentMethod,
        authorizationNumber: form.authorizationNumber || undefined,
        totalAmount: parseFloat(form.totalAmount) || 0,
        insuranceCoverage: form.insuranceCoverage ? parseFloat(form.insuranceCoverage) : undefined,
        pendingBalance: form.pendingBalance ? parseFloat(form.pendingBalance) : undefined,
        invoiceNumber: form.invoiceNumber,
        emergencyBypass: form.emergencyBypass || undefined,
      })
      setPaymentResult(res.data)
      await loadPendingPatients()
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'Error al registrar el pago.')
    } finally {
      setLoading(false)
    }
  }

  const statusBadge = (status: string) => {
    switch (status) {
      case 'PAID': return { text: 'Pagado ✅', cls: 'bg-green-100 text-green-800' }
      case 'BLOCKED': return { text: 'Bloqueado ❌', cls: 'bg-red-100 text-red-800' }
      case 'PENDING': return { text: 'Pendiente ⏳', cls: 'bg-yellow-100 text-yellow-800' }
      case 'VALIDATED': return { text: 'Habilitado para Clínica ✅', cls: 'bg-green-100 text-green-800' }
      case 'PENDING_PAYMENT': return { text: 'Pendiente de Pago ⚠️', cls: 'bg-yellow-100 text-yellow-800' }
      default: return { text: status, cls: 'bg-gray-100 text-gray-800' }
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />

      <main className="flex-1 max-w-7xl mx-auto px-4 py-8 w-full">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-800">💳 Verificación de Pago y Filtro de Atención</h1>
          <p className="text-gray-600 mt-1">Registre el pago del paciente para habilitarlo para la consulta médica.</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Patient List */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow overflow-hidden">
              <div className="bg-orange-50 px-4 py-3 border-b flex items-center justify-between">
                <h2 className="font-bold text-gray-800">Pacientes Pendientes de Pago</h2>
                <button
                  onClick={loadPendingPatients}
                  className="text-xs text-blue-600 hover:underline"
                >
                  🔄 Actualizar
                </button>
              </div>

              {loadingList ? (
                <div className="p-6 text-center text-gray-500">Cargando...</div>
              ) : pendingPatients.length === 0 ? (
                <div className="p-6 text-center text-gray-500">
                  <p>🎉 No hay pacientes pendientes de pago.</p>
                </div>
              ) : (
                <div className="divide-y max-h-[500px] overflow-y-auto">
                  {pendingPatients.map(apt => (
                    <button
                      key={apt.id}
                      type="button"
                      onClick={() => handleSelectAppointment(apt)}
                      className={`w-full text-left px-4 py-3 hover:bg-blue-50 transition
                        ${selectedAppointment?.id === apt.id ? 'bg-blue-50 border-l-4 border-blue-600' : ''}`}
                    >
                      <p className="font-semibold text-gray-800 text-sm">{apt.patientName}</p>
                      <p className="text-xs text-gray-500">{apt.specialty} – {apt.doctorName}</p>
                      <p className="text-xs text-gray-400">{apt.appointmentDate} {apt.appointmentTime}</p>
                      {apt.baseTariff && (
                        <p className="text-xs font-semibold text-orange-600 mt-1">Q{apt.baseTariff.toFixed(2)}</p>
                      )}
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Payment Form */}
          <div className="lg:col-span-2">
            {!selectedAppointment && !paymentResult && (
              <div className="bg-white rounded-lg shadow p-8 text-center text-gray-500">
                <p className="text-4xl mb-3">👈</p>
                <p className="font-semibold">Seleccione un paciente de la lista para registrar su pago.</p>
              </div>
            )}

            {selectedAppointment && !paymentResult && (
              <div className="bg-white rounded-lg shadow overflow-hidden">
                <div className="bg-blue-50 px-6 py-4 border-b">
                  <h2 className="font-bold text-gray-800">Registrar Cobro</h2>
                  <p className="text-sm text-gray-600 mt-1">
                    Paciente: <strong>{selectedAppointment.patientName}</strong> |
                    Cita #{selectedAppointment.id} – {selectedAppointment.specialty}
                  </p>
                </div>

                <form onSubmit={handleSubmit} className="p-6 space-y-4">
                  {/* Emergency Bypass */}
                  <div className="bg-red-50 border border-red-200 rounded-lg p-3 flex items-start gap-3">
                    <input
                      type="checkbox"
                      id="emergencyBypass"
                      name="emergencyBypass"
                      checked={form.emergencyBypass}
                      onChange={handleChange}
                      className="mt-1 h-4 w-4 accent-red-600"
                    />
                    <label htmlFor="emergencyBypass" className="text-sm text-red-800">
                      <span className="font-bold">🚨 FA02 – Emergencia Código Rojo</span>
                      <span className="block text-xs mt-0.5">
                        Marque si el paciente proviene de triaje con prioridad ROJA. Se habilitará acceso inmediato a clínica y el trámite de pago se realizará posteriormente.
                      </span>
                    </label>
                  </div>

                  {!form.emergencyBypass && (
                    <>
                      {/* Payment Method */}
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Método de Pago *</label>
                        <select
                          name="paymentMethod"
                          value={form.paymentMethod}
                          onChange={handleChange}
                          className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                          required
                        >
                          <option value="CASH">💵 Efectivo</option>
                          <option value="CARD">💳 Tarjeta</option>
                          <option value="INSURANCE">🏥 Seguro</option>
                        </select>
                      </div>

                      {/* Authorization Number */}
                      {(form.paymentMethod === 'CARD' || form.paymentMethod === 'INSURANCE') && (
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">
                            {form.paymentMethod === 'CARD' ? 'Número de Autorización *' : 'Código de Aprobación *'}
                          </label>
                          <input
                            type="text"
                            name="authorizationNumber"
                            value={form.authorizationNumber}
                            onChange={handleChange}
                            placeholder={form.paymentMethod === 'CARD' ? 'Ej. AUTH-123456' : 'Ej. SEG-APROV-789'}
                            className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                          />
                        </div>
                      )}

                      {/* Amounts */}
                      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">Monto Total (Q) *</label>
                          <input
                            type="number"
                            name="totalAmount"
                            value={form.totalAmount}
                            onChange={handleChange}
                            min="0"
                            step="0.01"
                            className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                          />
                        </div>
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">Cubierto por Seguro (Q)</label>
                          <input
                            type="number"
                            name="insuranceCoverage"
                            value={form.insuranceCoverage}
                            onChange={handleChange}
                            min="0"
                            step="0.01"
                            className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                          />
                        </div>
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">Saldo Pendiente (Q)</label>
                          <input
                            type="number"
                            name="pendingBalance"
                            value={form.pendingBalance}
                            onChange={handleChange}
                            min="0"
                            step="0.01"
                            className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                          />
                        </div>
                      </div>

                      {/* Invoice Number */}
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Número de Factura / Recibo *</label>
                        <input
                          type="text"
                          name="invoiceNumber"
                          value={form.invoiceNumber}
                          onChange={handleChange}
                          placeholder="Ej. FAC-2024-001"
                          className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                          required
                        />
                      </div>
                    </>
                  )}

                  {form.emergencyBypass && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Número de Registro de Emergencia *</label>
                      <input
                        type="text"
                        name="invoiceNumber"
                        value={form.invoiceNumber}
                        onChange={handleChange}
                        placeholder="Ej. EMERG-2024-001"
                        className="w-full border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-red-500 border-red-300"
                        required
                      />
                    </div>
                  )}

                  {error && (
                    <div className="bg-red-50 border border-red-200 rounded-lg p-3 text-red-700 text-sm">
                      {error}
                    </div>
                  )}

                  <div className="flex gap-3 pt-2">
                    <button
                      type="button"
                      onClick={() => setSelectedAppointment(null)}
                      className="flex-1 border border-gray-300 text-gray-600 hover:bg-gray-50 py-2 rounded-lg font-semibold transition"
                    >
                      Cancelar
                    </button>
                    <button
                      type="submit"
                      disabled={loading}
                      className={`flex-1 py-2 rounded-lg font-semibold transition text-white
                        ${form.emergencyBypass
                          ? 'bg-red-600 hover:bg-red-700'
                          : 'bg-green-600 hover:bg-green-700'} 
                        disabled:bg-gray-300`}
                    >
                      {loading ? '⏳ Procesando...' : form.emergencyBypass ? '🚨 Habilitar Código Rojo' : '✅ Registrar Pago'}
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* Payment Result */}
            {paymentResult && (
              <div className="bg-white rounded-lg shadow p-6">
                <div className="text-center mb-6">
                  <div className="text-5xl mb-3">
                    {paymentResult.paymentStatus === 'PAID' ? '✅'
                      : paymentResult.emergencyBypass ? '🚨' : '❌'}
                  </div>
                  <h2 className="text-xl font-bold text-gray-800">{paymentResult.message}</h2>
                </div>

                <div className="bg-gray-50 rounded-lg p-4 space-y-2 text-sm mb-4">
                  <div className="flex justify-between">
                    <span className="text-gray-500">Paciente:</span>
                    <span className="font-semibold">{paymentResult.patientName}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Cita #:</span>
                    <span className="font-semibold">{paymentResult.appointmentId}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Especialidad:</span>
                    <span className="font-semibold">{paymentResult.specialty}</span>
                  </div>
                  {paymentResult.totalAmount > 0 && (
                    <div className="flex justify-between">
                      <span className="text-gray-500">Monto Total:</span>
                      <span className="font-semibold">Q{paymentResult.totalAmount.toFixed(2)}</span>
                    </div>
                  )}
                  {paymentResult.insuranceCoverage !== null && paymentResult.insuranceCoverage !== undefined && paymentResult.insuranceCoverage > 0 && (
                    <div className="flex justify-between">
                      <span className="text-gray-500">Cobertura Seguro:</span>
                      <span className="font-semibold text-green-700">Q{paymentResult.insuranceCoverage.toFixed(2)}</span>
                    </div>
                  )}
                  <div className="flex justify-between">
                    <span className="text-gray-500">Factura:</span>
                    <span className="font-semibold">{paymentResult.invoiceNumber}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Estado del Pago:</span>
                    <span className={`px-2 py-0.5 rounded text-xs font-semibold ${statusBadge(paymentResult.paymentStatus).cls}`}>
                      {statusBadge(paymentResult.paymentStatus).text}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Estado Administrativo:</span>
                    <span className={`px-2 py-0.5 rounded text-xs font-semibold ${statusBadge(paymentResult.appointmentStatus).cls}`}>
                      {statusBadge(paymentResult.appointmentStatus).text}
                    </span>
                  </div>
                </div>

                {paymentResult.auditNote && (
                  <div className="bg-blue-50 rounded-lg p-3 text-xs text-blue-700 mb-4">
                    <span className="font-semibold">Bitácora de auditoría: </span>{paymentResult.auditNote}
                  </div>
                )}

                <button
                  onClick={() => { setPaymentResult(null); setSelectedAppointment(null) }}
                  className="w-full bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-lg font-semibold transition"
                >
                  Atender siguiente paciente
                </button>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  )
}

export default PaymentVerification
