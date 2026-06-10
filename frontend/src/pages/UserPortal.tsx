import React from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { authAPI } from '@/services/api'
import StatusChip from '@/components/ui/StatusChip'
import HospitalLogo from '@/components/ui/HospitalLogo'

const mainPortalActions = [
  {
    title: 'Agendar cita',
    icon: '',
    accent: 'from-cyan-500 to-cyan-600',
    useCase: 'Citas',
    statusLabel: 'Disponible en portal',
    statusTone: 'blue' as const,
    detail: 'Programar consulta, seleccionar especialidad y validar cobertura de seguro.',
    bulletPoints: ['Especialidad y médico', 'Fecha y hora disponible', 'Cobertura o póliza'],
    action: 'Solicitar cita',
    route: '/portal/appointments',
  },
  {
    title: 'Consultar resultados',
    icon: '',
    accent: 'from-emerald-500 to-emerald-600',
    useCase: 'Laboratorio',
    statusLabel: 'Sujeto a solvencia',
    statusTone: 'amber' as const,
    detail: 'Visualizar y descargar resultados de laboratorio cuando el proceso técnico y administrativo esté completo.',
    bulletPoints: ['Resultados publicados', 'Rangos de referencia', 'Bloqueo si hay pendiente de pago'],
    action: 'Ver resultados',
    route: null,
  },
  {
    title: 'Recetas y recordatorios',
    icon: '',
    accent: 'from-violet-500 to-violet-600',
    useCase: 'Recetas',
    statusLabel: 'Seguimiento activo',
    statusTone: 'emerald' as const,
    detail: 'Consultar prescripciones vigentes, dosis, frecuencia y recordatorios asociados al tratamiento.',
    bulletPoints: ['Medicamento y dosis', 'Frecuencia del tratamiento', 'Historial de despacho'],
    action: 'Ver recetas',
    route: '/portal/reminders',
  },
]

const patientRecordModules = [
  {
    title: 'Expediente clínico',
    subtitle: 'Expediente clínico',
    icon: '',
    accent: 'from-sky-500 to-sky-600',
    statusLabel: 'Acceso protegido',
    statusTone: 'slate' as const,
    detail: 'Consulta de historial médico, controles previos, alergias y episodios registrados en el hospital.',
  },
  {
    title: 'Documentos clínicos',
    subtitle: 'Documentación',
    icon: '',
    accent: 'from-fuchsia-500 to-fuchsia-600',
    statusLabel: 'Descarga condicionada',
    statusTone: 'amber' as const,
    detail: 'Resultados, recetas e informes emitidos por el sistema, con restricciones por estado de pago.',
  },
  {
    title: 'Estado de cuenta',
    subtitle: 'Estado financiero',
    icon: '',
    accent: 'from-amber-500 to-amber-600',
    statusLabel: 'Control administrativo',
    statusTone: 'orange' as const,
    detail: 'Revisión de pendientes de pago, solvencia vigente, deducibles y autorizaciones de seguro.',
  },
]

const quickStatus = [
  { label: 'Portal', value: 'Paciente', tone: 'emerald' as const },
  { label: 'Solvencia', value: 'Pendiente de validación', tone: 'amber' as const },
  { label: 'Auditoría', value: 'Eventos protegidos', tone: 'blue' as const },
]

