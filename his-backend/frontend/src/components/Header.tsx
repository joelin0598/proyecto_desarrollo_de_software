import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { useState } from 'react'

function Header() {
  const navigate = useNavigate()
  const location = useLocation()
  const { isAuthenticated, user, logout } = useAuth()
  const [isMenuOpen, setIsMenuOpen] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  const isActive = (path: string) => location.pathname === path ? 'text-blue-600 font-semibold' : 'text-gray-700 hover:text-blue-600'

  return (
    <header className="bg-white shadow-sm sticky top-0 z-50">
      <nav className="container-main flex justify-between items-center py-4">
        {/* Logo */}
        <div
          onClick={() => navigate('/')}
          className="flex items-center gap-2 cursor-pointer group"
        >
          <div className="text-3xl font-bold text-blue-600">🏥</div>
          <div>
            <h1 className="text-xl font-bold text-gray-800">HIS</h1>
            <p className="text-xs text-gray-500">Hospital Info System</p>
          </div>
        </div>

        {/* Desktop Navigation */}
        <div className="hidden md:flex items-center gap-8">
          {!isAuthenticated ? (
            <>
              <button
                onClick={() => navigate('/')}
                className={`transition ${isActive('/')}`}
              >
                Inicio
              </button>
              <button
                onClick={() => navigate('/login')}
                className="btn-primary"
              >
                Iniciar Sesión
              </button>
              <button
                onClick={() => navigate('/register')}
                className="btn-outline"
              >
                Registrarse
              </button>
            </>
          ) : (
            <>
              <div className="text-sm">
                <p className="text-gray-700">Bienvenido,</p>
                <p className="font-semibold text-gray-900">{user?.email}</p>
              </div>
              <button
                onClick={() => navigate(user?.role === 'ADMIN' ? '/admin' : '/user')}
                className="btn-primary"
              >
                {user?.role === 'ADMIN' ? 'Dashboard' : 'Mi Portal'}
              </button>
              <button
                onClick={handleLogout}
                className="btn-secondary"
              >
                Cerrar Sesión
              </button>
            </>
          )}
        </div>

        {/* Mobile Menu Button */}
        <div className="md:hidden">
          <button
            onClick={() => setIsMenuOpen(!isMenuOpen)}
            className="text-2xl"
          >
            ☰
          </button>
        </div>
      </nav>

      {/* Mobile Menu */}
      {isMenuOpen && (
        <div className="md:hidden bg-gray-50 border-t px-4 py-4 space-y-2">
          {!isAuthenticated ? (
            <>
              <button
                onClick={() => {
                  navigate('/')
                  setIsMenuOpen(false)
                }}
                className="block w-full text-left py-2 text-gray-700 hover:text-blue-600"
              >
                Inicio
              </button>
              <button
                onClick={() => {
                  navigate('/login')
                  setIsMenuOpen(false)
                }}
                className="block w-full btn-primary text-center"
              >
                Iniciar Sesión
              </button>
              <button
                onClick={() => {
                  navigate('/register')
                  setIsMenuOpen(false)
                }}
                className="block w-full btn-outline text-center"
              >
                Registrarse
              </button>
            </>
          ) : (
            <>
              <div className="py-2">
                <p className="text-gray-700 text-sm">Bienvenido,</p>
                <p className="font-semibold text-gray-900">{user?.email}</p>
              </div>
              <button
                onClick={() => {
                  navigate(user?.role === 'ADMIN' ? '/admin' : '/user')
                  setIsMenuOpen(false)
                }}
                className="block w-full btn-primary text-center"
              >
                {user?.role === 'ADMIN' ? 'Dashboard' : 'Mi Portal'}
              </button>
              <button
                onClick={() => {
                  handleLogout()
                  setIsMenuOpen(false)
                }}
                className="block w-full btn-secondary text-center"
              >
                Cerrar Sesión
              </button>
            </>
          )}
        </div>
      )}
    </header>
  )
}

export default Header

