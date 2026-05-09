import React from 'react'

type SidebarNavButtonProps = {
  label: string
  active?: boolean
  onClick: () => void
}

const SidebarNavButton: React.FC<SidebarNavButtonProps> = ({ label, active = false, onClick }) => {
  return (
    <button
      onClick={onClick}
      className={`w-full text-left px-3 py-2 rounded-lg text-sm transition ${
        active
          ? 'bg-white text-blue-700 border border-blue-200 font-semibold'
          : 'hover:bg-white/70 text-slate-700'
      }`}
    >
      {label}
    </button>
  )
}

export default SidebarNavButton

