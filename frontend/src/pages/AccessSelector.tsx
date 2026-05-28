import { useNavigate } from 'react-router-dom'
import StatusChip from '@/components/ui/StatusChip'

function AccessSelector() {
  const navigate = useNavigate()

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-100 via-sky-50 to-blue-100 text-slate-800 flex">
      <aside className="w-64 bg-blue-100/85 border-r border-blue-200 shadow-sm p-4 flex flex-col justify-between">
        <div>
          <div className="mb-7">
            <p className="text-xs uppercase tracking-[0.2em] text-slate-400">HIS</p>
            <h1 className="text-xl font-bold text-slate-900 mt-1">Acceso</h1>
            <p className="text-xs text-slate-600 mt-1">Ingreso al sistema</p>
          </div>

          <nav className="space-y-2">
            <button type="button" className="w-full text-left px-3 py-2 rounded-lg text-sm bg-white text-blue-700 border border-blue-200 font-semibold">
              Seleccion de perfil
            </button>
            <button type="button" onClick={() => navigate('/')} className="w-full text-left px-3 py-2 rounded-lg text-sm hover:bg-white/70 text-slate-700 transition">
              Volver al inicio
            </button>
          </nav>
        </div>

        <div className="rounded-lg border border-blue-200 bg-blue-50/70 p-3">
          <p className="text-xs text-slate-500">Estado</p>
          <p className="font-semibold text-slate-800">Publico</p>
        </div>
      </aside>

      <main className="flex-1 p-5 lg:p-6 flex items-center justify-center">
        <section className="w-full max-w-5xl bg-white rounded-xl shadow-md border border-gray-100 p-6 lg:p-8">
          <div className="flex items-start justify-between gap-4 mb-6">
            <div>
              <h2 className="text-2xl font-bold text-slate-900">Selecciona tu tipo de acceso</h2>
              <p className="text-sm text-slate-600 mt-1">Elige el perfil para continuar al portal correspondiente.</p>
            </div>
            <StatusChip label="Ingreso seguro" tone="blue" />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <article className="rounded-xl border border-blue-200 bg-blue-50 p-6 shadow-sm">
              <h3 className="text-xl font-semibold text-slate-900">Paciente</h3>
              <p className="text-sm text-slate-600 mt-2">Accede a citas, resultados, recetas y documentos clinicos.</p>
              <div className="mt-5 space-y-3">
                <button
                  onClick={() => navigate('/login/paciente')}
                  className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2.5 px-4 rounded-lg transition"
                >
                  Iniciar sesion como paciente
                </button>
                <button
                  onClick={() => navigate('/register')}
                  className="w-full border border-blue-200 bg-white text-blue-700 hover:bg-blue-100 font-semibold py-2.5 px-4 rounded-lg transition"
                >
                  Registrarme como paciente
                </button>
              </div>
            </article>

            <article className="rounded-xl border border-blue-200 bg-blue-50 p-6 shadow-sm">
              <h3 className="text-xl font-semibold text-slate-900">Personal hospitalario</h3>
              <p className="text-sm text-slate-600 mt-2">Usa tu cuenta institucional para procesos administrativos y clinicos.</p>
              <div className="mt-5">
                <button
                  onClick={() => navigate('/login/personal')}
                  className="w-full bg-slate-700 hover:bg-slate-800 text-white font-semibold py-2.5 px-4 rounded-lg transition"
                >
                  Iniciar sesion como personal
                </button>
              </div>
            </article>
          </div>
        </section>
      </main>
    </div>
  )
}

export default AccessSelector

