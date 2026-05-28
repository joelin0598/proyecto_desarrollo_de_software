import React from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import AdminSidebar from '@/components/ui/AdminSidebar'
import StatusChip from '@/components/ui/StatusChip'
import useSidebarPreference from '@/hooks/useSidebarPreference'
import { useAuth } from '@/context/AuthContext'
import {
  authAPI,
  catalogAPI,
  HOSPITAL_STAFF_ROLES,
  type CareUnitOption,
  type UserMaintenanceCreateRequest,
  type UserMaintenanceResponse,
  type SpecialtyOption,
  type UserMaintenanceUpdateRequest,
  type UserRole,
  userMaintenanceAPI,
} from '@/services/api'

type FeedbackState = { kind: 'success' | 'error'; message: string } | null

type CreateFormState = {
  nombreCompleto: string
  email: string
  password: string
  direccion: string
  telefonoCorporativo: string
  especialidadId: string
  unidadAtencionId: string
  rol: UserRole
  numeroColegiado: string
}

type EditFormState = {
  nombreCompleto: string
  direccion: string
  telefonoCorporativo: string
  especialidadId: string
  unidadAtencionId: string
  rol: UserRole | ''
  numeroColegiado: string
}

const emptyCreateForm = (): CreateFormState => ({
  nombreCompleto: '',
  email: '',
  password: '',
  direccion: '',
  telefonoCorporativo: '',
  especialidadId: '',
  unidadAtencionId: '',
  rol: 'ADMINISTRATIVO',
  numeroColegiado: '',
})

const emptyEditForm = (user?: UserMaintenanceResponse): EditFormState => ({
  nombreCompleto: user?.nombreCompleto ?? '',
  direccion: user?.direccion ?? '',
  telefonoCorporativo: user?.telefonoCorporativo ?? '',
  especialidadId: user?.especialidadId?.toString() ?? '',
  unidadAtencionId: user?.unidadAtencionId?.toString() ?? '',
  rol: user?.role ?? '',
  numeroColegiado: user?.numeroColegiado ?? '',
})

const parseOptionalNumber = (value: string): number | undefined => {
  const trimmed = value.trim()
  if (!trimmed) return undefined
  const parsed = Number(trimmed)
  return Number.isNaN(parsed) ? undefined : parsed
}

const buildPatchPayload = (
  original: UserMaintenanceResponse,
  draft: EditFormState,
): { payload: UserMaintenanceUpdateRequest; validationError?: string } => {
  const payload: UserMaintenanceUpdateRequest = {}

  const nombreCompleto = draft.nombreCompleto.trim()
  if (nombreCompleto !== (original.nombreCompleto ?? '')) {
    if (!nombreCompleto) return { payload, validationError: 'El nombre completo no puede quedar vacío.' }
    payload.nombreCompleto = nombreCompleto
  }

  const direccion = draft.direccion.trim()
  if (direccion !== (original.direccion ?? '')) {
    if (!direccion) return { payload, validationError: 'La dirección no puede quedar vacía.' }
    payload.direccion = direccion
  }

  const telefonoCorporativo = draft.telefonoCorporativo.trim()
  if (telefonoCorporativo !== (original.telefonoCorporativo ?? '')) {
    if (!telefonoCorporativo) return { payload, validationError: 'El teléfono corporativo no puede quedar vacío.' }
    payload.telefonoCorporativo = telefonoCorporativo
  }

  const especialidadId = parseOptionalNumber(draft.especialidadId)
  if (especialidadId !== (original.especialidadId ?? undefined)) {
    payload.especialidadId = especialidadId
  }

  const unidadAtencionId = parseOptionalNumber(draft.unidadAtencionId)
  if (unidadAtencionId !== (original.unidadAtencionId ?? undefined)) {
    payload.unidadAtencionId = unidadAtencionId
  }

  if (draft.rol && draft.rol !== original.role) {
    payload.rol = draft.rol
  }

  const numeroColegiado = draft.numeroColegiado.trim()
  if (numeroColegiado !== (original.numeroColegiado ?? '')) {
    payload.numeroColegiado = numeroColegiado
  }

  return { payload }
}

