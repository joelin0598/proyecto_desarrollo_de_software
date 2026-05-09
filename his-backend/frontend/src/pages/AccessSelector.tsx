import { useNavigate } from 'react-router-dom'
import Header from '@/components/Header'

function AccessSelector() {
  const navigate = useNavigate()

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      <Header />

      <div className="flex-1 flex items-center justify-center px-4 py-8">
        <div className="w-full max-w-4xl">
          <h1 className="text-3xl font-bold text-center text-gray-800 mb-2">
            Selecciona tu tipo de acceso
          </h1>
          <p className="text-center text-gray-600 mb-8">
            Elige la opcion correcta para iniciar sesion en el sistema
          </p>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="bg-white rounded-lg shadow-2xl p-8 border-t-4 border-blue-600">
              <h2 className="text-2xl font-bold text-gray-800 mb-2">Paciente</h2>
              <p className="text-gray-600 mb-6">
                Accede a tu portal para citas, resultados, recetas y documentos.
              </p>
              <button
                onClick={() => navigate('/login/paciente')}
                className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-lg transition"
              >
                Iniciar sesion como paciente
              </button>
              <button
                onClick={() => navigate('/register')}
                className="w-full mt-3 border border-blue-600 text-blue-600 hover:bg-blue-50 font-semibold py-2 px-4 rounded-lg transition"
              >
                Registrarme como paciente
              </button>
            </div>

            <div className="bg-white rounded-lg shadow-2xl p-8 border-t-4 border-gray-700">
              <h2 className="text-2xl font-bold text-gray-800 mb-2">Personal hospitalario</h2>
              <p className="text-gray-600 mb-6">
                Accede con tu cuenta institucional para operar procesos internos.
              </p>
              <button
                onClick={() => navigate('/login/personal')}
                className="w-full bg-gray-700 hover:bg-gray-800 text-white font-bold py-2 px-4 rounded-lg transition"
              >
                Iniciar sesion como personal
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default AccessSelector

