import { useNavigate } from 'react-router-dom'
import { getDefaultRouteForRole, isHospitalStaffRole, useAuth } from '@/context/AuthContext'
import { useState } from 'react'
import { authAPI } from '@/services/api'

type HeaderProps = {
  inverted?: boolean
}

function Header({ inverted = false }: HeaderProps) {
  const navigate = useNavigate()
  const { isAuthenticated, user, logout } = useAuth()
  const [isMenuOpen, setIsMenuOpen] = useState(false)

  const headerClass = inverted ? 'bg-blue-900/95 border-b border-blue-700 shadow-lg' : 'bg-white shadow-sm'
  const menuClass = inverted ? 'md:hidden bg-blue-900 border-t border-blue-700 px-4 py-4 space-y-2' : 'md:hidden bg-gray-50 border-t px-4 py-4 space-y-2'
  const logoTitleClass = inverted ? 'text-xl font-bold text-white' : 'text-xl font-bold text-gray-800'
  const logoSubClass = inverted ? 'text-xs text-blue-200' : 'text-xs text-gray-500'
  const userLabelClass = inverted ? 'text-blue-100' : 'text-gray-700'
  const userEmailClass = inverted ? 'font-semibold text-white' : 'font-semibold text-gray-900'
  const primaryBtnClass = inverted
    ? 'px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-400 transition duration-200 font-semibold'
    : 'btn-primary'
  const secondaryBtnClass = inverted
    ? 'px-6 py-2 bg-blue-800 text-white rounded-lg hover:bg-blue-700 border border-blue-600 transition duration-200 font-semibold'
    : 'btn-secondary'
  const staffAccessClass = inverted
    ? 'text-blue-100 hover:text-white transition'
    : 'text-gray-700 hover:text-blue-600 transition'

  const handleLogout = async () => {
    try {
      await authAPI.logout()
    } catch {
      // Si el token ya expiró/revocó, igual se limpia sesión local.
    } finally {
      logout()
      navigate('/')
    }
  }

  return (
    <header className={`${headerClass} sticky top-0 z-50`}>
      <nav className="container-main flex justify-between items-center py-4">
        {/* Logo */}
        <div
          onClick={() => navigate('/')}
          className="flex items-center gap-2 cursor-pointer group"
        >
          <div className={`text-3xl font-bold ${inverted ? 'text-blue-200' : 'text-blue-600'}`}>🏥</div>
          <div>
            <h1 className={logoTitleClass}>HIS</h1>
            <p className={logoSubClass}>Hospital Info System</p>
          </div>
        </div>

        {/* Desktop Navigation */}
        <div className="hidden md:flex items-center gap-8">
          {!isAuthenticated ? (
            <>
              <button
                onClick={() => navigate('/login')}
                className={primaryBtnClass}
              >
                Iniciar Sesion
              </button>
              <button
                onClick={() => navigate('/login/personal')}
                className={staffAccessClass}
              >
                Acceso Personal
              </button>
            </>
          ) : (
            <>
              <div className="text-sm">
                <p className={userLabelClass}>Bienvenido,</p>
                <p className={userEmailClass}>{user?.email}</p>
              </div>
              <button
                onClick={() => navigate(getDefaultRouteForRole(user?.role))}
                className={primaryBtnClass}
              >
                {isHospitalStaffRole(user?.role) ? 'Dashboard' : 'Mi Portal'}
              </button>
              <button
                onClick={() => void handleLogout()}
                className={secondaryBtnClass}
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
        <div className={menuClass}>
          {!isAuthenticated ? (
            <>
              <button
                onClick={() => {
                  navigate('/login')
                  setIsMenuOpen(false)
                }}
                className={`block w-full text-center ${primaryBtnClass}`}
              >
                Iniciar Sesion
              </button>
              <button
                onClick={() => {
                  navigate('/login/personal')
                  setIsMenuOpen(false)
                }}
                className={`block w-full text-left py-2 ${inverted ? 'text-blue-100 hover:text-white' : 'text-gray-700 hover:text-blue-600'}`}
              >
                Acceso Personal
              </button>
            </>
          ) : (
            <>
              <div className="py-2">
                <p className={`${userLabelClass} text-sm`}>Bienvenido,</p>
                <p className={userEmailClass}>{user?.email}</p>
              </div>
              <button
                onClick={() => {
                  navigate(getDefaultRouteForRole(user?.role))
                  setIsMenuOpen(false)
                }}
                className={`block w-full text-center ${primaryBtnClass}`}
              >
                {isHospitalStaffRole(user?.role) ? 'Dashboard' : 'Mi Portal'}
              </button>
              <button
                onClick={() => {
                  void handleLogout()
                  setIsMenuOpen(false)
                }}
                className={`block w-full text-center ${secondaryBtnClass}`}
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

