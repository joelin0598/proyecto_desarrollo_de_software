import axios from 'axios'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'
const AUTH_TOKEN_KEY = 'token'
const AUTH_USER_KEY = 'user'
const storage = window.sessionStorage

const api = axios.create({
  baseURL: API_URL,
})

// Interceptor para agregar token a las peticiones
api.interceptors.request.use((config) => {
  const token = storage.getItem(AUTH_TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    const hasSession = !!storage.getItem(AUTH_TOKEN_KEY)

    if (hasSession && status === 401) {
      storage.removeItem(AUTH_TOKEN_KEY)
      storage.removeItem(AUTH_USER_KEY)
      // Limpia llaves legacy por compatibilidad con sesiones antiguas.
      window.localStorage.removeItem(AUTH_TOKEN_KEY)
      window.localStorage.removeItem(AUTH_USER_KEY)
      window.dispatchEvent(new Event('auth:unauthorized'))
    }

    return Promise.reject(error)
  }
)

export type UserRole =
  | 'PACIENTE'
  | 'ADMIN'
  | 'DOCTOR'
  | 'ENFERMERA'
  | 'LABORATORISTA'
  | 'FARMACEUTICO'
  | 'ADMINISTRATIVO'
  | 'RECEPCION'

export type PatientGender = 'MASCULINO' | 'FEMENINO' | 'NO_ESPECIFICA'

export const HOSPITAL_STAFF_ROLES: UserRole[] = [
  'ADMIN',
  'DOCTOR',
  'ENFERMERA',
  'LABORATORISTA',
  'FARMACEUTICO',
  'ADMINISTRATIVO',
  'RECEPCION',
]

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  nombreCompleto: string
  email: string
  password: string
  dpi: string
  genero: PatientGender
  fechaNacimiento?: string
  direccion?: string
  telefono?: string
  contactoEmergencia?: string
  telefonoEmergencia?: string
  aseguradoraId?: number
}

export interface PatientGenderOption {
  code: PatientGender
  label: string
}

export interface InsuranceOption {
  id: number
  nombre: string
}

export interface SpecialtyOption {
  id: number
  nombre: string
  descripcion?: string
}

export interface CareUnitOption {
  id: number
  nombre: string
}

export interface DoctorOption {
  personalId: number
  nombreCompleto: string
  especialidadId?: number
  numeroColegiado?: string
}

export interface PatientLookupResponse {
  pacienteId: number
  nombreCompleto: string
  dpi: string
  fechaNacimiento?: string | null
  genero?: string | null
  telefono?: string | null
  emailContacto?: string | null
  direccion?: string | null
  contactoEmergencia?: string | null
  telefonoEmergencia?: string | null
}

export interface RegisterAdminRequest {
  nombreCompleto: string
  email: string
  password: string
  direccion: string
  telefonoCorporativo: string
  especialidadId?: number
  unidadAtencionId?: number
  rol: UserRole
  numeroColegiado?: string
}

export interface UserMaintenanceResponse {
  userId: number
  email: string
  role: UserRole
  active: boolean
  personalId?: number | null
  nombreCompleto?: string | null
  numeroColegiado?: string | null
  telefonoCorporativo?: string | null
  direccion?: string | null
  especialidadId?: number | null
  unidadAtencionId?: number | null
}

export interface UserMaintenanceCreateRequest {
  nombreCompleto: string
  email: string
  password: string
  direccion: string
  telefonoCorporativo: string
  especialidadId?: number
  unidadAtencionId?: number
  rol: UserRole
  numeroColegiado?: string
}

export interface UserMaintenanceUpdateRequest {
  nombreCompleto?: string
  direccion?: string
  telefonoCorporativo?: string
  especialidadId?: number
  unidadAtencionId?: number
  rol?: UserRole
  numeroColegiado?: string | null
}

export type PaymentOption = 'TARJETA' | 'SEGURO'
export type AdministrativeAppointmentStatus = 'PAGO_VALIDADO' | 'PAGO_PENDIENTE'
export type AppointmentStatus = 'PROGRAMADA' | 'EN_CURSO' | 'CANCELADA' | 'ATENDIDA'

