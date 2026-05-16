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

    if (hasSession && (status === 401 || status === 403)) {
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
}

export type TriagePriority = 'ROJO' | 'NARANJA' | 'AMARILLO' | 'VERDE'

/**
 * CU 2.0 — Solicitud unificada de ingreso y triaje.
 * Combina ficha del paciente + signos vitales.
 * El personalId lo resuelve el backend desde el JWT.
 */
export interface TriageRequest {
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
  prioridad: TriagePriority      // RN04 — calculada en dominio, nunca en frontend
  alertaEmergencia: boolean      // FA03 — true si prioridad es ROJO
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

export const triageAPI = {
  /** POST /api/triage — CU 2.0: registro de paciente + signos vitales + prioridad */
  create: (data: TriageRequest) =>
    api.post<TriageResponse>('/triage', data),

  /** GET /api/triage — listado cronológico de triages recientes */
  listRecent: () =>
    api.get<TriageListItemResponse[]>('/triage'),
}

export default api

