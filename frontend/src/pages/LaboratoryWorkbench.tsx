import React, { useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import AdminSidebar from '@/components/ui/AdminSidebar'
import useSidebarPreference from '@/hooks/useSidebarPreference'
import StatusChip from '@/components/ui/StatusChip'
import { useAuth } from '@/context/AuthContext'
import {
  authAPI,
  catalogAPI,
  laboratoryAPI,
  type InsuranceOption,
  type LaboratoryOrderResponse,
  type LaboratoryPaymentRequest,
  type PaymentOption,
} from '@/services/api'

const MAX_TEXT_LENGTH = 50
const CARD_NUMBER_PATTERN = /^[0-9]{13,19}$/
const CARD_EXPIRY_MMYY_PATTERN = /^(0[1-9]|1[0-2])[0-9]{2}$/
const CARD_CVC_PATTERN = /^[0-9]{3,4}$/
const LABORATORY_FEE = 200

interface LaboratoryPaymentForm {
  metodoPago: PaymentOption
  bancoTarjeta: string
  numeroTarjeta: string
  fechaVencimientoTarjeta: string
  nombreTitularTarjeta: string
  cvc: string
  aseguradoraId: string
  numeroPoliza: string
}

const INITIAL_PAYMENT_FORM: LaboratoryPaymentForm = {
  metodoPago: 'TARJETA',
  bancoTarjeta: '',
  numeroTarjeta: '',
  fechaVencimientoTarjeta: '',
  nombreTitularTarjeta: '',
  cvc: '',
  aseguradoraId: '',
  numeroPoliza: '',
}

const INITIAL_RESULT_FORM = {
  valorResultado: '',
  unidadResultado: '',
  referenciaMinima: '',
  referenciaMaxima: '',
  observaciones: '',
  resumen: '',
  conclusion: '',
}

const LaboratoryWorkbench: React.FC = () => {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { user, logout } = useAuth()
  const { collapsed: sidebarCollapsed, toggleCollapsed } = useSidebarPreference('admin-laboratory', false)
  const [loadingLogout, setLoadingLogout] = useState(false)

  const [resultPayload, setResultPayload] = useState(INITIAL_RESULT_FORM)
  const [patientDpiInput, setPatientDpiInput] = useState(searchParams.get('dpi') || '')
  const [detalleIdInput, setDetalleIdInput] = useState('')
  const [rejectReason, setRejectReason] = useState('')
  const [ordersByDetalle, setOrdersByDetalle] = useState<LaboratoryOrderResponse[]>([])
  const [currentOrder, setCurrentOrder] = useState<LaboratoryOrderResponse | null>(null)
  const [paymentForm, setPaymentForm] = useState<LaboratoryPaymentForm>(INITIAL_PAYMENT_FORM)
  const [showPaymentModal, setShowPaymentModal] = useState(false)
  const [insurances, setInsurances] = useState<InsuranceOption[]>([])
  const [loadingCatalogs, setLoadingCatalogs] = useState(false)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)

  const loadInsuranceCatalog = async () => {
    setLoadingCatalogs(true)
    try {
      const { data } = await catalogAPI.insurances()
      setInsurances(data)
    } catch {
      // El flujo con tarjeta sigue disponible aunque no carguen aseguradoras.
    } finally {
      setLoadingCatalogs(false)
    }
  }

  const resetFeedback = () => {
    setError('')
    setMessage('')
  }

  const parseOptionalNumber = (value: string): number | undefined => {
    if (!value.trim()) return undefined
    const parsed = Number(value)
    return Number.isNaN(parsed) ? undefined : parsed
  }

  const selectBestOrder = (orders: LaboratoryOrderResponse[]) => (
    orders.find((order) => order.estado !== 'COMPLETADO' && order.estado !== 'FINALIZADO') || orders[0] || null
  )

  const syncCurrentOrder = (order: LaboratoryOrderResponse) => {
    setCurrentOrder(order)
    setDetalleIdInput(String(order.citaMedicaDetalleId))
    setOrdersByDetalle((prev) => {
      const remaining = prev.filter((item) => item.ordenLaboratorioId !== order.ordenLaboratorioId)
      return [order, ...remaining]
    })
  }

  const applyOrderList = (orders: LaboratoryOrderResponse[]) => {
    setOrdersByDetalle(orders)
    const nextOrder = selectBestOrder(orders)
    setCurrentOrder(nextOrder)
    if (nextOrder) {
      setDetalleIdInput(String(nextOrder.citaMedicaDetalleId))
    } else {
      setDetalleIdInput('')
    }
  }

  const validatePaymentForm = (): string | null => {
    if (paymentForm.metodoPago === 'TARJETA') {
      if (paymentForm.bancoTarjeta.trim().length > 50) {
        return 'El banco no puede exceder 50 caracteres.'
      }
      if (paymentForm.nombreTitularTarjeta.trim().length > 50) {
        return 'El nombre del titular no puede exceder 50 caracteres.'
      }
      if (!paymentForm.bancoTarjeta.trim() || !paymentForm.numeroTarjeta.trim() || !paymentForm.fechaVencimientoTarjeta.trim() || !paymentForm.nombreTitularTarjeta.trim() || !paymentForm.cvc.trim()) {
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

    if (paymentForm.numeroPoliza.trim().length > 50) {
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

  const handleGetOrdersByDpi = async () => {
    resetFeedback()
    const normalizedDpi = patientDpiInput.trim()
    if (!/^[0-9]{1,13}$/.test(normalizedDpi)) {
      setError('Ingresa un DPI valido (solo digitos, maximo 13).')
      return
    }

    setBusy(true)
    try {
      const { data } = await laboratoryAPI.getOrdersByPatientDpi(normalizedDpi)
      applyOrderList(data)
      setMessage(data.length > 0
        ? `Se encontraron ${data.length} orden(es) de laboratorio para el DPI ${normalizedDpi}.`
        : `No hay ordenes de laboratorio para el DPI ${normalizedDpi}.`)
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'No se pudieron cargar ordenes por DPI.')
      setOrdersByDetalle([])
      setCurrentOrder(null)
    } finally {
      setBusy(false)
    }
  }

  React.useEffect(() => {
    void loadInsuranceCatalog()
  }, [])

  const openPaymentModal = () => {
    resetFeedback()
    setShowPaymentModal(true)
    if (insurances.length === 0 && !loadingCatalogs) {
      void loadInsuranceCatalog()
    }
  }

  React.useEffect(() => {
    const detalle = searchParams.get('citaMedicaDetalleId')
    const dpi = searchParams.get('dpi')

    if (dpi) {
      setPatientDpiInput(dpi)
    }
    if (!detalle) return

    setDetalleIdInput(detalle)
    void (async () => {
      setBusy(true)
      try {
        const { data } = await laboratoryAPI.getOrdersByDetalle(Number(detalle))
        applyOrderList(data)
        if (data.length > 0) {
          setMessage(`Se cargaron ${data.length} orden(es) generadas desde la consulta #${detalle}.`)
        }
      } catch {
        // No bloquear la vista si la cita aún no tiene órdenes creadas.
      } finally {
        setBusy(false)
      }
    })()
  }, [searchParams])

  const handleValidatePayment = async () => {
    resetFeedback()
    if (!currentOrder) {
      setError('Selecciona una orden antes de validar el pago.')
      return
    }
    const pendingPaymentStates = ['PENDIENTE_PAGO', 'PENDIENTE_MUESTRA']
    if (currentOrder.pagoValidado || !pendingPaymentStates.includes(currentOrder.estado)) {
      setError('La orden seleccionada ya no está pendiente de pago.')
      return
    }

    const validationError = validatePaymentForm()
    if (validationError) {
      setError(validationError)
      return
    }

    const payload: LaboratoryPaymentRequest = {
      dpiPaciente: patientDpiInput.trim() || undefined,
      metodoPago: paymentForm.metodoPago,
      bancoTarjeta: paymentForm.metodoPago === 'TARJETA' ? paymentForm.bancoTarjeta.trim() : undefined,
      numeroTarjeta: paymentForm.metodoPago === 'TARJETA' ? paymentForm.numeroTarjeta.trim() : undefined,
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
      const { data } = await laboratoryAPI.validateOrderPayment(currentOrder.ordenLaboratorioId, payload)
      syncCurrentOrder(data)
      setShowPaymentModal(false)
      setPaymentForm(INITIAL_PAYMENT_FORM)
      setMessage('Pago de laboratorio validado. Ya puedes recibir la muestra.')
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'No se pudo validar el pago de laboratorio.')
    } finally {
      setBusy(false)
    }
  }

  const handleReceive = async () => {
    resetFeedback()
    if (!currentOrder) {
      setError('Selecciona una orden antes de recibir muestra.')
      return
    }
    if (!currentOrder.pagoValidado) {
      setError('Debes validar el pago antes de recibir la muestra.')
      return
    }
    setBusy(true)
    try {
      const { data } = await laboratoryAPI.receiveSample(currentOrder.ordenLaboratorioId)
      syncCurrentOrder(data)
      setMessage(`Muestra recibida. Estado actual: ${data.estado}.`)
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'No se pudo recibir la muestra.')
    } finally {
      setBusy(false)
    }
  }

  const handleStartProcessing = async () => {
    resetFeedback()
    if (!currentOrder) {
      setError('Selecciona una orden antes de iniciar el procesamiento.')
      return
    }
    setBusy(true)
    try {
      const { data } = await laboratoryAPI.startProcessing(currentOrder.ordenLaboratorioId)
      syncCurrentOrder(data)
      setMessage(`Procesamiento iniciado. Estado actual: ${data.estado}. Etiqueta: ${data.etiquetaId || 'N/D'}.`)
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'No se pudo iniciar el procesamiento de la muestra.')
    } finally {
      setBusy(false)
    }
  }

  const handleReject = async () => {
    resetFeedback()
    if (!currentOrder) {
      setError('Selecciona una orden antes de rechazar la muestra.')
      return
    }
    if (!rejectReason.trim()) {
      setError('Ingresa un motivo de rechazo.')
      return
    }
    if (rejectReason.trim().length > MAX_TEXT_LENGTH) {
      setError('El motivo de rechazo no puede exceder 120 caracteres.')
      return
    }
    setBusy(true)
    try {
      const { data } = await laboratoryAPI.rejectSample(currentOrder.ordenLaboratorioId, rejectReason.trim())
      syncCurrentOrder(data)
      setRejectReason('')
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
    if (!currentOrder) {
      setError('Selecciona una orden antes de registrar el resultado.')
      return
    }
    if (!resultPayload.conclusion.trim()) {
      setError('Ingresa la conclusión del resultado.')
      return
    }
    if (currentOrder.estado !== 'EN_PROCESO') {
      setError('Solo puedes registrar resultados para órdenes en estado EN_PROCESO.')
      return
    }
    if (currentOrder.resultado) {
      setError('La orden seleccionada ya tiene un resultado registrado.')
      return
    }
    setBusy(true)
    try {
      const { data } = await laboratoryAPI.addResult({
        ordenLaboratorioId: currentOrder.ordenLaboratorioId,
        nombreExamen: currentOrder.nombreExamen,
        valorResultado: parseOptionalNumber(resultPayload.valorResultado),
        unidadResultado: resultPayload.unidadResultado.trim() || undefined,
        referenciaMinima: parseOptionalNumber(resultPayload.referenciaMinima),
        referenciaMaxima: parseOptionalNumber(resultPayload.referenciaMaxima),
        observaciones: resultPayload.observaciones.trim() || undefined,
        resumen: resultPayload.resumen.trim() || undefined,
        conclusion: resultPayload.conclusion.trim(),
      })
      syncCurrentOrder(data)
      setResultPayload(INITIAL_RESULT_FORM)
      setMessage(`Resultado registrado. Estado actual: ${data.estado}.`)
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'No se pudo registrar el resultado.')
    } finally {
      setBusy(false)
    }
  }

  const handleRefreshOrder = async () => {
    resetFeedback()
    if (!currentOrder) {
      setError('Selecciona una orden para recargar su estado.')
      return
    }
    setBusy(true)
    try {
      const { data } = await laboratoryAPI.getOrder(currentOrder.ordenLaboratorioId)
      syncCurrentOrder(data)
      setMessage(`Orden ${data.ordenLaboratorioId} actualizada.`)
    } catch (err: any) {
      setError(err.response?.data?.errorMessage || 'No se pudo actualizar la orden.')
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

  const canValidatePayment = !!currentOrder
    && ['PENDIENTE_PAGO', 'PENDIENTE_MUESTRA'].includes(currentOrder.estado)
    && !currentOrder.pagoValidado
  const canReceiveSample = !!currentOrder && currentOrder.estado === 'PENDIENTE_MUESTRA' && currentOrder.pagoValidado
  const canStartProcessing = !!currentOrder && currentOrder.estado === 'MUESTRA_RECIBIDA'
  const canRejectSample = !!currentOrder && ['PENDIENTE_MUESTRA', 'MUESTRA_RECIBIDA', 'EN_PROCESO'].includes(currentOrder.estado)
  const canSaveResult = !!currentOrder && currentOrder.estado === 'EN_PROCESO' && !currentOrder.resultado

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
            <h2 className="text-2xl font-bold text-slate-900">Laboratorio</h2>
            <p className="text-sm text-slate-600 mt-1">Las órdenes se generan en consulta. Aquí solo validas pago, recibes la muestra, procesas y publicas resultados.</p>
          </div>
          <StatusChip label="Módulo activo" tone="blue" />
        </div>

        {error && <div className="mb-4 rounded-lg border border-red-300 bg-red-100 px-4 py-3 text-sm text-red-700">{error}</div>}
        {message && <div className="mb-4 rounded-lg border border-emerald-300 bg-emerald-100 px-4 py-3 text-sm text-emerald-700">{message}</div>}

        <section className="grid grid-cols-1 xl:grid-cols-2 gap-5">
          <div className="rounded-xl border border-blue-200 bg-white p-5 shadow-sm space-y-4">
            <div>
              <h3 className="font-semibold text-slate-900">1) Buscar órdenes existentes</h3>
              <p className="text-xs text-slate-600 mt-1">Busca por DPI del paciente y selecciona la orden que vas a gestionar.</p>
            </div>

            <div className="flex gap-2">
              <input
                value={patientDpiInput}
                onChange={(e) => setPatientDpiInput(e.target.value.replace(/\D/g, '').slice(0, 13))}
                placeholder="DPI del paciente"
                className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm"
              />
              <button
                type="button"
                onClick={() => void handleGetOrdersByDpi()}
                disabled={busy}
                className="px-4 py-2 rounded-lg bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 disabled:opacity-60"
              >
                Buscar
              </button>
            </div>

            {detalleIdInput && (
              <div className="rounded-lg border border-sky-200 bg-sky-50 px-3 py-2 text-xs text-sky-800">
                Detalle de cita vinculado: #{detalleIdInput}. Si vienes desde consulta, las órdenes ya se cargaron automáticamente.
              </div>
            )}

            {ordersByDetalle.length > 0 ? (
              <div className="space-y-3">
                {ordersByDetalle.map((order) => {
                  const selected = currentOrder?.ordenLaboratorioId === order.ordenLaboratorioId
                  return (
                    <button
                      key={order.ordenLaboratorioId}
                      type="button"
                      onClick={() => syncCurrentOrder(order)}
                      className={`w-full text-left rounded-xl border p-4 transition ${selected ? 'border-blue-500 bg-blue-50' : 'border-slate-200 bg-white hover:border-blue-300'}`}
                    >
                      <div className="flex flex-wrap items-start justify-between gap-3">
                        <div>
                          <p className="text-sm font-semibold text-slate-900">Orden #{order.ordenLaboratorioId} · {order.nombreExamen}</p>
                          <p className="text-xs text-slate-600 mt-1">Detalle de cita: #{order.citaMedicaDetalleId} · Muestra: {order.tipoMuestra || 'No especificada'}</p>
                        </div>
                        <div className="flex flex-wrap gap-2 text-xs">
                          <span className={`px-2 py-0.5 rounded ${order.pagoValidado ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'}`}>
                            {order.pagoValidado ? 'Pago validado' : 'Pago pendiente'}
                          </span>
                          <span className="px-2 py-0.5 rounded bg-slate-100 text-slate-700">{order.estado}</span>
                        </div>
                      </div>
                    </button>
                  )
                })}
              </div>
            ) : (
              <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 px-4 py-6 text-sm text-slate-600">
                No hay órdenes cargadas todavía. Busca por DPI para consultar las órdenes creadas desde la atención médica.
              </div>
            )}
          </div>

          <div className="rounded-xl border border-blue-200 bg-white p-5 shadow-sm space-y-4">
            <div className="flex items-start justify-between gap-3">
              <div>
                <h3 className="font-semibold text-slate-900">2) Pago y gestión de muestra</h3>
                <p className="text-xs text-slate-600 mt-1">Primero valida el pago del laboratorio; después habilita la recepción y el procesamiento.</p>
              </div>
              <button
                type="button"
                onClick={() => void handleRefreshOrder()}
                disabled={busy || !currentOrder}
                className="px-3 py-2 rounded-lg border border-blue-300 text-blue-700 text-sm hover:bg-blue-50 disabled:opacity-60"
              >
                Recargar
              </button>
            </div>

            {currentOrder ? (
              <>
                <div className="rounded-xl border border-slate-200 bg-slate-50 p-4 space-y-2 text-sm text-slate-700">
                  <p><span className="font-semibold text-slate-900">Orden:</span> #{currentOrder.ordenLaboratorioId}</p>
                  <p><span className="font-semibold text-slate-900">Examen:</span> {currentOrder.nombreExamen}</p>
                  <p><span className="font-semibold text-slate-900">Estado:</span> {currentOrder.estado}</p>
                  <p><span className="font-semibold text-slate-900">Pago:</span> {currentOrder.pagoValidado ? 'Validado' : 'Pendiente'}</p>
                  <p><span className="font-semibold text-slate-900">Etiqueta:</span> {currentOrder.etiquetaId || 'Aún no generada'}</p>
                  <p><span className="font-semibold text-slate-900">Observación técnica:</span> {currentOrder.observacionesTecnico || 'N/D'}</p>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-xs">
                  <div className={`rounded-lg border px-3 py-2 ${currentOrder.pagoValidado ? 'border-emerald-200 bg-emerald-50 text-emerald-700' : 'border-amber-200 bg-amber-50 text-amber-700'}`}>
                    <p className="font-semibold">Paso 1: Pago</p>
                    <p>{currentOrder.pagoValidado ? 'Pago ya validado.' : 'Pendiente de validación en laboratorio.'}</p>
                  </div>
                  <div className={`rounded-lg border px-3 py-2 ${currentOrder.estado === 'MUESTRA_RECIBIDA' || currentOrder.estado === 'EN_PROCESO' || currentOrder.estado === 'COMPLETADO' ? 'border-emerald-200 bg-emerald-50 text-emerald-700' : 'border-slate-200 bg-slate-50 text-slate-600'}`}>
                    <p className="font-semibold">Paso 2: Muestra</p>
                    <p>{currentOrder.estado === 'PENDIENTE_MUESTRA' ? 'Lista para recepción.' : currentOrder.estado === 'MUESTRA_RECHAZADA' ? 'Muestra rechazada.' : 'Gestiona recepción o procesamiento.'}</p>
                  </div>
                  <div className={`rounded-lg border px-3 py-2 ${currentOrder.resultado ? 'border-emerald-200 bg-emerald-50 text-emerald-700' : 'border-slate-200 bg-slate-50 text-slate-600'}`}>
                    <p className="font-semibold">Paso 3: Resultado</p>
                    <p>{currentOrder.resultado ? 'Resultado ya publicado.' : 'Disponible al estar en EN_PROCESO.'}</p>
                  </div>
                </div>

                <div className="flex flex-wrap gap-2">
                  <button
                    type="button"
                    onClick={openPaymentModal}
                    disabled={busy || !canValidatePayment}
                    className="px-3 py-2 rounded-lg bg-amber-500 text-white text-sm font-semibold hover:bg-amber-600 disabled:opacity-60"
                  >
                    Validar pago
                  </button>
                  <button
                    type="button"
                    onClick={() => void handleReceive()}
                    disabled={busy || !canReceiveSample}
                    className="px-3 py-2 rounded-lg bg-emerald-600 text-white text-sm font-semibold hover:bg-emerald-700 disabled:opacity-60"
                  >
                    Recibir muestra
                  </button>
                  <button
                    type="button"
                    onClick={() => void handleStartProcessing()}
                    disabled={busy || !canStartProcessing}
                    className="px-3 py-2 rounded-lg bg-blue-600 text-white text-sm font-semibold hover:bg-blue-700 disabled:opacity-60"
                  >
                    Iniciar proceso
                  </button>
                </div>

                <div className="flex flex-col md:flex-row gap-2">
                  <input
                    value={rejectReason}
                    onChange={(e) => setRejectReason(e.target.value.slice(0, MAX_TEXT_LENGTH))}
                    maxLength={MAX_TEXT_LENGTH}
                    placeholder="Motivo de rechazo técnico"
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  />
                  <button
                    type="button"
                    onClick={() => void handleReject()}
                    disabled={busy || !canRejectSample}
                    className="px-3 py-2 rounded-lg bg-amber-700 text-white text-sm font-semibold hover:bg-amber-800 disabled:opacity-60"
                  >
                    Rechazar muestra
                  </button>
                </div>
              </>
            ) : (
              <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 px-4 py-6 text-sm text-slate-600">
                Selecciona una orden para habilitar acciones de pago, recepción y procesamiento.
              </div>
            )}
          </div>

          <form onSubmit={handleAddResult} className="rounded-xl border border-blue-200 bg-white p-5 shadow-sm space-y-4 xl:col-span-2">
            <div>
              <h3 className="font-semibold text-slate-900">3) Registrar resultado</h3>
              <p className="text-xs text-slate-600 mt-1">Solo podrás guardar el resultado cuando la orden seleccionada esté en estado EN_PROCESO.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div className="px-3 py-2 border border-slate-200 rounded-lg bg-slate-50 text-sm text-slate-700">
                <span className="font-semibold text-slate-900">Orden seleccionada:</span> {currentOrder ? `#${currentOrder.ordenLaboratorioId}` : 'N/D'}
              </div>
              <div className="px-3 py-2 border border-slate-200 rounded-lg bg-slate-50 text-sm text-slate-700">
                <span className="font-semibold text-slate-900">Examen:</span> {currentOrder?.nombreExamen || 'N/D'}
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <input value={resultPayload.conclusion} onChange={(e) => setResultPayload((p) => ({ ...p, conclusion: e.target.value.slice(0, MAX_TEXT_LENGTH) }))} maxLength={MAX_TEXT_LENGTH} placeholder="Conclusión" className="px-3 py-2 border border-gray-300 rounded-lg text-sm" required />
              <input value={resultPayload.valorResultado} onChange={(e) => setResultPayload((p) => ({ ...p, valorResultado: e.target.value }))} placeholder="Valor resultado (opcional)" className="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              <input value={resultPayload.unidadResultado} onChange={(e) => setResultPayload((p) => ({ ...p, unidadResultado: e.target.value.slice(0, MAX_TEXT_LENGTH) }))} maxLength={MAX_TEXT_LENGTH} placeholder="Unidad" className="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              <input value={resultPayload.referenciaMinima} onChange={(e) => setResultPayload((p) => ({ ...p, referenciaMinima: e.target.value }))} placeholder="Referencia mínima (ej: 70)" className="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              <input value={resultPayload.referenciaMaxima} onChange={(e) => setResultPayload((p) => ({ ...p, referenciaMaxima: e.target.value }))} placeholder="Referencia máxima (ej: 110)" className="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              <input value={resultPayload.resumen} onChange={(e) => setResultPayload((p) => ({ ...p, resumen: e.target.value.slice(0, MAX_TEXT_LENGTH) }))} maxLength={MAX_TEXT_LENGTH} placeholder="Resumen" className="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              <input value={resultPayload.observaciones} onChange={(e) => setResultPayload((p) => ({ ...p, observaciones: e.target.value.slice(0, MAX_TEXT_LENGTH) }))} maxLength={MAX_TEXT_LENGTH} placeholder="Observaciones" className="px-3 py-2 border border-gray-300 rounded-lg text-sm md:col-span-2" />
            </div>

            <button type="submit" disabled={busy || !canSaveResult} className="px-4 py-2 rounded-lg bg-violet-600 text-white text-sm font-semibold hover:bg-violet-700 disabled:opacity-60">
              Guardar resultado
            </button>
          </form>
        </section>

        {currentOrder?.resultado && (
          <section className="mt-5 rounded-xl border border-blue-200 bg-white p-5 shadow-sm">
            <h3 className="font-semibold text-slate-900">Resultado publicado</h3>
            <div className="mt-3 rounded-lg border border-violet-100 bg-violet-50 p-4 text-sm text-slate-700 space-y-1">
              <p><span className="font-semibold text-slate-900">Conclusión:</span> {currentOrder.resultado.conclusion} {currentOrder.resultado.critico ? '(CRÍTICO)' : ''}</p>
              <p><span className="font-semibold text-slate-900">Resumen:</span> {currentOrder.resultado.resumen || 'N/D'}</p>
              <p><span className="font-semibold text-slate-900">Observaciones:</span> {currentOrder.resultado.observaciones || 'N/D'}</p>
              <p><span className="font-semibold text-slate-900">Valor:</span> {currentOrder.resultado.valorResultado ?? 'N/D'} {currentOrder.resultado.unidadResultado || ''}</p>
            </div>
          </section>
        )}

        {showPaymentModal && currentOrder && (
          <div className="fixed inset-0 z-50 bg-slate-900/40 flex items-center justify-center p-4">
            <div className="w-full max-w-3xl rounded-xl border border-blue-200 bg-white p-5 shadow-xl">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h3 className="text-lg font-bold text-slate-900">Validar pago de laboratorio</h3>
                  <p className="text-sm text-slate-600 mt-1">Orden #{currentOrder.ordenLaboratorioId} · Examen: {currentOrder.nombreExamen}</p>
                </div>
                <button
                  type="button"
                  onClick={() => {
                    setShowPaymentModal(false)
                    setPaymentForm(INITIAL_PAYMENT_FORM)
                  }}
                  className="px-2 py-1 text-slate-500 hover:text-slate-700"
                >
                  Cerrar
                </button>
              </div>

              <div className="mt-4 rounded-lg border border-blue-100 bg-blue-50 p-3 text-sm text-slate-700">
                <p className="font-semibold text-slate-900">Resumen de cobro</p>
                <p className="mt-1">Examen: {currentOrder.nombreExamen}</p>
                <p>DPI de referencia: {patientDpiInput || 'No capturado en esta búsqueda'}</p>
                <p className="mt-2 font-semibold">Total laboratorio: Q{LABORATORY_FEE.toFixed(2)}</p>
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
                <button type="button" onClick={() => {
                  setShowPaymentModal(false)
                  setPaymentForm(INITIAL_PAYMENT_FORM)
                }} className="px-4 py-2 rounded-lg border border-slate-300 text-sm text-slate-600 hover:bg-slate-50">Cancelar</button>
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

export default LaboratoryWorkbench