export interface ScheduleAppointmentRequest {
  pacienteId?: number
  dpiPaciente?: string
  medicoPersonalId: number
  especialidadId?: number
  fechaCita: string
  horaCita: string
  motivoConsulta: string
  metodoPago: PaymentOption
  bancoTarjeta?: string
  numeroTarjeta?: string
  fechaVencimientoTarjeta?: string
  nombreTitularTarjeta?: string
  cvc?: string
  aseguradoraId?: number
  numeroPoliza?: string
}

export interface ScheduleAppointmentResponse {
  citaMedicaId: number
  pacienteId: number
  pacienteNombre?: string
  pacienteIdentificacion?: string
  medicoPersonalId: number
  medicoNombre?: string
  especialidadId?: number
  especialidadNombre?: string
  fechaCita: string
  horaCita: string
  motivoConsulta: string
  metodoPago: PaymentOption
  costoConsulta: number
  estadoCita: AppointmentStatus
  estadoAdministrativo: AdministrativeAppointmentStatus
  pagoValidado: boolean
  transaccionId?: string
  codigoCita?: string | null
  qrContenido?: string | null
  mensajeValidacion: string
}

export interface MedicalAppointmentQueueItemResponse {
  citaMedicaId: number
  pacienteId: number
  pacienteNombre: string
  pacienteDpi?: string | null
  fechaCita?: string | null
  horaCita?: string | null
  motivoConsulta?: string | null
  especialidadNombre?: string | null
  prioridad: string
  alertaEmergencia?: boolean
  tipoAtencion?: string | null
  presionSistolica?: number | null
  presionDiastolica?: number | null
  frecuenciaCardiaca?: number | null
  temperatura?: number | null
  saturacionOxigeno?: number | null
  estadoAdministrativo?: AdministrativeAppointmentStatus | string | null
}

export interface MedicalAppointmentAttentionResponse {
  citaMedicaDetalleId: number
  citaMedicaId?: number | null
  pacienteId: number
  pacienteNombre: string
  pacienteDpi?: string | null
  personalId: number
  medicoNombre: string
  estado: AppointmentStatus
  evaluacionFisica?: string | null
  diagnostico?: string | null
  ordenLaboratorio?: string | null
  recetaMedica?: string | null
  medicacionPrescrita?: string | null
  requiereSeguimiento?: boolean | null
  citaSeguimientoId?: number | null
  createdAt?: string | null
  fechaCita?: string | null
  horaCita?: string | null
  motivoConsulta?: string | null
  especialidadNombre?: string | null
  prioridad?: string | null
}

export interface CloseMedicalAppointmentAttentionRequest {
  evaluacionFisica: string
  diagnostico: string
  ordenLaboratorio?: string
  recetaMedica?: string
  medicacionPrescrita?: string
  requiereSeguimiento?: boolean
}

export interface AuthResponse {
  token: string
  user: {
    id: number
    email: string
    firstName: string
    lastName: string
    role: UserRole
  }
}

export interface ErrorResponse {
  errorMessage: string
}

export type LaboratoryOrderStatus = 'PENDIENTE_PAGO' | 'PENDIENTE_MUESTRA' | 'MUESTRA_RECIBIDA' | 'EN_PROCESO' | 'MUESTRA_RECHAZADA' | 'COMPLETADO' | 'FINALIZADO'

export interface CreateLaboratoryOrderRequest {
  citaMedicaDetalleId: number
  nombreExamen: string
  tipoMuestra?: string
}

export interface LaboratoryPaymentRequest {
  dpiPaciente?: string
  metodoPago: PaymentOption
  bancoTarjeta?: string
  numeroTarjeta?: string
  fechaVencimientoTarjeta?: string
  nombreTitularTarjeta?: string
  cvc?: string
  aseguradoraId?: number
  numeroPoliza?: string
}

export interface AddLaboratoryResultRequest {
  ordenLaboratorioId: number
  nombreExamen: string
  valorResultado?: number
  unidadResultado?: string
  referenciaMinima?: number
  referenciaMaxima?: number
  observaciones?: string
  resumen?: string
  conclusion: string
}

export interface LaboratoryResultResponse {
  resultadoLaboratorioId: number
  ordenLaboratorioId: number
  nombreExamen: string
  valorResultado?: number
  unidadResultado?: string
  referenciaMinima?: number
  referenciaMaxima?: number
  observaciones?: string
  resumen?: string
  conclusion: string
  critico: boolean
  createdAt?: string
}

