import React from 'react'

const STORAGE_PREFIX = 'his:sidebar:'

const resolveInitialValue = (storageKey: string, defaultValue: boolean): boolean => {
  if (typeof window === 'undefined') {
    return defaultValue
  }

  const storedValue = window.sessionStorage.getItem(storageKey)
  if (storedValue === null) {
    return defaultValue
  }

  return storedValue === 'collapsed'
}

const useSidebarPreference = (viewKey: string, defaultCollapsed: boolean) => {
  const storageKey = `${STORAGE_PREFIX}${viewKey}`

  const [collapsed, setCollapsed] = React.useState<boolean>(() => resolveInitialValue(storageKey, defaultCollapsed))

  React.useEffect(() => {
    window.sessionStorage.setItem(storageKey, collapsed ? 'collapsed' : 'expanded')
  }, [collapsed, storageKey])

  const toggleCollapsed = React.useCallback(() => {
    setCollapsed((prev) => !prev)
  }, [])

  return { collapsed, toggleCollapsed }
}

export default useSidebarPreference

