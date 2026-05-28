import React from 'react'

export type UseCaseModule = {
  title: string
  subtitle: string
  detail: string
  route: string | null
  enabled: boolean
  accent: string
  icon: string
}

type UseCaseModuleCardProps = {
  module: UseCaseModule
  onClick: (route: string | null) => void
}

const UseCaseModuleCard: React.FC<UseCaseModuleCardProps> = ({ module, onClick }) => {
  return (
    <button
      onClick={() => onClick(module.route)}
      disabled={!module.enabled}
      className={`text-left rounded-xl border border-blue-200 bg-blue-50 p-4 shadow-sm h-28 transition ${
        module.enabled ? 'hover:shadow-md hover:bg-white' : 'opacity-70 cursor-not-allowed'
      }`}
    >
      <div className="flex items-start justify-between h-full">
        <div>
          <p className="text-slate-900 font-semibold text-base leading-tight">{module.title}</p>
          <p className="text-blue-700 text-xs font-bold mt-1">{module.subtitle}</p>
          <p className="text-slate-500 text-xs mt-1.5">{module.detail}</p>
        </div>
        <span className={`inline-flex items-center justify-center h-10 w-10 rounded-lg text-lg bg-gradient-to-br ${module.accent} text-white`}>
          {module.icon}
        </span>
      </div>
    </button>
  )
}

export default UseCaseModuleCard

