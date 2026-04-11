import { useNavigate } from 'react-router-dom'
import Header from '@/components/Header'
import Footer from '@/components/Footer'

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

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <Header />

      {/* Hero Section */}
      <section className="bg-gradient-to-r from-blue-600 to-blue-800 text-white py-20">
        <div className="container-main">
          <div className="text-center">
            <h1 className="text-5xl font-bold mb-4">
              Bienvenido al Sistema de Información Hospitalario
            </h1>
            <p className="text-xl text-blue-100 mb-8">
              Plataforma digital para gestionar tu salud de forma segura y remota
            </p>
            <div className="flex justify-center gap-4">
              <button
                onClick={() => navigate('/login')}
                className="btn-primary bg-white text-blue-600 hover:bg-gray-100"
              >
                Iniciar Sesión
              </button>
              <button
                onClick={() => navigate('/register')}
                className="btn-outline border-white text-white hover:bg-blue-700"
              >
                Registrarse
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* Services Section */}
      <section className="py-20">
        <div className="container-main">
          <h2 className="text-4xl font-bold text-center mb-12 text-gray-800">
            Servicios Disponibles
          </h2>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {services.map((service) => (
              <div key={service.id} className="service-card">
                <div className={`${service.color} w-16 h-16 rounded-lg flex items-center justify-center text-3xl mb-4`}>
                  {service.icon}
                </div>
                <h3 className="card-title">{service.title}</h3>
                <p className="card-description">{service.description}</p>
                <button
                  onClick={() => navigate('/login')}
                  className="mt-4 text-blue-600 hover:text-blue-800 font-semibold text-sm"
                >
                  Acceder →
                </button>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Info Section */}
      <section className="bg-white py-20 border-t">
        <div className="container-main">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="text-4xl font-bold text-blue-600 mb-2">24/7</div>
              <h3 className="text-xl font-semibold mb-2">Disponibilidad</h3>
              <p className="text-gray-600">Acceso a nuestros servicios disponibles las 24 horas</p>
            </div>
            <div className="text-center">
              <div className="text-4xl font-bold text-blue-600 mb-2">🔒</div>
              <h3 className="text-xl font-semibold mb-2">Seguridad</h3>
              <p className="text-gray-600">Tus datos están protegidos con encriptación de alta seguridad</p>
            </div>
            <div className="text-center">
              <div className="text-4xl font-bold text-blue-600 mb-2">📱</div>
              <h3 className="text-xl font-semibold mb-2">Accesibilidad</h3>
              <p className="text-gray-600">Diseñado para funcionar en todos tus dispositivos</p>
            </div>
          </div>
        </div>
      </section>

      {/* Catalog Section */}
      <section className="py-20">
        <div className="container-main">
          <h2 className="text-4xl font-bold text-center mb-12 text-gray-800">
            Especialidades y Médicos
          </h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="service-card">
              <h3 className="card-title">Medicina General</h3>
              <p className="card-description mb-4">Consultas de medicina general con nuestros especialistas</p>
              <div className="text-sm text-gray-700 space-y-2">
                <p>📍 Disponibilidad: Lunes - Viernes</p>
                <p>⏰ Horario: 8:00 AM - 5:00 PM</p>
              </div>
            </div>

            <div className="service-card">
              <h3 className="card-title">Cardiología</h3>
              <p className="card-description mb-4">Especialista en enfermedades del corazón y sistema circulatorio</p>
              <div className="text-sm text-gray-700 space-y-2">
                <p>📍 Disponibilidad: Lunes - Jueves</p>
                <p>⏰ Horario: 9:00 AM - 4:00 PM</p>
              </div>
            </div>

            <div className="service-card">
              <h3 className="card-title">Pediatría</h3>
              <p className="card-description mb-4">Atención especializada para niños y adolescentes</p>
              <div className="text-sm text-gray-700 space-y-2">
                <p>📍 Disponibilidad: Lunes - Viernes</p>
                <p>⏰ Horario: 7:00 AM - 3:00 PM</p>
              </div>
            </div>

            <div className="service-card">
              <h3 className="card-title">Odontología</h3>
              <p className="card-description mb-4">Servicios dentales completos y preventivos</p>
              <div className="text-sm text-gray-700 space-y-2">
                <p>📍 Disponibilidad: Martes - Sábado</p>
                <p>⏰ Horario: 8:00 AM - 6:00 PM</p>
              </div>
            </div>
          </div>

          <div className="text-center mt-12">
            <button
              onClick={() => navigate('/login')}
              className="btn-primary"
            >
              Ver todos los médicos
            </button>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  )
}

export default LandingPage