export interface LaboratoryOrderResponse {
  ordenLaboratorioId: number
  citaMedicaDetalleId: number
  nombreExamen: string
  tipoMuestra?: string
  estado: LaboratoryOrderStatus
  pagoValidado: boolean
  etiquetaId?: string
  alertaCritica: boolean
  observacionesTecnico?: string
  createdAt?: string
  resultado?: LaboratoryResultResponse
}

export interface UpdatePatientProfileRequest {
  telefono?: string
  direccion?: string
  genero?: PatientGender
}

export interface PatientProfileResponse {
  id: number
  nombre: string
  dpi: string
  email: string
  fechaNacimiento?: string
  genero?: string
  telefono?: string
  direccion?: string
}

export interface MedicalRecordResponse {
  patientId: number
  nombreCompleto: string
  dpi: string
  genero?: PatientGender
  fechaNacimiento?: string
  telefono?: string
  direccion?: string
  appointments: Array<{
    citaMedicaId: number
    fechaCita?: string
    horaCita?: string
    motivoConsulta?: string
    estadoCita?: string
    estadoAdministrativo?: string
    solvenciaPago?: boolean
  }>
  triages: Array<{
    citaMedicaId: number
    prioridad?: TriagePriority
    alertaEmergencia?: boolean
    presionSistolica?: number
    presionDiastolica?: number
    frecuenciaCardiaca?: number
    temperatura?: number
    saturacionOxigeno?: number
    pesoKg?: number
    tallaCm?: number
    fechaHoraTriaje?: string
  }>
  prescriptions: Array<{
    recetaMedicaId: number
    citaMedicaDetalleId: number
    fechaEmision?: string
    instruccionesGenerales?: string
    createdAt?: string
    items: Array<{
      recetaMedicaDetalleId: number
      medicamentoId: number
      medicamentoNombre?: string
      cantidad?: number
      dosis?: string
      viaAdministracion?: string
      frecuenciaHoras?: number
      duracionDias?: number
      despachado?: boolean
      pagoValidado?: boolean
    }>
  }>
  laboratoryResults: LaboratoryOrderResponse[]
}

export interface CreatePrescriptionRequest {
  citaMedicaDetalleId: number
  instruccionesGenerales?: string
  items: Array<{
    medicamentoId: number
    cantidad: number
    dosis?: string
    viaAdministracion?: string
    frecuenciaHoras?: number
    duracionDias?: number
  }>
}

export interface PrescriptionDetailResponse {
  recetaMedicaDetalleId: number
  medicamentoId: number
  medicamentoNombre?: string
  cantidad: number
  dosis?: string
  viaAdministracion?: string
  frecuenciaHoras?: number
  duracionDias?: number
  stockActual?: number
  precioUnitario?: number
  subtotal?: number
  disponible?: boolean
  despachado: boolean
  pagoValidado: boolean
}

export interface PrescriptionResponse {
  recetaMedicaId: number
  citaMedicaDetalleId: number
  pacienteId?: number | null
  pacienteNombre?: string | null
  pacienteDpi?: string | null
  medicoNombre?: string | null
  estadoAdministrativo?: string | null
  instruccionesGenerales?: string
  fechaEmision: string
  createdAt?: string
  pagoFarmaciaValidado?: boolean
  despachada?: boolean
  totalMedicamentos?: number
  items: PrescriptionDetailResponse[]
}

export interface DispenseMedicineRequest {
  recetaMedicaDetalleId: number
}

export interface PharmacyPaymentRequest {
  dpiPaciente?: string
  metodoPago: PaymentOption
  bancoTarjeta?: string
  numeroTarjeta?: string
  fechaVencimientoTarjeta?: string
  nombreTitularTarjeta?: string
  cvc?: string
  aseguradoraId?: number
  numeroPoliza?: string
}

export interface PharmacyPrescriptionLookupResponse {
  pacienteId: number
  pacienteNombre: string
  pacienteDpi: string
  recetas: PrescriptionResponse[]
}

export interface MedicineResponse {
  medicamentoId: number
  nombre: string
  presentacion?: string
  descripcion?: string
  stockActual: number
  precioUnitario?: number
}