const UserPortal: React.FC = () => {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const [loading, setLoading] = React.useState(false)

  const handleLogout = async () => {
    setLoading(true)
    try {
      await authAPI.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      logout()
      navigate('/')
      setLoading(false)
    }
  }

  return (
    <div className="h-screen bg-gradient-to-br from-blue-100 via-sky-50 to-blue-100 text-slate-800 flex overflow-hidden">
      {/* Sidebar */}
      <aside className="w-64 h-full bg-blue-100/85 border-r border-blue-200 shadow-sm p-4 flex flex-col justify-between shrink-0 overflow-hidden">
        <div>
          <div className="mb-7">
            <p className="text-xs uppercase tracking-[0.2em] text-slate-400">HIS</p>
            <h1 className="text-xl font-bold text-slate-900 mt-1">Portal Paciente</h1>
            <p className="text-xs text-slate-600 mt-1">Gestión de salud personal</p>
          </div>
          <nav className="space-y-2">
            <button type="button" className="w-full text-left px-3 py-2 rounded-lg text-sm bg-white text-blue-700 border border-blue-200 font-semibold">Dashboard</button>
            <button
              type="button"
              onClick={() => navigate('/portal/my-appointments')}
              className="w-full text-left px-3 py-2 rounded-lg text-sm hover:bg-white/70 text-slate-700 transition"
            >
              Mis Citas
            </button>
            <button type="button" className="w-full text-left px-3 py-2 rounded-lg text-sm hover:bg-white/70 text-slate-700 transition">Servicios del portal</button>
            <button type="button" className="w-full text-left px-3 py-2 rounded-lg text-sm hover:bg-white/70 text-slate-700 transition">Expediente y documentos</button>
            <button type="button" className="w-full text-left px-3 py-2 rounded-lg text-sm hover:bg-white/70 text-slate-700 transition">Estado administrativo</button>
          </nav>
        </div>
        <div className="space-y-3">
          <div className="rounded-lg border border-blue-200 bg-blue-50/70 p-3">
            <p className="text-xs text-slate-500">Sesión actual</p>
            <p className="font-semibold text-slate-800 break-all text-sm">{user?.email}</p>
            <p className="text-xs text-slate-500 mt-1">Rol: {user?.role}</p>
          </div>
          <button onClick={() => void handleLogout()} disabled={loading} className="w-full px-4 py-2 rounded-lg bg-white hover:bg-slate-50 text-slate-700 border border-blue-200 font-semibold text-sm disabled:opacity-60 transition">
            {loading ? 'Cerrando...' : 'Cerrar sesión'}
          </button>
        </div>
      </aside>

      <main className="flex-1 p-5 lg:p-6 overflow-y-auto">
        {/* Header dashboard */}
        <div className="flex items-start justify-between gap-4 mb-5">
          <div>
            <h2 className="text-2xl font-bold text-slate-900">¡Bienvenido{user?.firstName ? `, ${user.firstName}` : ''}!</h2>
            <p className="text-sm text-slate-600 mt-1">Este dashboard concentra las opciones del paciente definidas en los casos de uso del sistema hospitalario.</p>
          </div>
          <StatusChip label="Paciente activo" tone="emerald" />
        </div>

        <section className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-5">
          {quickStatus.map((item) => (
            <article key={item.label} className="rounded-xl border border-blue-200 bg-white shadow-sm p-4">
              <p className="text-xs uppercase tracking-wide text-slate-500">{item.label}</p>
              <p className="text-lg font-bold text-slate-900 mt-1">{item.value}</p>
              <div className="mt-3">
                <StatusChip label={item.value} tone={item.tone} />
              </div>
            </article>
          ))}
        </section>

        <section className="mb-5 rounded-xl border border-blue-200 bg-white shadow-sm p-5">
          <div className="flex items-start justify-between gap-4 mb-4">
            <div>
              <h3 className="text-lg font-bold text-slate-900">Servicios principales del portal</h3>
              <p className="text-sm text-slate-600 mt-1">Accesos principales del portal de paciente.</p>
            </div>
            <StatusChip label="Portal paciente" tone="blue" />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            {mainPortalActions.map((mod) => (
              <article key={mod.title} className="rounded-xl border border-blue-200 bg-blue-50 p-4 shadow-sm hover:shadow-md hover:bg-white transition flex flex-col min-h-[260px]">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="text-xs font-bold text-blue-700 uppercase">{mod.useCase}</p>
                    <h4 className="text-base font-semibold text-slate-900 mt-1">{mod.title}</h4>
                  </div>
                  <span className={`inline-flex items-center justify-center h-10 w-10 rounded-lg text-lg bg-gradient-to-br ${mod.accent} text-white shrink-0`}>{mod.icon}</span>
                </div>

                <div className="mt-3">
                  <StatusChip label={mod.statusLabel} tone={mod.statusTone} />
                </div>

                <p className="text-sm text-slate-600 mt-3">{mod.detail}</p>

                <ul className="mt-4 space-y-2 text-sm text-slate-700 flex-1">
                  {mod.bulletPoints.map((point) => (
                    <li key={point} className="rounded-lg border border-blue-100 bg-white px-3 py-2">• {point}</li>
                  ))}
                </ul>

                <button
                  type="button"
                  onClick={() => mod.route && navigate(mod.route)}
                  disabled={!mod.route}
                  className="w-full mt-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold transition disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {mod.action}
                </button>
              </article>
            ))}
          </div>
        </section>

        <section className="grid grid-cols-1 xl:grid-cols-[1.4fr_1fr] gap-4 mb-5">
          <article className="rounded-xl border border-blue-200 bg-white shadow-sm p-5">
            <div className="flex items-start justify-between gap-4 mb-4">
              <div>
                <h3 className="font-semibold text-slate-900 flex items-center gap-2">
                  <span className="inline-flex items-center justify-center h-8 w-8 rounded-lg bg-gradient-to-br from-indigo-500 to-indigo-600 text-white text-sm">️</span>
                  Expediente y documentos del paciente
                </h3>
                <p className="text-sm text-slate-600 mt-1">Opciones derivadas de consulta del expediente y visualización/descarga documental.</p>
              </div>
              <StatusChip label="RN05 · Histórico inmutable" tone="slate" />
            </div>

            <div className="space-y-3">
              {patientRecordModules.map((module) => (
                <div key={module.title} className="rounded-xl border border-blue-200 bg-blue-50 p-4">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <p className="text-xs font-bold text-blue-700 uppercase">{module.subtitle}</p>
                      <h4 className="text-base font-semibold text-slate-900 mt-1">{module.title}</h4>
                    </div>
                    <span className={`inline-flex items-center justify-center h-9 w-9 rounded-lg text-lg bg-gradient-to-br ${module.accent} text-white shrink-0`}>{module.icon}</span>
                  </div>
                  <p className="text-sm text-slate-600 mt-3">{module.detail}</p>
                  <div className="mt-3">
                    <StatusChip label={module.statusLabel} tone={module.statusTone} />
                  </div>
                </div>
              ))}
            </div>
          </article>

          <article className="rounded-xl border border-blue-200 bg-white shadow-sm p-5">
            <h3 className="font-semibold text-slate-900 mb-4 flex items-center gap-2">
              <span className="inline-flex items-center justify-center h-8 w-8 rounded-lg bg-gradient-to-br from-rose-500 to-rose-600 text-white text-sm">⚖️</span>
              Reglas relevantes del portal
            </h3>
            <div className="space-y-3">
              <div className="rounded-lg border border-amber-200 bg-amber-50 p-4">
                <p className="text-sm font-semibold text-amber-800">RN03 · Solvencia administrativa</p>
                <p className="text-xs text-amber-700 mt-1">La descarga de resultados, atención y despacho pueden bloquearse si existe pendiente de pago o seguro no validado.</p>
              </div>
              <div className="rounded-lg border border-slate-200 bg-slate-50 p-4">
                <p className="text-sm font-semibold text-slate-800">RN01 · Sesión privada</p>
                <p className="text-xs text-slate-600 mt-1">La sesión del paciente debe cerrarse por inactividad para proteger información clínica sensible.</p>
              </div>
              <div className="rounded-lg border border-blue-200 bg-blue-50 p-4">
                <p className="text-sm font-semibold text-blue-800">CU00 · Panel personal</p>
                <p className="text-xs text-blue-700 mt-1">El dashboard debe concentrar acceso a citas, resultados, recetas, recordatorios y estado de cuenta.</p>
              </div>
            </div>
          </article>
        </section>

        <section className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          <article className="rounded-xl border border-blue-200 bg-white shadow-sm p-5">
            <h3 className="font-semibold text-slate-900 mb-4 flex items-center gap-2">
              <span className="inline-flex items-center justify-center h-8 w-8 rounded-lg bg-gradient-to-br from-slate-500 to-slate-600 text-white text-sm"></span>
              Información Personal
            </h3>
            <div className="space-y-3">
              <div className="rounded-lg bg-blue-50 border border-blue-100 px-4 py-2">
                <p className="text-xs text-slate-500 uppercase font-semibold">Nombre completo</p>
                <p className="text-sm font-semibold text-slate-800 mt-0.5">{user?.firstName} {user?.lastName}</p>
              </div>
              <div className="rounded-lg bg-blue-50 border border-blue-100 px-4 py-2">
                <p className="text-xs text-slate-500 uppercase font-semibold">Correo electrónico</p>
                <p className="text-sm font-semibold text-slate-800 mt-0.5 break-all">{user?.email}</p>
              </div>
              <div className="rounded-lg bg-blue-50 border border-blue-100 px-4 py-2">
                <p className="text-xs text-slate-500 uppercase font-semibold">Tipo de usuario</p>
                <p className="text-sm font-semibold text-slate-800 mt-0.5">{user?.role}</p>
              </div>
            </div>
            <button
              type="button"
              onClick={() => navigate('/portal/profile/edit')}
              className="w-full mt-4 py-2 rounded-lg border border-slate-300 bg-white hover:bg-slate-50 text-slate-700 text-sm font-semibold transition"
            >
              Editar perfil
            </button>
          </article>

          <article className="rounded-xl border border-blue-200 bg-white shadow-sm p-5">
            <h3 className="font-semibold text-slate-900 mb-4 flex items-center gap-2">
              <span className="inline-flex items-center justify-center h-8 w-8 rounded-lg bg-gradient-to-br from-amber-500 to-amber-600 text-white text-sm"></span>
              ¿Necesitas ayuda?
            </h3>
            <div className="space-y-2">
              <button className="w-full text-left rounded-lg border border-blue-100 bg-blue-50 hover:bg-blue-100 px-4 py-3 transition">
                <p className="text-sm font-semibold text-slate-800">Contactar soporte</p>
                <p className="text-xs text-slate-500 mt-0.5">Disponible 24/7</p>
              </button>
              <button className="w-full text-left rounded-lg border border-blue-100 bg-blue-50 hover:bg-blue-100 px-4 py-3 transition">
                <p className="text-sm font-semibold text-slate-800">Ver preguntas frecuentes</p>
                <p className="text-xs text-slate-500 mt-0.5">FAQs del sistema</p>
              </button>
              <button className="w-full text-left rounded-lg border border-blue-100 bg-blue-50 hover:bg-blue-100 px-4 py-3 transition">
                <p className="text-sm font-semibold text-slate-800">Solicitar nueva cita</p>
                <p className="text-xs text-slate-500 mt-0.5">Agendar con un especialista</p>
              </button>
            </div>
          </article>
        </section>
      </main>
    </div>
  )
}

export default UserPortal
