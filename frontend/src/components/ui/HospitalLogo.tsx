import React from 'react'

type HospitalLogoProps = {
  className?: string
  alt?: string
}

const HospitalLogo: React.FC<HospitalLogoProps> = ({
  className = 'h-10 w-10',
  alt = 'Hospital',
}) => {
  const [hasError, setHasError] = React.useState(false)

  if (hasError) {
    return (
      <span
        aria-hidden="true"
        className={`${className} inline-flex items-center justify-center rounded-lg bg-blue-600 text-white font-bold text-sm`}
      >
        H
      </span>
    )
  }

  return (
    <img
      src="/hospital-logo.svg"
      alt={alt}
      className={`${className} object-contain`}
      onError={() => setHasError(true)}
      draggable={false}
    />
  )
}

export default HospitalLogo