export interface MedicationReminderResponse {
  recordatorioId: number
  medicamentoNombre: string
  dosis?: string
  frecuenciaHoras?: number
  duracionDias?: number
  viaAdministracion?: string
  proximoRecordatorio?: string
  activo: boolean
}

// Auth endpoints
export const authAPI = {
  login: (data: LoginRequest) =>
    api.post<AuthResponse>('/auth/authenticate', data),

  register: (data: RegisterRequest) =>
    api.post<AuthResponse>('/auth/register', data),

  registerPersonal: (data: RegisterAdminRequest) =>
    api.post<AuthResponse>('/auth/register/personal', data),

  // Endpoint legacy mantenido por compatibilidad.
  registerAdmin: (data: RegisterAdminRequest) =>
    api.post<AuthResponse>('/auth/register/admin', data),

  logout: () =>
    api.post('/auth/logout'),
}

export const catalogAPI = {
  patientGenders: () =>
    api.get<PatientGenderOption[]>('/catalogs/patient-genders'),

  insurances: () =>
    api.get<InsuranceOption[]>('/catalogs/insurances'),

  specialties: () =>
    api.get<SpecialtyOption[]>('/catalogs/specialties'),

  careUnits: () =>
    api.get<CareUnitOption[]>('/catalogs/care-units'),

  doctorsBySpecialty: (especialidadId?: number) =>
    api.get<DoctorOption[]>('/catalogs/doctors', {
      params: especialidadId ? { especialidadId } : undefined,
    }),
}

export const laboratoryAPI = {
  createOrder: (data: CreateLaboratoryOrderRequest) =>
    api.post<LaboratoryOrderResponse>('/laboratory/orders', data),

  validateOrderPayment: (ordenLaboratorioId: number, data: LaboratoryPaymentRequest) =>
    api.post<LaboratoryOrderResponse>(`/laboratory/orders/${ordenLaboratorioId}/payment`, data),

  getOrdersByDetalle: (citaMedicaDetalleId: number) =>
    api.get<LaboratoryOrderResponse[]>(`/laboratory/orders/by-detalle/${citaMedicaDetalleId}`),

  getOrdersByPatientDpi: (dpi: string) =>
    api.get<LaboratoryOrderResponse[]>('/laboratory/orders/by-patient-dpi', { params: { dpi } }),

  receiveSample: (ordenLaboratorioId: number) =>
    api.patch<LaboratoryOrderResponse>(`/laboratory/orders/${ordenLaboratorioId}/receive`),

  startProcessing: (ordenLaboratorioId: number) =>
    api.patch<LaboratoryOrderResponse>(`/laboratory/orders/${ordenLaboratorioId}/start-processing`),

  rejectSample: (ordenLaboratorioId: number, motivo: string) =>
    api.patch<LaboratoryOrderResponse>(`/laboratory/orders/${ordenLaboratorioId}/reject`, null, { params: { motivo } }),

  addResult: (data: AddLaboratoryResultRequest) =>
    api.post<LaboratoryOrderResponse>('/laboratory/orders/result', data),

  getOrder: (ordenLaboratorioId: number) =>
    api.get<LaboratoryOrderResponse>(`/laboratory/orders/${ordenLaboratorioId}`),

  getResultsByPatient: (patientId: number) =>
    api.get<LaboratoryOrderResponse[]>(`/laboratory/results-by-patient/${patientId}`),
}

export const pharmacyAPI = {
  listMedicines: () =>
    api.get<MedicineResponse[]>('/pharmacy/medicines'),

  createPrescription: (data: CreatePrescriptionRequest) =>
    api.post<PrescriptionResponse>('/pharmacy/prescriptions', data),

  getPrescriptionByDetalle: (citaMedicaDetalleId: number) =>
    api.get<PrescriptionResponse>(`/pharmacy/prescriptions/by-detalle/${citaMedicaDetalleId}`),

  getPrescriptionsByDpi: (dpi: string) =>
    api.get<PharmacyPrescriptionLookupResponse>('/pharmacy/prescriptions/by-dpi', { params: { dpi } }),

  validatePrescriptionPayment: (recetaMedicaId: number, data: PharmacyPaymentRequest) =>
    api.post<PrescriptionResponse>(`/pharmacy/prescriptions/${recetaMedicaId}/payment`, data),

  dispensePrescription: (recetaMedicaId: number) =>
    api.post<PrescriptionResponse>(`/pharmacy/prescriptions/${recetaMedicaId}/dispense`),

  dispense: (data: DispenseMedicineRequest) =>
    api.post<PrescriptionResponse>('/pharmacy/dispense', data),

  getRemindersByPatientId: (pacienteId: number) =>
    api.get<MedicationReminderResponse[]>(`/pharmacy/reminders/${pacienteId}`),

  getMyReminders: () =>
    api.get<MedicationReminderResponse[]>('/pharmacy/reminders/me'),
}

