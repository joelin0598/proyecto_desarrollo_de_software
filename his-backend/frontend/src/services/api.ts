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

export interface PatientRequest {
  firstName: string
  lastName: string
  dpi: string
  fechaNacimiento: string
  genero: string
  telefono: string
  direccion: string
  email: string
}

export interface PatientResponse {
  id: number
  firstName: string
  lastName: string
  dpi: string
  fechaNacimiento: string  // ISO date: YYYY-MM-DD (from Java LocalDate)
  genero: string
  telefono: string
  direccion: string
  email: string
}

// Patient endpoints
export const patientAPI = {
  create: (data: PatientRequest) =>
    api.post<PatientResponse>('/patients', data),

  getAll: () =>
    api.get<PatientResponse[]>('/patients'),

  getById: (id: number) =>
    api.get<PatientResponse>(`/patients/${id}`),
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

export default api

