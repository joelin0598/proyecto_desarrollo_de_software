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

// CU-04: Citas
export interface AppointmentRequest {
  specialty: string
  doctorName: string
  appointmentDate: string
  appointmentTime: string
  reason: string
  insurerName?: string
  policyNumber?: string
  holderDpi?: string
}

export interface AppointmentResponse {
  id: number
  patientId: number
  patientName: string
  specialty: string
  doctorName: string
  appointmentDate: string
  appointmentTime: string
  reason: string
  insurerName?: string
  policyNumber?: string
  status: 'PENDING_PAYMENT' | 'VALIDATED' | 'CANCELLED'
  baseTariff?: number
  deductible?: number
  createdAt: string
  auditNote?: string
  message?: string
}

// CU-05: Pagos
export interface PaymentRequest {
  appointmentId: number
  paymentMethod: 'CASH' | 'CARD' | 'INSURANCE'
  authorizationNumber?: string
  totalAmount: number
  insuranceCoverage?: number
  pendingBalance?: number
  invoiceNumber: string
  emergencyBypass?: boolean
}

export interface PaymentResponse {
  id: number
  appointmentId: number
  patientId: number
  patientName: string
  specialty: string
  doctorName: string
  paymentMethod: string
  authorizationNumber?: string
  totalAmount: number
  insuranceCoverage?: number
  pendingBalance?: number
  invoiceNumber: string
  paymentStatus: 'PENDING' | 'PAID' | 'BLOCKED'
  appointmentStatus: string
  emergencyBypass?: boolean
  createdAt: string
  auditNote?: string
  message?: string
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

// CU-04: Appointment endpoints
export const appointmentAPI = {
  schedule: (data: AppointmentRequest) =>
    api.post<AppointmentResponse>('/appointments', data),

  getMyAppointments: () =>
    api.get<AppointmentResponse[]>('/appointments/my'),

  getPendingPayment: () =>
    api.get<AppointmentResponse[]>('/appointments/pending-payment'),
}

// CU-05: Payment endpoints
export const paymentAPI = {
  getPending: () =>
    api.get<AppointmentResponse[]>('/payments/pending'),

  registerPayment: (data: PaymentRequest) =>
    api.post<PaymentResponse>('/payments', data),
}

export default api

