import React from 'react'
import SidebarNavButton from '@/components/ui/SidebarNavButton'

type AdminSidebarProps = {
  email?: string
  role?: string
  loading: boolean
  activeSection?: 'dashboard' | 'triage' | 'triage-list' | 'users'
  collapsed?: boolean
  showSessionDetails?: boolean
  onToggleCollapse: () => void
  onDashboard: () => void
  onTriage: () => void
  onUsers: () => void
  onTriageList: () => void
  onLogout: () => void
}

const AdminSidebar: React.FC<AdminSidebarProps> = ({
  email,
  role,
  loading,
  activeSection = 'dashboard',
  collapsed = false,
  showSessionDetails = false,
  onToggleCollapse,
  onDashboard,
  onTriage,
  onUsers,
  onTriageList,
  onLogout,
}) => {
  return (
    <aside className={`h-full shrink-0 relative z-20 bg-blue-100/85 border-r border-blue-200 shadow-sm p-4 flex flex-col justify-between overflow-hidden transition-all duration-300 ${collapsed ? 'w-20' : 'w-64'}`}>
      <div>
        <div className={`mb-7 ${collapsed ? 'flex flex-col items-center gap-3' : ''}`}>
          {!collapsed && (
            <>
              <p className="text-xs uppercase tracking-[0.2em] text-slate-400">HIS</p>
              <h1 className="text-xl font-bold text-slate-900 mt-1">Admin Panel</h1>
              <p className="text-xs text-slate-600 mt-1">Gestión hospitalaria</p>
            </>
          )}
          <button
            type="button"
            onClick={onToggleCollapse}
            className="px-2.5 py-1.5 rounded-lg bg-white hover:bg-slate-50 text-slate-700 border border-blue-200 font-semibold text-xs"
            title={collapsed ? 'Expandir menú' : 'Ocultar menú'}
          >
            {collapsed ? '>>' : '<<'}
          </button>
        </div>

        <nav className="space-y-2">
          <SidebarNavButton label="Dashboard" icon="🏠" collapsed={collapsed} active={activeSection === 'dashboard'} onClick={onDashboard} />
          <SidebarNavButton label="Registro y Triaje" icon="🩺" collapsed={collapsed} active={activeSection === 'triage'} onClick={onTriage} />
          <SidebarNavButton label="Usuarios" icon="👥" collapsed={collapsed} active={activeSection === 'users'} onClick={onUsers} />
          <SidebarNavButton label="Listar Triajes" icon="📋" collapsed={collapsed} active={activeSection === 'triage-list'} onClick={onTriageList} />
        </nav>
      </div>

      <div className="space-y-3">
        {!collapsed && showSessionDetails && (
          <div className="rounded-lg border border-blue-200 bg-blue-50/70 p-3">
            <p className="text-xs text-slate-500">Sesión actual</p>
            <p className="font-semibold text-slate-800 break-all">{email}</p>
            <p className="text-xs text-slate-500 mt-1">Rol: {role}</p>
          </div>
        )}
        <button
          type="button"
          onClick={onLogout}
          disabled={loading}
          className={`w-full px-4 py-2 rounded-lg bg-white hover:bg-slate-50 text-slate-700 border border-blue-200 font-semibold text-sm disabled:opacity-60 ${collapsed ? 'px-2 text-xs' : ''}`}
          title="Cerrar sesión"
        >
          {loading ? '...' : collapsed ? 'Salir' : 'Cerrar sesión'}
        </button>
      </div>
    </aside>
  )
}

export default AdminSidebar

