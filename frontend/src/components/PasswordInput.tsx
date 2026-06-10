import React, { useState } from 'react'
import PasswordToggle from '@/components/PasswordToggle'

type Props = React.InputHTMLAttributes<HTMLInputElement> & {
  label?: string
}

const PasswordInput: React.FC<Props> = ({ label, className = '', ...rest }) => {
  const [show, setShow] = useState(false)

  return (
    <div className="relative">
      {label && <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>}
      <input
        {...rest}
        type={show ? 'text' : 'password'}
        className={`w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 ${className}`}
      />

      <PasswordToggle
        visible={show}
        onToggle={() => setShow(s => !s)}
        className="absolute right-3 top-9"
      />
    </div>
  )
}

export default PasswordInput