export type TriagePriority = 'ROJO' | 'NARANJA' | 'AMARILLO' | 'VERDE'

/** Solicitud unificada de ingreso y triaje. */
export interface TriageRequest {
  citaMedicaId?: number
  // Datos personales
  nombreCompleto: string
  dpi: string
  fechaNacimiento?: string       // ISO date YYYY-MM-DD
  genero: PatientGender
  emailContacto?: string
  telefono?: string
  direccion?: string
  // Contacto emergencia
  contactoEmergencia: string
  telefonoEmergencia: string
  // Seguro (opcional)
  aseguradoraId?: number
  polizaSeguro?: string
  // Pago para flujo walk-in
  metodoPago?: PaymentOption
  bancoTarjeta?: string
  numeroTarjeta?: string
  fechaVencimientoTarjeta?: string
  nombreTitularTarjeta?: string
  cvc?: string
  // Signos vitales
  presionSistolica: number
  presionDiastolica: number
  frecuenciaCardiaca: number
  temperatura: number
  saturacionOxigeno: number
  pesoKg: number
  tallaCm: number
}

/** Respuesta del backend: el pacienteId creado/encontrado + prioridad calculada en dominio. */
export interface TriageResponse {
  pacienteId: number
  nombreCompleto: string
  dpi: string
  pacienteNuevo: boolean
  signosVitalesId: number
  citaMedicaId?: number | null
  prioridad: TriagePriority      // RN04 — calculada en dominio, nunca en frontend
  alertaEmergencia: boolean      // FA03 — true si prioridad es ROJO
  pagoValidado: boolean
  mensajePago: string
  presionSistolica: number
  presionDiastolica: number
  frecuenciaCardiaca: number
  temperatura: number
  saturacionOxigeno: number
  pesoKg: number
  tallaCm: number
}

export interface TriageListItemResponse {
  signosVitalesId: number
  pacienteId: number
  fechaHoraRegistro: string
  nombreCompleto: string
  dpi: string
  prioridad: TriagePriority
  alertaEmergencia: boolean
  presionSistolica: number
  presionDiastolica: number
  frecuenciaCardiaca: number
  temperatura: number
  saturacionOxigeno: number
  pesoKg: number
  tallaCm: number
}

export interface TriagePaidAppointmentLookupResponse {
  citaMedicaId: number
  pacienteId: number
  pacienteNombre: string
  pacienteDpi: string
  fechaNacimiento?: string | null
  genero?: PatientGender | null
  telefono?: string | null
  emailContacto?: string | null
  direccion?: string | null
  contactoEmergencia?: string | null
  telefonoEmergencia?: string | null
  medicoPersonalId: number
  especialidadId?: number | null
  fechaCita?: string | null
  horaCita?: string | null
  motivoConsulta?: string | null
  estadoAdministrativo?: AdministrativeAppointmentStatus | string | null
}

export interface PatientRegisterRequest {
  nombreCompleto: string
  dpi: string
  fechaNacimiento?: string
  genero: PatientGender
  telefono?: string
  emailContacto?: string
  direccion?: string
  contactoEmergencia: string
  telefonoEmergencia: string
  metodoPago: PaymentOption
  bancoTarjeta?: string
  numeroTarjeta?: string
  fechaVencimientoTarjeta?: string
  nombreTitularTarjeta?: string
  cvc?: string
  aseguradoraId?: number
  polizaSeguro?: string
}

