import React from 'react'

type StatusChipProps = {
  label: string
  tone?: 'amber' | 'blue' | 'emerald' | 'ghost' | 'orange' | 'red' | 'slate' | 'yellow'
}

const toneClasses: Record<NonNullable<StatusChipProps['tone']>, string> = {
  amber: 'bg-amber-100 text-amber-700 border-amber-200',
  blue: 'bg-blue-100 text-blue-700 border-blue-200',
  emerald: 'bg-emerald-100 text-emerald-700 border-emerald-200',
  ghost: 'bg-white text-slate-500 border-slate-200 shadow-sm',
  orange: 'bg-orange-100 text-orange-700 border-orange-200',
  red: 'bg-red-100 text-red-700 border-red-200',
  slate: 'bg-slate-100 text-slate-700 border-slate-200',
  yellow: 'bg-yellow-100 text-yellow-800 border-yellow-300',
}

const StatusChip: React.FC<StatusChipProps> = ({ label, tone = 'slate' }) => {
  return (
    <span className={`text-xs font-semibold px-3 py-1.5 rounded-full border ${toneClasses[tone]}`}>
      {label}
    </span>
  )
}

export default StatusChip


