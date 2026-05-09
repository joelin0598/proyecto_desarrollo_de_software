import React from 'react'
import SidebarNavButton from '@/components/ui/SidebarNavButton'

type AdminSidebarProps = {
  email?: string
  role?: string
  loading: boolean
  onDashboard: () => void
  onTriage: () => void
  onLogout: () => void
}

const AdminSidebar: React.FC<AdminSidebarProps> = ({
  email,
  role,
  loading,
  onDashboard,
  onTriage,
  onLogout,
}) => {
  return (
    <aside className="w-64 bg-blue-100/85 border-r border-blue-200 shadow-sm p-4 flex flex-col justify-between">
      <div>
        <div className="mb-7">
          <p className="text-xs uppercase tracking-[0.2em] text-slate-400">HIS</p>
          <h1 className="text-xl font-bold text-slate-900 mt-1">Admin Panel</h1>
          <p className="text-xs text-slate-600 mt-1">Gestión hospitalaria</p>
        </div>

        <nav className="space-y-2">
          <SidebarNavButton label="Dashboard" active onClick={onDashboard} />
          <SidebarNavButton label="Registro y Triaje" onClick={onTriage} />
        </nav>
      </div>

      <div className="space-y-3">
        <div className="rounded-lg border border-blue-200 bg-blue-50/70 p-3">
          <p className="text-xs text-slate-500">Sesión actual</p>
          <p className="font-semibold text-slate-800 break-all">{email}</p>
          <p className="text-xs text-slate-500 mt-1">Rol: {role}</p>
        </div>
        <button
          onClick={onLogout}
          disabled={loading}
          className="w-full px-4 py-2 rounded-lg bg-white hover:bg-slate-50 text-slate-700 border border-blue-200 font-semibold text-sm disabled:opacity-60"
        >
          {loading ? 'Cerrando...' : 'Cerrar sesión'}
        </button>
      </div>
    </aside>
  )
}

export default AdminSidebar

