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
  firstName: string
  lastName: string
  email: string
  password: string
}

export interface RegisterAdminRequest extends RegisterRequest {
  telefono: string
  direccion: string
  dpi: string
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

export default api

