import axios from 'axios'

const API_URL = 'http://localhost:8080/api'

const api = axios.create({
  baseURL: API_URL,
})

// Interceptor para agregar token a las peticiones
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

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
}

export interface AuthResponse {
  token: string
  user: {
    id: number
    email: string
    firstName: string
    lastName: string
    role: 'USER' | 'ADMIN'
  }
}

export interface ErrorResponse {
  errorMessage: string
}

// --- Patient types ---
export interface PatientRequest {
  fullName: string
  dpi?: string
  birthDate?: string
  gender?: string
  phone?: string
  email?: string
  address?: string
  emergencyContactName?: string
  emergencyContactPhone?: string
  insurancePolicyNumber?: string
  insuranceProvider?: string
}

export interface PatientResponse {
  patientId: number
  fullName: string
  dpi?: string
  birthDate?: string
  gender?: string
  phone?: string
  email?: string
  address?: string
  emergencyContactName?: string
  emergencyContactPhone?: string
  insurancePolicyNumber?: string
  insuranceProvider?: string
  createdAt?: string
  updatedAt?: string
}

// --- Triage types ---
export type TriagePriority = 'RED' | 'ORANGE' | 'GREEN'

export interface TriageRequest {
  patientId: number
  systolicPressure?: number
  diastolicPressure?: number
  heartRate?: number
  temperature?: number
  oxygenSaturation?: number
  weight?: number
  notes?: string
}

export interface TriageResponse {
  triageId: number
  patient: PatientResponse
  systolicPressure?: number
  diastolicPressure?: number
  heartRate?: number
  temperature?: number
  oxygenSaturation?: number
  weight?: number
  priority: TriagePriority
  notes?: string
  arrivalTime: string
  registeredBy?: string
}

// Auth endpoints
export const authAPI = {
  login: (data: LoginRequest) =>
    api.post<AuthResponse>('/auth/authenticate', data),

  register: (data: RegisterRequest) =>
    api.post<AuthResponse>('/auth/register', data),

  registerAdmin: (data: RegisterAdminRequest) =>
    api.post<AuthResponse>('/auth/register/admin', data),

  logout: () =>
    api.post('/auth/logout'),
}

// Patient endpoints
export const patientAPI = {
  register: (data: PatientRequest) =>
    api.post<PatientResponse>('/patients', data),

  update: (patientId: number, data: PatientRequest) =>
    api.put<PatientResponse>(`/patients/${patientId}`, data),

  getById: (patientId: number) =>
    api.get<PatientResponse>(`/patients/${patientId}`),

  findByDpi: (dpi: string) =>
    api.get<PatientResponse>('/patients/search', { params: { dpi } }),

  getAll: () =>
    api.get<PatientResponse[]>('/patients'),
}

// Triage endpoints
export const triageAPI = {
  record: (data: TriageRequest) =>
    api.post<TriageResponse>('/triage', data),

  getHistory: (patientId: number) =>
    api.get<TriageResponse[]>(`/triage/patient/${patientId}`),

  getWaitingList: () =>
    api.get<TriageResponse[]>('/triage/waiting-list'),
}

export default api

