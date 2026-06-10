import { useNavigate } from 'react-router-dom'
import StatusChip from '@/components/ui/StatusChip'

function LandingPage() {
  const navigate = useNavigate()

  const services = [
    {
      id: 1,
      title: 'Agendar Cita',
      description: 'Reserva tu consulta con nuestros especialistas disponibles',
      icon: '📅',
      color: 'bg-blue-100'
    },
    {
      id: 2,
      title: 'Laboratorio',
      description: 'Solicita exámenes clínicos y consulta resultados',
      icon: '🧪',
      color: 'bg-green-100'
    },
    {
      id: 3,
      title: 'Recetas y Medicamentos',
      description: 'Visualiza tus recetas y controla tus medicamentos',
      icon: '💊',
      color: 'bg-purple-100'
    },
    {
      id: 4,
      title: 'Historial Médico',
      description: 'Accede a tu historial clínico y resultados previos',
      icon: '📋',
      color: 'bg-orange-100'
    },
    {
      id: 5,
      title: 'Pago en Línea',
      description: 'Consulta tu estado de cuenta y realiza pagos',
      icon: '💳',
      color: 'bg-red-100'
    },
    {
      id: 6,
      title: 'Controles Médicos',
      description: 'Monitorea tus signos vitales y controles de salud',
      icon: '❤️',
      color: 'bg-pink-100'
    }
  ]

  const highlights = [
    { label: 'Disponibilidad', value: '24/7', detail: 'Soporte y acceso continuo a servicios digitales.' },
    { label: 'Seguridad', value: 'Alta', detail: 'Datos protegidos con controles de seguridad hospitalaria.' },
    { label: 'Accesibilidad', value: 'Multi-dispositivo', detail: 'Experiencia responsive para escritorio y movil.' },
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-100 via-sky-50 to-blue-100 text-slate-800 flex">
      <aside className="w-64 bg-blue-100/85 border-r border-blue-200 shadow-sm p-4 flex flex-col justify-between">
        <div>
          <div className="mb-7 flex items-center gap-3">
            <img src="/hospital-logo.svg" alt="Hospital" className="h-10 w-10 object-contain" />
            <div>
              <p className="text-xs uppercase tracking-[0.2em] text-slate-400">HIS</p>
              <h1 className="text-xl font-bold text-slate-900 mt-1">Portal Clinico</h1>
              <p className="text-xs text-slate-600 mt-1">Bienvenida institucional</p>
            </div>
          </div>
          <nav className="space-y-2 text-sm">
            <button type="button" className="w-full text-left px-3 py-2 rounded-lg bg-white text-blue-700 border border-blue-200 font-semibold">Inicio</button>
            <button type="button" onClick={() => navigate('/login')} className="w-full text-left px-3 py-2 rounded-lg hover:bg-white/70 text-slate-700 transition">Acceder al sistema</button>
            <button type="button" onClick={() => navigate('/register')} className="w-full text-left px-3 py-2 rounded-lg hover:bg-white/70 text-slate-700 transition">Registro de paciente</button>
          </nav>
        </div>

        <div className="rounded-lg border border-blue-200 bg-blue-50/70 p-3">
          <p className="text-xs text-slate-500">Plataforma</p>
          <p className="font-semibold text-slate-800">Sistema de informacion hospitalario</p>
        </div>
      </aside>

      <main className="flex-1 p-5 lg:p-6 overflow-auto">
        <section className="rounded-xl border border-blue-200 bg-white shadow-sm p-6 lg:p-7 mb-5">
          <div className="flex flex-col md:flex-row md:items-start md:justify-between gap-4">
            <div>
              <h2 className="text-3xl font-bold text-slate-900">Bienvenido al Sistema de Informacion Hospitalario</h2>
              <p className="text-slate-600 mt-2 max-w-3xl">Plataforma digital para gestionar salud, consultas y seguimiento clinico de forma segura, trazable y remota.</p>
            </div>
            <StatusChip label="Portal publico" tone="blue" />
          </div>

          <div className="mt-5 flex flex-wrap gap-3">
            <button onClick={() => navigate('/login/paciente')} className="px-4 py-2.5 rounded-lg bg-blue-600 hover:bg-blue-700 text-white font-semibold transition">
              Iniciar sesion paciente
            </button>
            <button onClick={() => navigate('/register')} className="px-4 py-2.5 rounded-lg border border-blue-200 bg-blue-50 hover:bg-blue-100 text-blue-700 font-semibold transition">
              Registrarse
            </button>
            <button onClick={() => navigate('/login/personal')} className="px-4 py-2.5 rounded-lg border border-slate-300 bg-white hover:bg-slate-50 text-slate-700 font-semibold transition">
              Acceso personal
            </button>
          </div>
        </section>

        <section className="mb-5">
          <h3 className="text-xl font-bold text-slate-900 mb-3">Servicios disponibles</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            {services.map((service) => (
              <article key={service.id} className="rounded-xl border border-blue-200 bg-blue-50 p-4 shadow-sm hover:shadow-md hover:bg-white transition">
                <div className={`${service.color} w-12 h-12 rounded-lg flex items-center justify-center text-2xl mb-3`}>
                  {service.icon}
                </div>
                <h4 className="text-base font-semibold text-slate-900">{service.title}</h4>
                <p className="text-sm text-slate-600 mt-1">{service.description}</p>
                <button onClick={() => navigate('/login/paciente')} className="mt-3 text-sm text-blue-700 hover:text-blue-800 font-semibold">
                  Acceder como paciente
                </button>
              </article>
            ))}
          </div>
        </section>

        <section>
          <h3 className="text-xl font-bold text-slate-900 mb-3">Indicadores de plataforma</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {highlights.map((item) => (
              <article key={item.label} className="rounded-xl border border-blue-200 bg-white p-4 shadow-sm">
                <p className="text-xs uppercase tracking-wide text-slate-500">{item.label}</p>
                <p className="text-2xl font-bold text-blue-700 mt-1">{item.value}</p>
                <p className="text-sm text-slate-600 mt-2">{item.detail}</p>
              </article>
            ))}
          </div>
        </section>
      </main>
    </div>
  )
}

export default LandingPage