export interface PatientRegisterResponse {
  pacienteId: number
  citaMedicaId: number
  pacienteNuevo: boolean
  pagoValidado: boolean
  mensaje: string
}

export interface PatientAvailabilityResponse {
  dpiInUse: boolean
  emailInUse: boolean
  available: boolean
  message: string
}

export interface PatientTriageRequest {
  citaMedicaId?: number
  dpi?: string
  presionSistolica: number
  presionDiastolica: number
  frecuenciaCardiaca: number
  temperatura: number
  saturacionOxigeno: number
  pesoKg: number
  tallaCm: number
}

export const triageAPI = {
  /** GET /api/patients/availability — validación rápida de DPI/correo para fase 1 */
  checkAvailability: (dpi: string, email?: string) =>
    api.get<PatientAvailabilityResponse>('/patients/availability', {
      params: { dpi, email },
    }),

  /** POST /api/patients/register — fase de registro + validación administrativa */
  register: (data: PatientRegisterRequest) =>
    api.post<PatientRegisterResponse>('/patients/register', data),

  /** POST /api/patients/triage — registro de signos vitales + prioridad */
  create: (data: PatientTriageRequest) =>
    api.post<TriageResponse>('/patients/triage', data),

  /** GET /api/triage — listado cronológico de triages recientes */
  listRecent: () =>
    api.get<TriageListItemResponse[]>('/triage'),

  /** GET /api/triage/paid-appointment?dpi=... — búsqueda de cita pagada para vincular triaje */
  findPaidAppointmentByDpi: (dpi: string) =>
    api.get<TriagePaidAppointmentLookupResponse>('/triage/paid-appointment', { params: { dpi } }),

  /** GET /api/triage/paid-appointment?citaMedicaId=... — búsqueda de cita pagada por ID */
  findPaidAppointmentById: (citaMedicaId: number) =>
    api.get<TriagePaidAppointmentLookupResponse>('/triage/paid-appointment', { params: { citaMedicaId } }),
}

export const patientAPI = {
  lookupByDpi: (dpi: string) =>
    api.get<PatientLookupResponse>('/patients/lookup', { params: { dpi } }),

  getMyProfile: () =>
    api.get<PatientProfileResponse>('/patients/me'),

  updateProfile: (data: UpdatePatientProfileRequest) =>
    api.put<PatientProfileResponse>('/patients/profile/edit', data),

  getMedicalRecord: (patientId: number) =>
    api.get<MedicalRecordResponse>(`/patients/medical-record/${patientId}`),
}

export const userMaintenanceAPI = {
  list: () =>
    api.get<UserMaintenanceResponse[]>('/users/maintenance'),

  create: (data: UserMaintenanceCreateRequest) =>
    api.post<UserMaintenanceResponse>('/users/maintenance/staff', data),

  update: (userId: number, data: UserMaintenanceUpdateRequest) =>
    api.patch<UserMaintenanceResponse>(`/users/maintenance/${userId}`, data),

  suspend: (userId: number) =>
    api.patch<UserMaintenanceResponse>(`/users/maintenance/${userId}/suspend`),

  delete: (userId: number) =>
    api.delete<void>(`/users/maintenance/${userId}`),
}

export const appointmentAPI = {
  schedule: (data: ScheduleAppointmentRequest) =>
    api.post<ScheduleAppointmentResponse>('/appointments', data),

  list: () =>
    api.get<ScheduleAppointmentResponse[]>('/appointments'),
}

export const appointmentAttentionAPI = {
  queue: () =>
    api.get<MedicalAppointmentQueueItemResponse[]>('/appointments/attention/queue'),

  current: () =>
    api.get<MedicalAppointmentAttentionResponse>('/appointments/attention/current', {
      validateStatus: (status) => status >= 200 && status < 300,
    }),

  open: (citaMedicaId: number) =>
    api.post<MedicalAppointmentAttentionResponse>('/appointments/attention/open', null, {
      params: { citaMedicaId },
    }),

  cancel: () =>
    api.post('/appointments/attention/cancel'),

  close: (citaMedicaDetalleId: number, data: CloseMedicalAppointmentAttentionRequest) =>
    api.patch<MedicalAppointmentAttentionResponse>(`/appointments/attention/${citaMedicaDetalleId}/close`, data),
}

export default api
