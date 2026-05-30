import React from 'react'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import TriageIntake, { resolvePriority } from '@/pages/TriageIntake'
import { catalogAPI, triageAPI } from '@/services/api'

vi.mock('@/services/api', async () => {
  const actual = await vi.importActual<typeof import('@/services/api')>('@/services/api')
  return {
    ...actual,
    catalogAPI: {
      patientGenders: vi.fn(),
      insurances: vi.fn(),
    },
    triageAPI: {
      checkAvailability: vi.fn(),
      register: vi.fn(),
      create: vi.fn(),
      listRecent: vi.fn(),
      findPaidAppointmentByDpi: vi.fn(),
      findPaidAppointmentById: vi.fn(),
    },
  }
})

describe('TriageIntake quality checks', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(catalogAPI.patientGenders).mockResolvedValue({
      data: [
        { code: 'FEMENINO', label: 'Femenino' },
        { code: 'MASCULINO', label: 'Masculino' },
      ],
    } as any)
    vi.mocked(catalogAPI.insurances).mockResolvedValue({ data: [] } as any)
    vi.mocked(triageAPI.checkAvailability).mockResolvedValue({
      data: {
        dpiInUse: false,
        emailInUse: false,
        available: true,
        message: 'Disponibilidad valida para continuar con el registro.',
      },
    } as any)
  })

  it('marca ROJO cuando la presión sistólica es crítica aunque los otros signos estén estables', () => {
    const priority = resolvePriority({
      flowType: 'WALK_IN',
      citaMedicaId: '',
      nombreCompleto: '',
      dpi: '',
      fechaNacimiento: '',
      genero: '',
      telefono: '',
      email: '',
      direccion: '',
      contactoEmergencia: '',
      telefonoEmergencia: '',
      insuranceMode: 'UNSELECTED',
      aseguradoraId: undefined,
      polizaSeguro: '',
      bancoTarjeta: '',
      numeroTarjeta: '',
      fechaVencimientoTarjeta: '',
      nombreTitularTarjeta: '',
      cvcTarjeta: '',
      presionSistolica: '79',
      presionDiastolica: '50',
      frecuenciaCardiaca: '72',
      temperatura: '37',
      saturacionOxigeno: '98',
      pesoKg: '70',
      tallaCm: '170',
    })

    expect(priority).toBe('ROJO')
  })

  it('mantiene la fase 3 enfocada solo en validación de pago para walk-in', async () => {
    const { container } = render(
      <MemoryRouter>
        <TriageIntake />
      </MemoryRouter>,
    )

    await waitFor(() => expect(catalogAPI.patientGenders).toHaveBeenCalled())

    fireEvent.change(container.querySelector('input[name="nombreCompleto"]')!, { target: { value: 'Ana Torres' } })
    fireEvent.change(container.querySelector('input[name="dpi"]')!, { target: { value: '1234567890123' } })
    fireEvent.change(container.querySelector('input[name="fechaNacimiento"]')!, { target: { value: '1995-05-10' } })
    fireEvent.change(container.querySelector('select[name="genero"]')!, { target: { value: 'FEMENINO' } })
    fireEvent.change(container.querySelector('input[name="telefono"]')!, { target: { value: '55112233' } })
    fireEvent.change(container.querySelector('input[name="email"]')!, { target: { value: 'ana@example.com' } })
    fireEvent.change(container.querySelector('input[name="direccion"]')!, { target: { value: 'Zona 1' } })

    fireEvent.click(screen.getByRole('button', { name: 'Siguiente fase' }))
    await waitFor(() => expect(triageAPI.checkAvailability).toHaveBeenCalledWith('1234567890123', 'ana@example.com'))

    fireEvent.change(container.querySelector('input[name="contactoEmergencia"]')!, { target: { value: 'Maria Torres' } })
    fireEvent.change(container.querySelector('input[name="telefonoEmergencia"]')!, { target: { value: '55443322' } })
    fireEvent.click(screen.getByRole('button', { name: 'Siguiente fase' }))

    expect(screen.getByText('Selecciona modalidad de pago')).toBeInTheDocument()
    expect(screen.queryByText('Origen del paciente')).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Llega con cita programada' })).not.toBeInTheDocument()
  })

  it('FA06 permite avanzar y guardar triaje sin correo cuando fases 1 y 2 se precargan desde expediente', async () => {
    vi.mocked(triageAPI.register).mockResolvedValue({
      data: {
        pacienteId: 10,
        citaMedicaId: 20,
        pacienteNuevo: false,
        pagoValidado: true,
        mensaje: 'Registro completado y pago validado',
      },
    } as any)
    vi.mocked(triageAPI.create).mockResolvedValue({
      data: {
        pacienteId: 10,
        nombreCompleto: 'Paciente Existente',
        dpi: '1234567890123',
        pacienteNuevo: false,
        signosVitalesId: 20,
        citaMedicaId: 20,
        prioridad: 'VERDE',
        alertaEmergencia: false,
        pagoValidado: true,
        mensajePago: 'Triaje registrado',
        presionSistolica: 120,
        presionDiastolica: 80,
        frecuenciaCardiaca: 70,
        temperatura: 37,
        saturacionOxigeno: 98,
        pesoKg: 70,
        tallaCm: 170,
      },
    } as any)

    const patientData = encodeURIComponent(
      JSON.stringify({
        nombreCompleto: 'Paciente Existente',
        dpi: '1234567890123',
        fechaNacimiento: '1990-01-01',
        genero: 'FEMENINO',
        telefono: '55112233',
        email: '',
        direccion: 'Zona 1',
        contactoEmergencia: 'Maria',
        telefonoEmergencia: '55443322',
      })
    )

    const { container } = render(
      <MemoryRouter initialEntries={[`/triage/intake?mode=WALK_IN&skipToInsurance=true&patientDataJson=${patientData}`]}>
        <TriageIntake />
      </MemoryRouter>,
    )

    await waitFor(() => expect(screen.getByText('Selecciona modalidad de pago')).toBeInTheDocument())

    fireEvent.click(screen.getByRole('button', { name: 'Sin seguro' }))
    fireEvent.change(container.querySelector('input[name="bancoTarjeta"]')!, { target: { value: 'Banco Demo' } })
    fireEvent.change(container.querySelector('input[name="nombreTitularTarjeta"]')!, { target: { value: 'Paciente Existente' } })
    fireEvent.change(container.querySelector('input[name="numeroTarjeta"]')!, { target: { value: '4111111111111111' } })
    fireEvent.change(container.querySelector('input[name="fechaVencimientoTarjeta"]')!, { target: { value: '12/30' } })
    fireEvent.change(container.querySelector('input[name="cvcTarjeta"]')!, { target: { value: '123' } })
    fireEvent.click(screen.getByRole('button', { name: 'Siguiente fase' }))

    fireEvent.change(container.querySelector('input[name="presionSistolica"]')!, { target: { value: '120' } })
    fireEvent.change(container.querySelector('input[name="presionDiastolica"]')!, { target: { value: '80' } })
    fireEvent.change(container.querySelector('input[name="frecuenciaCardiaca"]')!, { target: { value: '70' } })
    fireEvent.change(container.querySelector('input[name="temperatura"]')!, { target: { value: '37' } })
    fireEvent.change(container.querySelector('input[name="saturacionOxigeno"]')!, { target: { value: '98' } })
    fireEvent.change(container.querySelector('input[name="pesoKg"]')!, { target: { value: '70' } })
    fireEvent.change(container.querySelector('input[name="tallaCm"]')!, { target: { value: '170' } })

    fireEvent.click(screen.getByRole('button', { name: 'Guardar triaje' }))

    await waitFor(() => expect(triageAPI.register).toHaveBeenCalled())
    expect(triageAPI.checkAvailability).not.toHaveBeenCalled()
    expect(screen.queryByText('El correo electronico es obligatorio.')).not.toBeInTheDocument()
  })
})


