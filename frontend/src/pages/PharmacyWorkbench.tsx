import React, { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import {
  authAPI,
  catalogAPI,
  pharmacyAPI,
  type InsuranceOption,
  type MedicineResponse,
  type PaymentOption,
  type PharmacyPaymentRequest,
  type PharmacyPrescriptionLookupResponse,
  type PrescriptionResponse,
} from '@/services/api'
import AdminSidebar from '@/components/ui/AdminSidebar'
import useSidebarPreference from '@/hooks/useSidebarPreference'
import StatusChip from '@/components/ui/StatusChip'

interface PharmacyPaymentForm {
  metodoPago: PaymentOption
  bancoTarjeta: string
  numeroTarjeta: string
  fechaVencimientoTarjeta: string
  nombreTitularTarjeta: string
  cvc: string
  aseguradoraId: string
  numeroPoliza: string
}

const INITIAL_PAYMENT_FORM: PharmacyPaymentForm = {
  metodoPago: 'TARJETA',
  bancoTarjeta: '',
  numeroTarjeta: '',
  fechaVencimientoTarjeta: '',
  nombreTitularTarjeta: '',
  cvc: '',
  aseguradoraId: '',
  numeroPoliza: '',
}

const MAX_TEXT_LENGTH = 50
const CARD_NUMBER_PATTERN = /^[0-9]{13,19}$/
const CARD_EXPIRY_MMYY_PATTERN = /^(0[1-9]|1[0-2])[0-9]{2}$/
const CARD_CVC_PATTERN = /^[0-9]{3,4}$/

const PharmacyWorkbench: React.FC = () => {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { user, logout } = useAuth()
  const { collapsed: sidebarCollapsed, toggleCollapsed } = useSidebarPreference('admin-pharmacy', false)
  const [loadingLogout, setLoadingLogout] = useState(false)

  const [dpi, setDpi] = useState('')
  const [lookup, setLookup] = useState<PharmacyPrescriptionLookupResponse | null>(null)
  const [selectedPrescriptionId, setSelectedPrescriptionId] = useState<number | null>(null)
  const [medicines, setMedicines] = useState<MedicineResponse[]>([])
  const [insurances, setInsurances] = useState<InsuranceOption[]>([])
  const [paymentForm, setPaymentForm] = useState<PharmacyPaymentForm>(INITIAL_PAYMENT_FORM)
  const [showPaymentModal, setShowPaymentModal] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)
  const [loadingCatalogs, setLoadingCatalogs] = useState(false)

  const selectedPrescription = lookup?.recetas.find((item) => item.recetaMedicaId === selectedPrescriptionId) || null

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

  const handleLookupByDpi = async () => {
    resetFeedback()
    if (!dpi.trim()) {
      setError('Ingresa DPI del paciente para buscar recetas activas.')
      return
    }
    setBusy(true)
    try {
      const { data } = await pharmacyAPI.getPrescriptionsByDpi(dpi.trim())
      setLookup(data)
      const firstPending = data.recetas.find((receta) => !receta.despachada) || data.recetas[0] || null
      setSelectedPrescriptionId(firstPending ? firstPending.recetaMedicaId : null)
      if (data.recetas.length === 0) {
        setMessage('Paciente localizado, pero no hay recetas activas disponibles para despacho.')
      } else {
        setMessage(`Se encontraron ${data.recetas.length} receta(s) activas para ${data.pacienteNombre}.`)
      }
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'No se pudo buscar recetas por DPI.')
      setLookup(null)
      setSelectedPrescriptionId(null)
    } finally {
      setBusy(false)
    }
  }

  useEffect(() => {
    const initialDpi = searchParams.get('dpi')
    if (!initialDpi) return
    setDpi(initialDpi)
  }, [searchParams])

  useEffect(() => {
    const loadCatalogs = async () => {
      setLoadingCatalogs(true)
      try {
        const { data } = await catalogAPI.insurances()
        setInsurances(data)
      } catch {
        // Los seguros son opcionales; si fallan, se mantiene el flujo con tarjeta.
      } finally {
        setLoadingCatalogs(false)
      }
    }
    void loadCatalogs()
  }, [])

  const mapPrescriptionInLookup = (updated: PrescriptionResponse) => {
    setLookup((prev) => {
      if (!prev) {
        return prev
      }
      return {
        ...prev,
        recetas: prev.recetas.map((receta) =>
          receta.recetaMedicaId === updated.recetaMedicaId ? updated : receta
        ),
      }
    })
  }

  const validatePaymentForm = (): string | null => {
    if (paymentForm.metodoPago === 'TARJETA') {
      if (paymentForm.bancoTarjeta.trim().length > MAX_TEXT_LENGTH) {
        return 'El banco no puede exceder 50 caracteres.'
      }
      if (paymentForm.nombreTitularTarjeta.trim().length > MAX_TEXT_LENGTH) {
        return 'El nombre del titular no puede exceder 50 caracteres.'
      }
      if (
        !paymentForm.bancoTarjeta.trim() ||
        !paymentForm.numeroTarjeta.trim() ||
        !paymentForm.fechaVencimientoTarjeta.trim() ||
        !paymentForm.nombreTitularTarjeta.trim() ||
        !paymentForm.cvc.trim()
      ) {
        return 'Completa todos los datos de tarjeta para validar el pago.'
      }
      if (!CARD_NUMBER_PATTERN.test(paymentForm.numeroTarjeta.trim())) {
        return 'El numero de tarjeta debe tener entre 13 y 19 digitos.'
      }
      if (!CARD_EXPIRY_MMYY_PATTERN.test(paymentForm.fechaVencimientoTarjeta.trim())) {
        return 'La fecha de vencimiento debe usar formato MMYY.'
      }
      if (!CARD_CVC_PATTERN.test(paymentForm.cvc.trim())) {
        return 'El CVC debe tener 3 o 4 digitos.'
      }
      return null
    }

    if (paymentForm.numeroPoliza.trim().length > MAX_TEXT_LENGTH) {
      return 'El numero de poliza no puede exceder 50 caracteres.'
    }
    if (!paymentForm.aseguradoraId || !paymentForm.numeroPoliza.trim()) {
      return 'Selecciona aseguradora e ingresa numero de poliza.'
    }
    return null
  }

  const handleCardNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const sanitized = e.target.value.replace(/\D/g, '').slice(0, 19)
    setPaymentForm((prev) => ({ ...prev, numeroTarjeta: sanitized }))
    setError('')
  }

  const handleCardExpiryChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const sanitized = e.target.value.replace(/\D/g, '').slice(0, 4)
    setPaymentForm((prev) => ({ ...prev, fechaVencimientoTarjeta: sanitized }))
    setError('')
  }

  const handleCardCvcChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const sanitized = e.target.value.replace(/\D/g, '').slice(0, 4)
    setPaymentForm((prev) => ({ ...prev, cvc: sanitized }))
    setError('')
  }

  const handleValidatePayment = async () => {
    resetFeedback()
    if (!selectedPrescription) {
      setError('Selecciona una receta antes de validar el pago.')
      return
    }

    const validationError = validatePaymentForm()
    if (validationError) {
      setError(validationError)
      return
    }

    const payload: PharmacyPaymentRequest = {
      dpiPaciente: lookup?.pacienteDpi,
      metodoPago: paymentForm.metodoPago,
      bancoTarjeta: paymentForm.metodoPago === 'TARJETA' ? paymentForm.bancoTarjeta.trim() : undefined,
      numeroTarjeta: paymentForm.metodoPago === 'TARJETA' ? paymentForm.numeroTarjeta.replace(/\s+/g, '') : undefined,
      fechaVencimientoTarjeta: paymentForm.metodoPago === 'TARJETA'
        ? `${paymentForm.fechaVencimientoTarjeta.trim().slice(0, 2)}/${paymentForm.fechaVencimientoTarjeta.trim().slice(2)}`
        : undefined,
      nombreTitularTarjeta: paymentForm.metodoPago === 'TARJETA' ? paymentForm.nombreTitularTarjeta.trim() : undefined,
      cvc: paymentForm.metodoPago === 'TARJETA' ? paymentForm.cvc.trim() : undefined,
      aseguradoraId: paymentForm.metodoPago === 'SEGURO' ? Number(paymentForm.aseguradoraId) : undefined,
      numeroPoliza: paymentForm.metodoPago === 'SEGURO' ? paymentForm.numeroPoliza.trim() : undefined,
    }

    setBusy(true)
    try {
      const { data } = await pharmacyAPI.validatePrescriptionPayment(selectedPrescription.recetaMedicaId, payload)
      mapPrescriptionInLookup(data)
      setShowPaymentModal(false)
      setPaymentForm(INITIAL_PAYMENT_FORM)
      setMessage('Pago validado en farmacia. Ya puedes despachar la receta seleccionada.')
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'No se pudo validar el pago en farmacia.')
    } finally {
      setBusy(false)
    }
  }

  const handleDispensePrescription = async () => {
    resetFeedback()
    if (!selectedPrescription) {
      setError('Selecciona una receta para despachar.')
      return
    }
    if (!selectedPrescription.pagoFarmaciaValidado) {
      setError('Debes validar el pago antes de despachar la receta.')
      return
    }
    setBusy(true)
    try {
      const { data } = await pharmacyAPI.dispensePrescription(selectedPrescription.recetaMedicaId)
      mapPrescriptionInLookup(data)
      setMessage('Despacho registrado correctamente. Inventario descontado y recordatorios generados si aplican.')
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
            <h2 className="text-2xl font-bold text-slate-900">Farmacia</h2>
            <p className="text-sm text-slate-600 mt-1">Flujo: buscar por DPI, seleccionar receta, validar pago y luego despachar.</p>
          </div>
          <StatusChip label="Módulo activo" tone="emerald" />
        </div>

        {error && <div className="mb-4 rounded-lg border border-red-300 bg-red-100 px-4 py-3 text-sm text-red-700">{error}</div>}
        {message && <div className="mb-4 rounded-lg border border-emerald-300 bg-emerald-100 px-4 py-3 text-sm text-emerald-700">{message}</div>}

        <section className="grid grid-cols-1 xl:grid-cols-2 gap-5">
          <div className="rounded-xl border border-blue-200 bg-white p-5 shadow-sm space-y-3">
            <h3 className="font-semibold text-slate-900">1) Buscar recetas por DPI</h3>
            <div className="flex gap-2">
              <input
                value={dpi}
                onChange={(e) => setDpi(e.target.value)}
                placeholder="DPI del paciente (13 digitos)"
                className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm"
              />
              <button type="button" onClick={() => void handleLookupByDpi()} disabled={busy} className="px-3 py-2 rounded-lg bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 disabled:opacity-60">
                Buscar
              </button>
            </div>
            {lookup && (
              <p className="text-xs text-slate-600">
                Paciente: <span className="font-semibold">{lookup.pacienteNombre}</span> · DPI: {lookup.pacienteDpi}
              </p>
            )}
          </div>

          <div className="rounded-xl border border-blue-200 bg-white p-5 shadow-sm space-y-3">
            <h3 className="font-semibold text-slate-900">2) Pago y despacho</h3>
            <div className="flex flex-wrap gap-2">
              <button
                type="button"
                onClick={() => setShowPaymentModal(true)}
                disabled={busy || !selectedPrescription || !!selectedPrescription.pagoFarmaciaValidado || !!selectedPrescription.despachada}
                className="px-3 py-2 rounded-lg bg-amber-500 text-white text-sm font-semibold hover:bg-amber-600 disabled:opacity-60"
              >
                Validar pago
              </button>
              <button
                type="button"
                onClick={() => void handleDispensePrescription()}
                disabled={busy || !selectedPrescription || !selectedPrescription.pagoFarmaciaValidado || !!selectedPrescription.despachada}
                className="px-3 py-2 rounded-lg bg-emerald-600 text-white text-sm font-semibold hover:bg-emerald-700 disabled:opacity-60"
              >
                Despachar receta
              </button>
            </div>
            <p className="text-xs text-slate-600">La receta solo se puede despachar cuando el pago en farmacia ya fue validado.</p>
            <button type="button" onClick={() => void handleLoadMedicines()} disabled={busy} className="px-3 py-2 rounded-lg border border-blue-300 text-blue-700 text-sm hover:bg-blue-50 disabled:opacity-60">
              Cargar inventario
            </button>
          </div>
        </section>

        {lookup && lookup.recetas.length > 0 && (
          <section className="mt-5 rounded-xl border border-blue-200 bg-white p-5 shadow-sm">
            <h3 className="font-semibold text-slate-900">Recetas disponibles</h3>
            <div className="mt-3 grid grid-cols-1 md:grid-cols-2 gap-3">
              {lookup.recetas.map((receta) => (
                <button
                  key={receta.recetaMedicaId}
                  type="button"
                  onClick={() => setSelectedPrescriptionId(receta.recetaMedicaId)}
                  className={`text-left rounded-lg border px-4 py-3 transition ${selectedPrescriptionId === receta.recetaMedicaId ? 'border-blue-500 bg-blue-50' : 'border-slate-200 hover:border-blue-300'}`}
                >
                  <p className="text-sm font-semibold text-slate-900">Receta #{receta.recetaMedicaId}</p>
                  <p className="text-xs text-slate-600 mt-1">Medico: {receta.medicoNombre || 'N/D'}</p>
                  <p className="text-xs text-slate-600">Fecha: {String(receta.fechaEmision)}</p>
                  <p className="text-xs text-slate-600">Total: Q{(receta.totalMedicamentos ?? 0).toFixed(2)}</p>
                  <div className="mt-2 flex flex-wrap gap-2 text-xs">
                    <span className={`px-2 py-0.5 rounded ${receta.pagoFarmaciaValidado ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'}`}>
                      {receta.pagoFarmaciaValidado ? 'Pago validado' : 'Pago pendiente'}
                    </span>
                    <span className={`px-2 py-0.5 rounded ${receta.despachada ? 'bg-slate-200 text-slate-700' : 'bg-blue-100 text-blue-700'}`}>
                      {receta.despachada ? 'Despachada' : 'Pendiente despacho'}
                    </span>
                  </div>
                </button>
              ))}
            </div>
          </section>
        )}

        {selectedPrescription && (
          <section className="mt-5 rounded-xl border border-blue-200 bg-white p-5 shadow-sm">
            <h3 className="font-semibold text-slate-900">Detalle de receta seleccionada</h3>
            <p className="text-sm text-slate-700 mt-2">
              Receta ID: {selectedPrescription.recetaMedicaId} · Fecha emision: {String(selectedPrescription.fechaEmision)} · Estado administrativo: {selectedPrescription.estadoAdministrativo || 'N/D'}
            </p>
            <div className="mt-3 overflow-x-auto">
              <table className="min-w-full text-sm">
                <thead>
                  <tr className="text-left text-slate-600 border-b border-slate-200">
                    <th className="py-2 pr-4">Detalle ID</th>
                    <th className="py-2 pr-4">Medicamento</th>
                    <th className="py-2 pr-4">Cantidad</th>
                    <th className="py-2 pr-4">Stock</th>
                    <th className="py-2 pr-4">Frecuencia</th>
                    <th className="py-2 pr-4">Duración</th>
                    <th className="py-2 pr-4">Subtotal</th>
                    <th className="py-2 pr-4">Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {selectedPrescription.items.map((item) => (
                    <tr key={item.recetaMedicaDetalleId} className="border-b border-slate-100">
                      <td className="py-2 pr-4">{item.recetaMedicaDetalleId}</td>
                      <td className="py-2 pr-4">{item.medicamentoNombre || item.medicamentoId}</td>
                      <td className="py-2 pr-4">{item.cantidad}</td>
                      <td className="py-2 pr-4">{item.stockActual ?? 'N/D'}</td>
                      <td className="py-2 pr-4">{item.frecuenciaHoras ?? 'N/D'}h</td>
                      <td className="py-2 pr-4">{item.duracionDias ?? 'N/D'} días</td>
                      <td className="py-2 pr-4">Q{(item.subtotal ?? 0).toFixed(2)}</td>
                      <td className="py-2 pr-4">{item.despachado ? 'Atendida' : item.disponible ? 'Pendiente' : 'Sin stock'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        )}

        {lookup && lookup.recetas.length === 0 && (
          <section className="mt-5 rounded-xl border border-blue-200 bg-white p-5 shadow-sm text-sm text-slate-700">
            No hay recetas activas para el DPI consultado.
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
                    <th className="py-2 pr-4">Precio</th>
                    <th className="py-2 pr-4">Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {medicines.map((medicine) => (
                    <tr key={medicine.medicamentoId} className="border-b border-slate-100">
                      <td className="py-2 pr-4">{medicine.medicamentoId}</td>
                      <td className="py-2 pr-4">{medicine.nombre}</td>
                      <td className="py-2 pr-4">{medicine.presentacion || 'N/D'}</td>
                      <td className="py-2 pr-4">{medicine.stockActual}</td>
                      <td className="py-2 pr-4">Q{(medicine.precioUnitario ?? 0).toFixed(2)}</td>
                      <td className="py-2 pr-4">{medicine.stockActual <= 10 ? 'Bajo stock' : 'Disponible'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        )}

        {showPaymentModal && selectedPrescription && (
          <div className="fixed inset-0 z-50 bg-slate-900/40 flex items-center justify-center p-4">
            <div className="w-full max-w-3xl rounded-xl border border-blue-200 bg-white p-5 shadow-xl">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h3 className="text-lg font-bold text-slate-900">Validar pago de farmacia</h3>
                  <p className="text-sm text-slate-600 mt-1">Receta #{selectedPrescription.recetaMedicaId} · Paciente: {lookup?.pacienteNombre}</p>
                </div>
                <button
                  type="button"
                  onClick={() => setShowPaymentModal(false)}
                  className="px-2 py-1 text-slate-500 hover:text-slate-700"
                >
                  Cerrar
                </button>
              </div>

              <div className="mt-4 rounded-lg border border-blue-100 bg-blue-50 p-3 text-sm text-slate-700">
                <p className="font-semibold text-slate-900">Detalle de medicamentos</p>
                <ul className="mt-2 space-y-1">
                  {selectedPrescription.items.map((item) => (
                    <li key={item.recetaMedicaDetalleId}>
                      {item.medicamentoNombre || `Medicamento ${item.medicamentoId}`} · cant. {item.cantidad} · subtotal Q{(item.subtotal ?? 0).toFixed(2)}
                    </li>
                  ))}
                </ul>
                <p className="mt-2 font-semibold">Total: Q{(selectedPrescription.totalMedicamentos ?? 0).toFixed(2)}</p>
              </div>

              <div className="mt-4 space-y-4">
                <div className="flex flex-wrap gap-2">
                  <button
                    type="button"
                    onClick={() => setPaymentForm((prev) => ({ ...prev, metodoPago: 'TARJETA' }))}
                    className={`px-4 py-2 rounded-lg border text-sm font-semibold ${paymentForm.metodoPago === 'TARJETA' ? 'bg-blue-600 border-blue-600 text-white' : 'bg-white border-blue-200 text-slate-700 hover:bg-blue-50'}`}
                  >
                    Tarjeta
                  </button>
                  <button
                    type="button"
                    onClick={() => setPaymentForm((prev) => ({ ...prev, metodoPago: 'SEGURO' }))}
                    className={`px-4 py-2 rounded-lg border text-sm font-semibold ${paymentForm.metodoPago === 'SEGURO' ? 'bg-blue-600 border-blue-600 text-white' : 'bg-white border-blue-200 text-slate-700 hover:bg-blue-50'}`}
                  >
                    Seguro medico
                  </button>
                </div>

                {paymentForm.metodoPago === 'TARJETA' ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <input
                      value={paymentForm.bancoTarjeta}
                      onChange={(e) => setPaymentForm((prev) => ({ ...prev, bancoTarjeta: e.target.value.slice(0, MAX_TEXT_LENGTH) }))}
                      maxLength={MAX_TEXT_LENGTH}
                      placeholder="Banco"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    />
                    <input
                      value={paymentForm.numeroTarjeta}
                      onChange={handleCardNumberChange}
                      inputMode="numeric"
                      maxLength={19}
                      placeholder="13 a 19 digitos"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    />
                    <div>
                      <input
                        value={paymentForm.fechaVencimientoTarjeta}
                        onChange={handleCardExpiryChange}
                        inputMode="numeric"
                        maxLength={4}
                        placeholder="MMYY"
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                      />
                      <p className="mt-1 text-xs text-slate-500">Formato requerido: MMYY (ej. 0728)</p>
                    </div>
                    <input
                      value={paymentForm.nombreTitularTarjeta}
                      onChange={(e) => setPaymentForm((prev) => ({ ...prev, nombreTitularTarjeta: e.target.value.slice(0, MAX_TEXT_LENGTH) }))}
                      maxLength={MAX_TEXT_LENGTH}
                      placeholder="Titular"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    />
                    <input
                      value={paymentForm.cvc}
                      onChange={handleCardCvcChange}
                      inputMode="numeric"
                      maxLength={4}
                      placeholder="3-4 digitos"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    />
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                    <select
                      value={paymentForm.aseguradoraId}
                      onChange={(e) => setPaymentForm((prev) => ({ ...prev, aseguradoraId: e.target.value }))}
                      disabled={loadingCatalogs}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    >
                      <option value="">{loadingCatalogs ? 'Cargando aseguradoras...' : 'Selecciona aseguradora'}</option>
                      {insurances.map((insurance) => (
                        <option key={insurance.id} value={insurance.id}>{insurance.nombre}</option>
                      ))}
                    </select>
                    <input
                      value={paymentForm.numeroPoliza}
                      onChange={(e) => setPaymentForm((prev) => ({ ...prev, numeroPoliza: e.target.value.slice(0, MAX_TEXT_LENGTH) }))}
                      maxLength={MAX_TEXT_LENGTH}
                      placeholder="Numero de poliza"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    />
                  </div>
                )}
              </div>

              <div className="mt-5 flex justify-end gap-2">
                <button type="button" onClick={() => setShowPaymentModal(false)} className="px-4 py-2 rounded-lg border border-slate-300 text-sm text-slate-600 hover:bg-slate-50">Cancelar</button>
                <button type="button" onClick={() => void handleValidatePayment()} disabled={busy} className="px-4 py-2 rounded-lg bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 disabled:opacity-60">
                  Confirmar pago
                </button>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  )
}

export default PharmacyWorkbench