const getErrorMessage = (error: unknown, fallback: string) =>
  (error as { response?: { data?: { errorMessage?: string } } })?.response?.data?.errorMessage ||
  (error as Error)?.message ||
  fallback

const UserMaintenance: React.FC = () => {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const { user, logout } = useAuth()
  const { collapsed: sidebarCollapsed, toggleCollapsed } = useSidebarPreference('admin-shell', false)
  const [loadingLogout, setLoadingLogout] = React.useState(false)
  const [loadingList, setLoadingList] = React.useState(true)
  const [refreshing, setRefreshing] = React.useState(false)
  const [creating, setCreating] = React.useState(false)
  const [savingEdit, setSavingEdit] = React.useState(false)
  const [processingUserId, setProcessingUserId] = React.useState<number | null>(null)
  const [users, setUsers] = React.useState<UserMaintenanceResponse[]>([])
  const [specialties, setSpecialties] = React.useState<SpecialtyOption[]>([])
  const [careUnits, setCareUnits] = React.useState<CareUnitOption[]>([])
  const [loadingCatalogs, setLoadingCatalogs] = React.useState(true)
  const [searchTerm, setSearchTerm] = React.useState('')
  const [roleFilter, setRoleFilter] = React.useState<'ALL' | UserRole>('ALL')
  const [statusFilter, setStatusFilter] = React.useState<'ALL' | 'ACTIVE' | 'SUSPENDED'>('ALL')
  const [feedback, setFeedback] = React.useState<FeedbackState>(null)
  const [showCreateModal, setShowCreateModal] = React.useState(false)
  const [createForm, setCreateForm] = React.useState<CreateFormState>(() => emptyCreateForm())
  const [editingUser, setEditingUser] = React.useState<UserMaintenanceResponse | null>(null)
  const [editForm, setEditForm] = React.useState<EditFormState>(() => emptyEditForm())

  const handleLogout = async () => {
    setLoadingLogout(true)
    try {
      await authAPI.logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      logout()
      navigate('/')
      setLoadingLogout(false)
    }
  }

  const syncCreateMode = React.useCallback(() => {
    const shouldOpen = searchParams.get('create') === '1'
    setShowCreateModal(shouldOpen)
  }, [searchParams])

  React.useEffect(() => {
    syncCreateMode()
  }, [syncCreateMode])

  const loadUsers = React.useCallback(async (showRefreshing = false) => {
    if (showRefreshing) {
      setRefreshing(true)
    } else {
      setLoadingList(true)
    }

    try {
      const response = await userMaintenanceAPI.list()
      setUsers(response.data)
    } catch (error) {
      setFeedback({ kind: 'error', message: getErrorMessage(error, 'No se pudo cargar el listado de usuarios.') })
    } finally {
      setLoadingList(false)
      setRefreshing(false)
    }
  }, [])

  const loadCatalogs = React.useCallback(async () => {
    setLoadingCatalogs(true)
    try {
      const [specialtiesRes, careUnitsRes] = await Promise.all([
        catalogAPI.specialties(),
        catalogAPI.careUnits(),
      ])
      setSpecialties(specialtiesRes.data)
      setCareUnits(careUnitsRes.data)
    } catch (error) {
      console.error('No se pudieron cargar los catalogos para mantenimiento de usuarios:', error)
      setFeedback({ kind: 'error', message: 'No se pudieron cargar los catálogos de especialidades y unidades.' })
    } finally {
      setLoadingCatalogs(false)
    }
  }, [])

  React.useEffect(() => {
    void loadUsers()
  }, [loadUsers])

  React.useEffect(() => {
    void loadCatalogs()
  }, [loadCatalogs])

  const filteredUsers = React.useMemo(() => {
    const normalized = searchTerm.trim().toLowerCase()
    return users.filter((item) => {
      const matchesSearch = !normalized ||
        [item.nombreCompleto, item.email, item.numeroColegiado, item.role, item.telefonoCorporativo, item.direccion]
          .filter(Boolean)
          .join(' ')
          .toLowerCase()
          .includes(normalized)

      const matchesRole = roleFilter === 'ALL' || item.role === roleFilter
      const matchesStatus = statusFilter === 'ALL'
        || (statusFilter === 'ACTIVE' && item.active)
        || (statusFilter === 'SUSPENDED' && !item.active)

      return matchesSearch && matchesRole && matchesStatus
    })
  }, [searchTerm, users, roleFilter, statusFilter])

  const usersCount = users.length
  const activeCount = users.filter((item) => item.active).length
  const suspendedCount = users.filter((item) => !item.active).length
  const adminCount = users.filter((item) => item.role === 'ADMIN').length

  const specialtyById = React.useMemo(
    () => new Map(specialties.map((item) => [item.id, item])),
    [specialties],
  )

  const careUnitById = React.useMemo(
    () => new Map(careUnits.map((item) => [item.id, item])),
    [careUnits],
  )

  const filteredSummary = `${filteredUsers.length} de ${users.length}`

  const openCreateModal = () => {
    setShowCreateModal(true)
    setSearchParams({ create: '1' }, { replace: true })
  }

  const closeCreateModal = () => {
    setShowCreateModal(false)
    setCreateForm(emptyCreateForm())
    setSearchParams({}, { replace: true })
  }

  const handleCreateChange = (event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = event.target
    let sanitized = value

    if (name === 'nombreCompleto') {
      // Solo letras, espacios, acentos y apóstrofes — sin números
      sanitized = value.replace(/[0-9]/g, '')
    } else if (name === 'telefonoCorporativo') {
      // Solo dígitos, máximo 15 (backend acepta 8-15)
      sanitized = value.replace(/\D/g, '').slice(0, 15)
    } else if (name === 'numeroColegiado') {
      // Máximo 20 caracteres (backend @Size(max=20))
      sanitized = value.slice(0, 20)
    }

    setCreateForm((prev) => ({ ...prev, [name]: sanitized }))
  }

  const handleEditChange = (event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = event.target
    setEditForm((prev) => ({ ...prev, [name]: value }))
  }

  const handleCreateSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setCreating(true)
    setFeedback(null)

    try {
      const payload: UserMaintenanceCreateRequest = {
        nombreCompleto: createForm.nombreCompleto.trim(),
        email: createForm.email.trim(),
        password: createForm.password,
        direccion: createForm.direccion.trim(),
        telefonoCorporativo: createForm.telefonoCorporativo.trim(),
        especialidadId: parseOptionalNumber(createForm.especialidadId),
        unidadAtencionId: parseOptionalNumber(createForm.unidadAtencionId),
        rol: createForm.rol,
        numeroColegiado: createForm.numeroColegiado.trim() || undefined,
      }

      await userMaintenanceAPI.create(payload)
      setFeedback({ kind: 'success', message: 'Usuario de personal creado correctamente.' })
      setCreateForm(emptyCreateForm())
      closeCreateModal()
      await loadUsers(true)
    } catch (error) {
      setFeedback({ kind: 'error', message: getErrorMessage(error, 'No se pudo crear el usuario.') })
    } finally {
      setCreating(false)
    }
  }

  const openEditModal = (item: UserMaintenanceResponse) => {
    setEditingUser(item)
    setEditForm(emptyEditForm(item))
    setFeedback(null)
  }

  const closeEditModal = () => {
    setEditingUser(null)
    setSavingEdit(false)
  }

  const handleUpdateSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!editingUser) return

    setSavingEdit(true)
    setFeedback(null)

    try {
      const { payload, validationError } = buildPatchPayload(editingUser, editForm)
      if (validationError) {
        setFeedback({ kind: 'error', message: validationError })
        return
      }
      if (Object.keys(payload).length === 0) {
        setFeedback({ kind: 'error', message: 'Debe modificar al menos un campo para actualizar.' })
        return
      }

      const response = await userMaintenanceAPI.update(editingUser.userId, payload)
      setUsers((prev) => prev.map((item) => (item.userId === response.data.userId ? response.data : item)))
      setFeedback({ kind: 'success', message: 'Usuario actualizado correctamente.' })
      setEditingUser(null)
    } catch (error) {
      setFeedback({ kind: 'error', message: getErrorMessage(error, 'No se pudo actualizar el usuario.') })
    } finally {
      setSavingEdit(false)
    }
  }

  const handleSuspend = async (item: UserMaintenanceResponse) => {
    if (!window.confirm(`¿Deseas suspender a ${item.nombreCompleto ?? item.email}?`)) return

    setProcessingUserId(item.userId)
    setFeedback(null)

    try {
      const response = await userMaintenanceAPI.suspend(item.userId)
      setUsers((prev) => prev.map((userRow) => (userRow.userId === response.data.userId ? response.data : userRow)))
      setFeedback({ kind: 'success', message: 'Cuenta suspendida correctamente.' })
    } catch (error) {
      setFeedback({ kind: 'error', message: getErrorMessage(error, 'No se pudo suspender la cuenta.') })
    } finally {
      setProcessingUserId(null)
    }
  }

  const handleDelete = async (item: UserMaintenanceResponse) => {
    if (!window.confirm(`¿Eliminar definitivamente a ${item.nombreCompleto ?? item.email}?`)) return

    setProcessingUserId(item.userId)
    setFeedback(null)

    try {
      await userMaintenanceAPI.delete(item.userId)
      setUsers((prev) => prev.filter((userRow) => userRow.userId !== item.userId))
      setFeedback({ kind: 'success', message: 'Usuario eliminado correctamente.' })
    } catch (error) {
      setFeedback({ kind: 'error', message: getErrorMessage(error, 'No se pudo eliminar el usuario.') })
    } finally {
      setProcessingUserId(null)
    }
  }

  return (
    <div className="h-screen bg-gradient-to-br from-blue-100 via-sky-50 to-blue-100 text-slate-800 flex overflow-hidden">
      <AdminSidebar
        email={user?.email}
        role={user?.role}
        loading={loadingLogout}
        activeSection="users"
        collapsed={sidebarCollapsed}
        onToggleCollapse={toggleCollapsed}
        onDashboard={() => navigate('/admin')}
        onTriage={() => navigate('/triage')}
        onUsers={() => navigate('/admin/users')}
        onTriageList={() => navigate('/admin/triages')}
        onAppointments={() => navigate('/admin/appointments')}
        onConsultation={() => navigate('/doctor/appointments/attention')}
        onLogout={() => void handleLogout()}
      />

      <main className="flex-1 min-w-0 p-5 lg:p-6 overflow-y-auto">
        <div className="flex flex-col gap-4 mb-4">
          <div className="flex flex-col xl:flex-row xl:items-start xl:justify-between gap-3">
            <div>
              <h2 className="text-2xl font-bold text-slate-900">Mantenimiento de Usuarios</h2>
              <p className="text-sm text-slate-600 mt-1">Listado de personal con alta, edición, suspensión y eliminación.</p>
            </div>

            <div className="flex flex-wrap items-center gap-3">
                <StatusChip label={`CU03 · ${filteredSummary}`} tone="blue" />
              <button
                type="button"
                onClick={openCreateModal}
                className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm shadow-sm transition"
              >
                Registrar usuario
              </button>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
            <div className="md:col-span-2">
              <input
                type="search"
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.target.value)}
                className="w-full px-3 py-2.5 rounded-lg border border-blue-200 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                placeholder="Buscar por nombre, correo, rol, teléfono o colegiado"
              />
            </div>
            <select
              value={roleFilter}
              onChange={(event) => setRoleFilter(event.target.value as 'ALL' | UserRole)}
              className="w-full px-3 py-2.5 rounded-lg border border-blue-200 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
            >
              <option value="ALL">Todos los roles</option>
              {HOSPITAL_STAFF_ROLES.map((role) => (
                <option key={role} value={role}>{role}</option>
              ))}
            </select>
            <select
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value as 'ALL' | 'ACTIVE' | 'SUSPENDED')}
              className="w-full px-3 py-2.5 rounded-lg border border-blue-200 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
            >
              <option value="ALL">Activos y suspendidos</option>
              <option value="ACTIVE">Solo activos</option>
              <option value="SUSPENDED">Solo suspendidos</option>
            </select>
          </div>

          <div className="flex flex-wrap items-center gap-2 text-xs text-slate-600">
            <span className="px-2.5 py-1 rounded-full bg-white border border-blue-200">Usuarios: {usersCount}</span>
            <span className="px-2.5 py-1 rounded-full bg-white border border-blue-200">Activos: {activeCount}</span>
            <span className="px-2.5 py-1 rounded-full bg-white border border-blue-200">Suspendidos: {suspendedCount}</span>
            <span className="px-2.5 py-1 rounded-full bg-white border border-blue-200">Administradores: {adminCount}</span>
          </div>

          <div className="flex justify-end">
            <button
              type="button"
              onClick={() => void loadUsers(true)}
              disabled={loadingList || refreshing}
              className="px-4 py-2.5 rounded-lg bg-white hover:bg-slate-50 text-slate-700 border border-blue-200 font-semibold text-sm disabled:opacity-60 transition"
            >
              {refreshing ? 'Actualizando...' : 'Actualizar listado'}
            </button>
          </div>

          {feedback && (
            <div
              className={`rounded-lg border px-4 py-3 text-sm ${
                feedback.kind === 'success'
                  ? 'border-emerald-200 bg-emerald-50 text-emerald-700'
                  : 'border-red-200 bg-red-50 text-red-700'
              }`}
            >
              {feedback.message}
            </div>
          )}
        </div>

        <section className="rounded-xl border border-blue-200 bg-white shadow-sm p-4 lg:p-5">
          <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-3 mb-4">
            <div>
              <h3 className="text-lg font-bold text-slate-900">Listado de personal</h3>
              <p className="text-sm text-slate-600 mt-1">Cada fila muestra identidad, estado y acciones rápidas.</p>
            </div>
            <StatusChip label="Vista compacta" tone="slate" />
          </div>

          {loadingList ? (
            <div className="rounded-xl border border-blue-100 bg-blue-50 p-6 text-sm text-slate-600">Cargando usuarios...</div>
          ) : filteredUsers.length === 0 ? (
            <div className="rounded-xl border border-blue-100 bg-blue-50 p-6 text-sm text-slate-600">
              No hay usuarios que coincidan con la búsqueda.
            </div>
          ) : (
            <>
              <div className="hidden 2xl:block overflow-x-auto rounded-xl border border-blue-100">
                <table className="min-w-full text-xs">
                  <thead className="bg-blue-50 text-slate-700">
                    <tr>
                      <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Usuario</th>
                      <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Estado</th>
                      <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Perfil</th>
                      <th className="px-3 py-2.5 text-left font-semibold uppercase tracking-wide">Acciones</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-blue-100 bg-white">
                    {filteredUsers.map((item) => (
                      <tr key={item.userId} className="align-top hover:bg-sky-50/60 transition">
                        <td className="px-3 py-3">
                          <div className="font-semibold text-sm text-slate-900">{item.nombreCompleto ?? 'Sin nombre'}</div>
                          <div className="text-slate-500 mt-1 break-all">{item.email}</div>
                          <div className="text-slate-500 mt-1">ID usuario: {item.userId}{item.personalId ? ` · ID perfil: ${item.personalId}` : ''}</div>
                        </td>
                        <td className="px-3 py-3">
                          <div className="flex flex-wrap gap-2">
                            <StatusChip label={item.role} tone={item.role === 'ADMIN' ? 'red' : 'blue'} />
                            <StatusChip label={item.active ? 'Activo' : 'Suspendido'} tone={item.active ? 'emerald' : 'amber'} />
                          </div>
                        </td>
                        <td className="px-3 py-3 text-slate-700 space-y-1">
                          <div>{item.numeroColegiado ? `Colegiado: ${item.numeroColegiado}` : 'Colegiado no registrado'}</div>
                          <div>{item.telefonoCorporativo ? `Teléfono: ${item.telefonoCorporativo}` : 'Teléfono no registrado'}</div>
                          <div>{item.direccion ? `Dirección: ${item.direccion}` : 'Dirección no registrada'}</div>
                          <div>{item.especialidadId ? `Especialidad: ${specialtyById.get(item.especialidadId)?.nombre ?? `ID ${item.especialidadId}`}` : 'Especialidad no asignada'}</div>
                          <div>{item.unidadAtencionId ? `Unidad: ${careUnitById.get(item.unidadAtencionId)?.nombre ?? `ID ${item.unidadAtencionId}`}` : 'Unidad no asignada'}</div>
                        </td>
                        <td className="px-3 py-3">
                          <div className="flex flex-wrap gap-2">
                            <button type="button" onClick={() => openEditModal(item)} className="px-3 py-2 rounded-lg border border-blue-200 bg-white hover:bg-blue-50 text-blue-700 font-semibold text-xs">Editar</button>
                            <button
                              type="button"
                              onClick={() => void handleSuspend(item)}
                              disabled={!item.active || processingUserId === item.userId}
                              className="px-3 py-2 rounded-lg border border-amber-200 bg-amber-50 hover:bg-amber-100 text-amber-700 font-semibold text-xs disabled:opacity-50"
                            >
                              {processingUserId === item.userId ? 'Procesando...' : item.active ? 'Suspender' : 'Suspendido'}
                            </button>
                            <button
                              type="button"
                              onClick={() => void handleDelete(item)}
                              disabled={processingUserId === item.userId}
                              className="px-3 py-2 rounded-lg border border-red-200 bg-red-50 hover:bg-red-100 text-red-700 font-semibold text-xs disabled:opacity-50"
                            >
                              {processingUserId === item.userId ? 'Procesando...' : 'Eliminar'}
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="2xl:hidden grid grid-cols-1 lg:grid-cols-2 gap-4 text-xs">
                {filteredUsers.map((item) => (
                  <article key={item.userId} className="rounded-xl border border-blue-200 bg-blue-50 p-4">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="text-xs uppercase tracking-wide text-slate-500">Usuario #{item.userId}</p>
                        <h4 className="text-sm font-semibold text-slate-900 mt-1">{item.nombreCompleto ?? 'Sin nombre'}</h4>
                        <p className="text-xs text-slate-600 mt-1 break-all">{item.email}</p>
                        <p className="text-xs text-slate-500 mt-1">ID perfil: {item.personalId ?? 'N/D'}</p>
                      </div>
                      <div className="flex flex-wrap gap-2 justify-end">
                        <StatusChip label={item.role} tone={item.role === 'ADMIN' ? 'red' : 'blue'} />
                        <StatusChip label={item.active ? 'Activo' : 'Suspendido'} tone={item.active ? 'emerald' : 'amber'} />
                      </div>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 mt-3 text-xs text-slate-700">
                      <div className="rounded-lg border border-blue-100 bg-white px-3 py-2">Colegiado: {item.numeroColegiado || 'N/A'}</div>
                      <div className="rounded-lg border border-blue-100 bg-white px-3 py-2">Teléfono: {item.telefonoCorporativo || 'N/A'}</div>
                      <div className="rounded-lg border border-blue-100 bg-white px-3 py-2 sm:col-span-2">Dirección: {item.direccion || 'N/A'}</div>
                      <div className="rounded-lg border border-blue-100 bg-white px-3 py-2">Especialidad: {item.especialidadId ? specialtyById.get(item.especialidadId)?.nombre ?? `ID ${item.especialidadId}` : 'N/A'}</div>
                      <div className="rounded-lg border border-blue-100 bg-white px-3 py-2">Unidad: {item.unidadAtencionId ? careUnitById.get(item.unidadAtencionId)?.nombre ?? `ID ${item.unidadAtencionId}` : 'N/A'}</div>
                    </div>

                    <div className="flex flex-wrap gap-2 mt-3">
                      <button type="button" onClick={() => openEditModal(item)} className="px-3 py-2 rounded-lg border border-blue-200 bg-white hover:bg-blue-50 text-blue-700 font-semibold text-xs">Editar</button>
                      <button
                        type="button"
                        onClick={() => void handleSuspend(item)}
                        disabled={!item.active || processingUserId === item.userId}
                        className="px-3 py-2 rounded-lg border border-amber-200 bg-amber-50 hover:bg-amber-100 text-amber-700 font-semibold text-xs disabled:opacity-50"
                      >
                        {processingUserId === item.userId ? 'Procesando...' : item.active ? 'Suspender' : 'Suspendido'}
                      </button>
                      <button
                        type="button"
                        onClick={() => void handleDelete(item)}
                        disabled={processingUserId === item.userId}
                        className="px-3 py-2 rounded-lg border border-red-200 bg-red-50 hover:bg-red-100 text-red-700 font-semibold text-xs disabled:opacity-50"
                      >
                        {processingUserId === item.userId ? 'Procesando...' : 'Eliminar'}
                      </button>
                    </div>
                  </article>
                ))}
              </div>
            </>
          )}
        </section>
      </main>

      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="w-full max-w-3xl rounded-2xl border border-blue-200 bg-white shadow-2xl overflow-hidden max-h-[92vh] flex flex-col">
            <div className="px-5 py-4 border-b border-blue-100 flex items-start justify-between gap-4">
              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-slate-400">Alta rápida</p>
                <h3 className="text-xl font-bold text-slate-900 mt-1">Registrar usuario</h3>
                <p className="text-sm text-slate-600 mt-1">Crea personal nuevo sin abandonar el listado.</p>
              </div>
              <button type="button" onClick={closeCreateModal} className="px-3 py-2 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-semibold text-sm">Cerrar</button>
            </div>

            <form onSubmit={handleCreateSubmit} className="p-5 space-y-4 overflow-y-auto">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">
                    Nombre completo
                    <span className={`ml-1 font-normal ${createForm.nombreCompleto.length < 5 ? 'text-red-500' : 'text-gray-400'}`}>
                      ({createForm.nombreCompleto.length}/150)
                    </span>
                  </label>
                  <input
                    type="text" name="nombreCompleto" value={createForm.nombreCompleto}
                    onChange={handleCreateChange} required minLength={5} maxLength={150}
                    placeholder="Solo letras, mín. 5 caracteres"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Correo institucional</label>
                  <input
                    type="email" name="email" value={createForm.email}
                    onChange={handleCreateChange} required
                    placeholder="usuario@hospital.com"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">
                    Contraseña inicial
                    {createForm.password.length > 0 && createForm.password.length < 6 && (
                      <span className="ml-1 font-normal text-red-500">(mín. 6 caracteres)</span>
                    )}
                  </label>
                  <input
                    type="password" name="password" value={createForm.password}
                    onChange={handleCreateChange} required minLength={6}
                    placeholder="Mínimo 6 caracteres"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Rol</label>
                  <select name="rol" value={createForm.rol} onChange={handleCreateChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm">
                    {HOSPITAL_STAFF_ROLES.map((role) => <option key={role} value={role}>{role}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">
                    Teléfono corporativo
                    <span className={`ml-1 font-normal ${createForm.telefonoCorporativo.length > 0 && (createForm.telefonoCorporativo.length < 8 || createForm.telefonoCorporativo.length > 15) ? 'text-red-500' : 'text-gray-400'}`}>
                      ({createForm.telefonoCorporativo.length}/15 · mín. 8)
                    </span>
                  </label>
                  <input
                    type="text" name="telefonoCorporativo" value={createForm.telefonoCorporativo}
                    onChange={handleCreateChange} required
                    inputMode="numeric" minLength={8} maxLength={15}
                    placeholder="Solo dígitos, 8-15 números"
                    pattern="^[0-9]{8,15}$"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">
                    Número de colegiado
                    <span className="ml-1 font-normal text-gray-400">({createForm.numeroColegiado.length}/20)</span>
                  </label>
                  <input
                    type="text" name="numeroColegiado" value={createForm.numeroColegiado}
                    onChange={handleCreateChange} maxLength={20}
                    placeholder="Opcional, máx. 20 caracteres"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Especialidad</label>
                  <select
                    name="especialidadId"
                    value={createForm.especialidadId}
                    onChange={handleCreateChange}
                    disabled={loadingCatalogs}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm disabled:bg-slate-100"
                  >
                    <option value="">Selecciona una especialidad</option>
                    {specialties.map((specialty) => (
                      <option key={specialty.id} value={specialty.id}>{specialty.nombre}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Unidad de atención</label>
                  <select
                    name="unidadAtencionId"
                    value={createForm.unidadAtencionId}
                    onChange={handleCreateChange}
                    disabled={loadingCatalogs}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm disabled:bg-slate-100"
                  >
                    <option value="">Selecciona una unidad de atención</option>
                    {careUnits.map((unit) => (
                      <option key={unit.id} value={unit.id}>{unit.nombre}</option>
                    ))}
                  </select>
                </div>
                <div className="md:col-span-2">
                  <label className="block text-xs font-semibold text-gray-600 mb-1">
                    Dirección
                    {createForm.direccion.length > 0 && createForm.direccion.length < 5 && (
                      <span className="ml-1 font-normal text-red-500">(mín. 5 caracteres)</span>
                    )}
                  </label>
                  <input
                    type="text" name="direccion" value={createForm.direccion}
                    onChange={handleCreateChange} required minLength={5} maxLength={255}
                    placeholder="Dirección completa, mín. 5 caracteres"
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
                  />
                </div>
              </div>

              <div className="flex items-center justify-end gap-2 pt-1">
                <button type="button" onClick={closeCreateModal} className="px-4 py-2 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-semibold text-sm">Cancelar</button>
                <button type="submit" disabled={creating} className="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-4 rounded-lg transition disabled:opacity-50">{creating ? 'Creando...' : 'Registrar usuario'}</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {editingUser && (
        <div className="fixed inset-0 z-50 bg-slate-950/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="w-full max-w-3xl rounded-2xl border border-blue-200 bg-white shadow-2xl overflow-hidden max-h-[92vh] flex flex-col">
            <div className="px-5 py-4 border-b border-blue-100 flex items-start justify-between gap-4">
              <div>
                <p className="text-xs uppercase tracking-[0.2em] text-slate-400">Edición parcial</p>
                <h3 className="text-xl font-bold text-slate-900 mt-1">Actualizar usuario</h3>
                <p className="text-sm text-slate-600 mt-1">Solo modifica los campos que necesites.</p>
              </div>
              <StatusChip label={editingUser.role} tone={editingUser.role === 'ADMIN' ? 'red' : 'blue'} />
            </div>

            <form onSubmit={handleUpdateSubmit} className="p-5 space-y-4 overflow-y-auto">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Nombre completo</label>
                  <input type="text" name="nombreCompleto" value={editForm.nombreCompleto} onChange={handleEditChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Rol</label>
                  <select name="rol" value={editForm.rol} onChange={handleEditChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm">
                    <option value="">Sin cambios</option>
                    {HOSPITAL_STAFF_ROLES.map((role) => <option key={role} value={role}>{role}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Teléfono corporativo</label>
                  <input type="text" name="telefonoCorporativo" value={editForm.telefonoCorporativo} onChange={handleEditChange} pattern="^[0-9]{8,15}$" className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Número de colegiado</label>
                  <input type="text" name="numeroColegiado" value={editForm.numeroColegiado} onChange={handleEditChange} maxLength={20} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Especialidad</label>
                  <select
                    name="especialidadId"
                    value={editForm.especialidadId}
                    onChange={handleEditChange}
                    disabled={loadingCatalogs}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm disabled:bg-slate-100"
                  >
                    <option value="">Sin cambios</option>
                    {specialties.map((specialty) => (
                      <option key={specialty.id} value={specialty.id}>{specialty.nombre}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Unidad de atención</label>
                  <select
                    name="unidadAtencionId"
                    value={editForm.unidadAtencionId}
                    onChange={handleEditChange}
                    disabled={loadingCatalogs}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm disabled:bg-slate-100"
                  >
                    <option value="">Sin cambios</option>
                    {careUnits.map((unit) => (
                      <option key={unit.id} value={unit.id}>{unit.nombre}</option>
                    ))}
                  </select>
                </div>
                <div className="md:col-span-2">
                  <label className="block text-xs font-semibold text-gray-600 mb-1">Dirección</label>
                  <input type="text" name="direccion" value={editForm.direccion} onChange={handleEditChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm" />
                </div>
              </div>

              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 pt-1">
                <p className="text-xs text-slate-500">Usuario: <span className="font-semibold text-slate-700">{editingUser.email}</span> · ID {editingUser.userId}</p>
                <div className="flex items-center gap-2">
                  <button type="button" onClick={closeEditModal} className="px-4 py-2 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 font-semibold text-sm">Cancelar</button>
                  <button type="submit" disabled={savingEdit} className="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-4 rounded-lg transition disabled:opacity-50">{savingEdit ? 'Guardando...' : 'Guardar cambios'}</button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default UserMaintenance








