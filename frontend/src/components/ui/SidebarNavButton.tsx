import React from 'react'

type SidebarNavButtonProps = {
  label: string
  active?: boolean
  icon?: string
  collapsed?: boolean
  onClick: () => void
}

const SidebarNavButton: React.FC<SidebarNavButtonProps> = ({
  label,
  active = false,
  icon,
  collapsed = false,
  onClick,
}) => {
  const iconLabel = icon?.trim() || '•'

  return (
    <button
      type="button"
      title={label}
      aria-label={label}
      onClick={onClick}
      className={`w-full px-3 py-2 rounded-lg text-sm transition flex items-center ${
        collapsed ? 'justify-center' : 'justify-start gap-2'
      } ${
        active
          ? 'bg-white text-blue-700 border border-blue-200 font-semibold'
          : 'hover:bg-white/70 text-slate-700'
      }`}
    >
      <span className="text-base leading-none">{iconLabel}</span>
      {!collapsed && <span>{label}</span>}
    </button>
  )
}

export default SidebarNavButton

