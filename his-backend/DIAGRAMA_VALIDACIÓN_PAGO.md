#  Diagrama: Flujo de Pagos en el Sistema HIS

## 1. ARQUITECTURA GENERAL

```
┌─────────────────────────────────────────────────────────────────┐
│                       FRONTEND (React)                          │
│                  http://localhost:5173                          │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  AppointmentAttentionWorkspace.tsx                       │  │
│  │  (/doctor/appointments/attention)                        │  │
│  │                                                          │  │
│  │  GET /api/appointments/attention/queue                  │  │
│  │         ↓                                                │  │
│  │  Muestra cola de pacientes "disponibles"                │  │
│  │  (SOLO con pago validado)                               │  │
│  └──────────────────────────────────────────────────────────┘  │
│                           ↑                                      │
│                           │ HTTP Request                         │
│                           ↓                                      │
└─────────────────────────────────────────────────────────────────┘
                            │
                            │ JSON
                            │
┌─────────────────────────────────────────────────────────────────┐
│                    BACKEND (Spring Boot)                        │
│                  http://localhost:8080                          │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  AppointmentAttentionController                          │  │
│  │  GET /api/appointments/attention/queue                  │  │
│  │         ↓                                                │  │
│  │  MedicalAppointmentAttentionService                     │  │
│  │  .getPatientQueue(emailDoctor)                          │  │
│  │         ↓                                                │  │
│  │  SqlMedicalAppointmentRepository                        │  │
│  │  .findPendingQueueByDoctor(personalId)                  │  │
│  │         ↓                                                │  │
│  │  Filter SQL:                                            │  │
│  │    WHERE estado_cita = 'PROGRAMADA'                     │  │
│  │      AND estado_administrativo = 'PAGO_VALIDADO'  ⭐    │  │
│  │      AND personal_id = ?                                │  │
│  │      AND is_active = true                               │  │
│  │         ↓                                                │  │
│  │  Retorna List<MedicalAppointmentQueueItemResponse>      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                           ↑                                      │
│                           │                                      │
└─────────────────────────────────────────────────────────────────┘
                            │
                            │ SQL Query
                            │
┌─────────────────────────────────────────────────────────────────┐
│                   PostgreSQL Database                           │
│                     jdbc:postgresql://...                       │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │             TABLE: cita_medica                           │  │
│  │                                                          │  │
│  │  cita_medica_id (PK)                                     │  │
│  │  paciente_id (FK)                                        │  │
│  │  personal_id (FK) ← Doctor/Médico                        │  │
│  │  estado_cita ← PROGRAMADA, EN_CURSO, ATENDIDA...        │  │
│  │  estado_administrativo ← ⭐ PAGO_VALIDADO               │  │
│  │                            PAGO_PENDIENTE                │  │
│  │  observacion_administrativa ← Razón de rechazo          │  │
│  │  metodo_pago ← TARJETA, SEGURO                          │  │
│  │  costo_consulta ← 175.00                                │  │
│  │  created_at, updated_at                                 │  │
│  │  is_active ← true/false                                 │  │
│  │                                                          │  │
│  │  [ROW 1] estado_administrativo = PAGO_VALIDADO ✅        │  │
│  │  [ROW 2] estado_administrativo = PAGO_PENDIENTE ❌       │  │
│  │  [ROW 3] estado_administrativo = PAGO_VALIDADO ✅        │  │
│  │  [ROW 4] estado_administrativo = PAGO_PENDIENTE ❌       │  │
│  │  [ROW 5] estado_administrativo = PAGO_VALIDADO ✅        │  │
│  │                                                          │  │
│  │  ← Backend solo retorna las ✅                           │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. FLUJO DE CREACIÓN DE CITA CON VALIDACIÓN DE PAGO

```
┌─────────────────────────────────────────────────────────────────┐
│  1. FRONTEND - Formulario de Agendamiento de Cita               │
│  POST /api/appointments                                         │
│                                                                 │
│  {                                                              │
│    pacienteId: 1,                                               │
│    medicoPersonalId: 1,                                         │
│    fechaCita: "2026-05-25",                                     │
│    horaCita: "09:00",                                           │
│    metodoPago: "TARJETA",  ← TARJETA o SEGURO                  │
│    numeroTarjeta: "4111111111111111",                           │
│    cvc: "123",                                                  │
│    ...                                                          │
│  }                                                              │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│  2. BACKEND - AppointmentController                             │
│  POST /api/appointments                                         │
│  └─> AppointmentService.scheduleAppointment()                 │
│                                                                 │
│      Valida datos básicos:                                      │
│      ✓ Paciente existe                                          │
│      ✓ Doctor existe                                            │
│      ✓ Fecha es válida (24 hrs en adelante)                     │
│      ✓ Horario es válido (08:00-16:30)                          │
│      ✓ Doctor no tiene otra cita en ese horario                 │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│  3. VALIDACIÓN DE PAGO - validateAndSimulatePayment()           │
│                                                                 │
│  SI metodoPago == TARJETA:                                      │
│  ┌────────────────────────────────────────────────────────┐    │
│  │ Verifica:                                              │    │
│  │ ✓ Banco, número, vencimiento, titular, CVC presentes │    │
│  │ ✓ Fecha de vencimiento no expirada                    │    │
│  │ ✓ Formato de fecha válido (MM/yy)                     │    │
│  │                                                        │    │
│  │ Lógica de aprobación:                                  │    │
│  │ approved = !numeroTarjeta.endsWith("0000")             │    │
│  │                                                        │    │
│  │ SI termina en 0000:                                    │    │
│  │   ❌ RECHAZADA → "saldo insuficiente"                 │    │
│  │ SI NO termina en 0000:                                │    │
│  │   ✅ APROBADA → "validado correctamente"              │    │
│  └────────────────────────────────────────────────────────┘    │
│                                                                 │
│  SI metodoPago == SEGURO:                                      │
│  ┌────────────────────────────────────────────────────────┐    │
│  │ Verifica:                                              │    │
│  │ ✓ aseguradoraId válida                                │    │
│  │ ✓ numeroPoliza presente                                │    │
│  │ ✓ Aseguradora existe en BD                             │    │
│  │                                                        │    │
│  │ Lógica de aprobación:                                  │    │
│  │ approved = !(poliza.startsWith("X") ||                │    │
│  │             poliza.contains("RECHAZADA"))              │    │
│  │                                                        │    │
│  │ SI empieza con X o contiene RECHAZADA:               │    │
│  │   ❌ RECHAZADA → "póliza no vigente"                 │    │
│  │ SI NO:                                                │    │
│  │   ✅ APROBADA → "validado correctamente"              │    │
│  └────────────────────────────────────────────────────────┘    │
│                                                                 │
│  RETORNA: PaymentValidationResult                              │
│  {                                                              │
│    approved: true/false,                                        │
│    message: "..."                                               │
│  }                                                              │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│  4. CREACIÓN DE OBJETO MedicalAppointment                       │
│                                                                 │
│  MedicalAppointment appointment = builder()                     │
│    .pacienteId(...)                                             │
│    .personalId(...)                                             │
│    .estadoCita(PROGRAMADA)                                      │
│    .metodoPago(TARJETA/SEGURO)                                 │
│    .costoConsulta(175.0)                                        │
│    .estadoAdministrativo(                                       │
│        paymentValidation.approved                               │
│            ? PAGO_VALIDADO ✅                                   │
│            : PAGO_PENDIENTE ❌                                  │
│    )                                                            │
│    .observacionAdministrativa(paymentValidation.message)       │
│    .build()                                                     │
│                                                                 │
│  ⭐ AQUÍ SE DEFINE SI APARECERÁ EN LA COLA ⭐                    │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│  5. PERSISTENCIA EN BASE DE DATOS                               │
│                                                                 │
│  medicalAppointmentRepository.save(appointment)                │
│    ↓                                                             │
│  INSERT INTO cita_medica (                                      │
│    paciente_id, personal_id, ...,                               │
│    estado_cita,              ← 'PROGRAMADA'                     │
│    estado_administrativo,    ← 'PAGO_VALIDADO' o 'PAGO_...'   │
│    observacion_administrativa, ← Razón                          │
│    ...                                                          │
│  ) VALUES (...)                                                 │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│  6. RESPUESTA AL FRONTEND                                       │
│                                                                 │
│  ScheduleAppointmentResponse {                                  │
│    citaMedicaId: 1,                                             │
│    estadoCita: "PROGRAMADA",                                    │
│    estadoAdministrativo: "PAGO_VALIDADO",  ← El valor clave!  │
│    pagoValidado: true,                                          │
│    mensajeValidacion: "Pago con tarjeta validado correctamente" │
│  }                                                              │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. FLUJO DE CONSULTA DE COLA DE ATENCIÓN

```
┌─────────────────────────────────────────────────────────────────┐
│  DOCTOR AUTENTICADO VE /doctor/appointments/attention           │
│                                                                 │
│  useEffect(() => {                                              │
│    appointmentAttentionAPI.queue()                             │
│      .then(queueRes => setQueue(queueRes.data))                │
│  })                                                             │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│  GET /api/appointments/attention/queue                          │
│                                                                 │
│  AppointmentAttentionController.getQueue()                     │
│    └─> useCase.getPatientQueue(getAuthenticatedEmail())        │
│            ↓                                                    │
│        MedicalAppointmentAttentionService.getPatientQueue()   │
│            ├─ Obtener email del doctor                         │
│            ├─ Resolver personalId                              │
│            └─> appointmentRepository.findPendingQueueByDoctor  │
│                    ↓                                            │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│  BASE DE DATOS - FILTRAR CITAS                                  │
│                                                                 │
│  SELECT * FROM cita_medica                                      │
│  WHERE                                                          │
│    personal_id = ? (ID del doctor autenticado)                  │
│    AND estado_cita = 'PROGRAMADA'              ⭐               │
│    AND estado_administrativo = 'PAGO_VALIDADO' ⭐               │
│    AND is_active = true                                        │
│  ORDER BY fecha_cita ASC, hora_cita ASC                         │
│                                                                 │
│  ┌──────────────────────────────────────────────┐              │
│  │ Tabla cita_medica (ejemplo con 5 registros) │              │
│  ├──────────────────────────────────────────────┤              │
│  │ ID│ Estado     │ Pago Validado │ ¿Aparece? │              │
│  ├──────────────────────────────────────────────┤              │
│  │ 1 │ PROGRAMADA │ PAGO_VALIDADO │ ✅ Sí     │              │
│  │ 2 │ PROGRAMADA │ PAGO_PENDIENTE│ ❌ No     │              │
│  │ 3 │ PROGRAMADA │ PAGO_VALIDADO │ ✅ Sí     │              │
│  │ 4 │ EN_CURSO   │ PAGO_VALIDADO │ ❌ No*    │              │
│  │ 5 │ ATENDIDA   │ PAGO_VALIDADO │ ❌ No*    │              │
│  └──────────────────────────────────────────────┘              │
│  * No cumple estado_cita = 'PROGRAMADA'                        │
│                                                                 │
│  RESULTADO: RETORNA SOLO citas 1 y 3                           │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│  MAPEO A DTOs                                                   │
│                                                                 │
│  List<MedicalAppointmentQueueItemResponse> = [                 │
│    {                                                            │
│      citaMedicaId: 1,                                           │
│      pacienteNombre: "Juan Pérez",                              │
│      fechaCita: "2026-05-25",                                   │
│      horaCita: "09:00",                                         │
│      especialidadNombre: "Cardiología",                         │
│      prioridad: "VERDE",                                        │
│      estadoAdministrativo: "PAGO_VALIDADO"                     │
│    },                                                           │
│    {                                                            │
│      citaMedicaId: 3,                                           │
│      pacienteNombre: "María López",                             │
│      fechaCita: "2026-05-25",                                   │
│      horaCita: "10:30",                                         │
│      especialidadNombre: "Cardiología",                         │
│      prioridad: "AMARILLO",                                     │
│      estadoAdministrativo: "PAGO_VALIDADO"                     │
│    }                                                            │
│  ]                                                              │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│  FRONTEND - MOSTRAR COLA                                        │
│                                                                 │
│  ✅ APARECEN EN LA PANTALLA:                                    │
│                                                                 │
│  Cola de pacientes:                                             │
│  ┌─────────────────────────────────────┐                       │
│  │ Juan Pérez                          │                       │
│  │ Cita #1 • 2026-05-25 09:00         │                       │
│  │ Prioridad: VERDE • Cardiología      │                       │
│  │ [Iniciar atención]                  │                       │
│  └─────────────────────────────────────┘                       │
│  ┌─────────────────────────────────────┐                       │
│  │ María López                         │                       │
│  │ Cita #3 • 2026-05-25 10:30         │                       │
│  │ Prioridad: AMARILLO • Cardiología  │                       │
│  │ [Iniciar atención]                  │                       │
│  └─────────────────────────────────────┘                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. TABLA COMPARATIVA: QUÉ APARECE Y QUÉ NO

```
┌─────────────────────────────────────────────────────────────────┐
│ CRITERIOS REQUERIDOS PARA APARECER EN LA COLA                  │
├─────────────────────────────────────────────────────────────────┤
│ 1. estado_cita = 'PROGRAMADA'                                   │
│ 2. estado_administrativo = 'PAGO_VALIDADO'                      │ ⭐
│ 3. personal_id = [ID del doctor autenticado]                    │
│ 4. is_active = true                                             │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────┐
│ Ejemplo 1: APARECE ✅                                                │
├──────────────────────────────────────────────────────────────────────┤
│ personal_id:            1 (Doctor "Juan")                           │
│ estado_cita:            'PROGRAMADA'           ✓                     │
│ estado_administrativo:  'PAGO_VALIDADO'        ✓ ← VALIDADO!        │
│ is_active:              true                   ✓                     │
│                                                                      │
│ RESULTADO: ✅ APARECE EN LA COLA                                    │
└──────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────┐
│ Ejemplo 2: NO APARECE ❌ (Pago rechazado)                           │
├──────────────────────────────────────────────────────────────────────┤
│ personal_id:            1 (Doctor "Juan")                           │
│ estado_cita:            'PROGRAMADA'           ✓                     │
│ estado_administrativo:  'PAGO_PENDIENTE'       ✗ ← RECHAZADO!       │
│ is_active:              true                   ✓                     │
│                                                                      │
│ RESULTADO: ❌ NO APARECE                                            │
│ RAZÓN: Pago no validado                                            │
│ SOLUCIÓN: Crear nueva cita con datos de pago válidos              │
└──────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────┐
│ Ejemplo 3: NO APARECE ❌ (Doctor diferente)                         │
├──────────────────────────────────────────────────────────────────────┤
│ personal_id:            2 (Doctor "María")     ✗ ← Otro doctor!     │
│ estado_cita:            'PROGRAMADA'           ✓                     │
│ estado_administrativo:  'PAGO_VALIDADO'        ✓                     │
│ is_active:              true                   ✓                     │
│                                                                      │
│ RESULTADO: ❌ NO APARECE en la cola del Doctor Juan                │
│ RAZÓN: La cita es del Doctor María, no de Juan                    │
│ NOTA: Aparecería en la cola del Doctor María                      │
└──────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────┐
│ Ejemplo 4: NO APARECE ❌ (Cita ya atendida)                         │
├──────────────────────────────────────────────────────────────────────┤
│ personal_id:            1 (Doctor "Juan")                           │
│ estado_cita:            'ATENDIDA'             ✗ ← Ya fue atendida! │
│ estado_administrativo:  'PAGO_VALIDADO'        ✓                     │
│ is_active:              true                   ✓                     │
│                                                                      │
│ RESULTADO: ❌ NO APARECE                                            │
│ RAZÓN: Cita ya fue atendida                                        │
│ NOTA: Puedes verla en el historial, no en la cola activa          │
└──────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────┐
│ Ejemplo 5: NO APARECE ❌ (Cita en curso)                            │
├──────────────────────────────────────────────────────────────────────┤
│ personal_id:            1 (Doctor "Juan")                           │
│ estado_cita:            'EN_CURSO'             ✗ ← Ya abierta!      │
│ estado_administrativo:  'PAGO_VALIDADO'        ✓                     │
│ is_active:              true                   ✓                     │
│                                                                      │
│ RESULTADO: ❌ NO APARECE en la cola                                 │
│ NOTA: Aparece en "Atención en curso" (lado derecho de la pantalla) │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 5. VALIDACIÓN DE PAGO - ÁRBOLES DE DECISIÓN

###  TARJETA

```
POST /api/appointments
  {
    metodoPago: "TARJETA",
    numeroTarjeta: "????",
    ...
  }
    ↓
validateCardAuthorization()
    ├─ ¿Banco presente? 
    │  NO → ❌ Error: "Campos obligatorios faltantes"
    │  SÍ ↓
    ├─ ¿Número tarjeta presente?
    │  NO → ❌ Error: "Campos obligatorios faltantes"
    │  SÍ ↓
    ├─ ¿Vencimiento presente?
    │  NO → ❌ Error: "Campos obligatorios faltantes"
    │  SÍ ↓
    ├─ ¿Titular presente?
    │  NO → ❌ Error: "Campos obligatorios faltantes"
    │  SÍ ↓
    ├─ ¿CVC presente?
    │  NO → ❌ Error: "Campos obligatorios faltantes"
    │  SÍ ↓
    ├─ ¿Fecha vencimiento válida (MM/yy)?
    │  NO → ❌ Error: "Formato de fecha inválido"
    │  SÍ ↓
    ├─ ¿Tarjeta no expirada?
    │  NO → ❌ Error: "Tarjeta expirada"
    │  SÍ ↓
    ├─ ¿Número NO termina en 0000?
    │  SÍ → ✅ APROBADA
    │       estado_administrativo = "PAGO_VALIDADO"
    │       mensaje = "Pago con tarjeta validado correctamente"
    │
    │  NO → ❌ RECHAZADA
    │       estado_administrativo = "PAGO_PENDIENTE"
    │       mensaje = "Simulación de saldo insuficiente"
```

###  SEGURO

```
POST /api/appointments
  {
    metodoPago: "SEGURO",
    aseguradoraId: 1,
    numeroPoliza: "???",
    ...
  }
    ↓
simulateInsuranceCoverage()
    ├─ ¿aseguradoraId presente?
    │  NO → ❌ Error: "aseguradoraId obligatorio"
    │  SÍ ↓
    ├─ ¿numeroPoliza presente?
    │  NO → ❌ Error: "numeroPoliza obligatorio"
    │  SÍ ↓
    ├─ ¿Aseguradora existe en BD?
    │  NO → ❌ Error: "Aseguradora no existe"
    │  SÍ ↓
    ├─ ¿Póliza NO empieza con "X" Y NO contiene "RECHAZADA"?
    │  SÍ → ✅ APROBADA
    │       estado_administrativo = "PAGO_VALIDADO"
    │       mensaje = "Cobertura de seguro validada correctamente"
    │
    │  NO → ❌ RECHAZADA
    │       estado_administrativo = "PAGO_PENDIENTE"
    │       mensaje = "Simulación de póliza no vigente/no cubierta"
```

---

## 6. RESUMEN: ¿DÓNDE SE GUARDA EL PAGO?

```
┌──────────────────────────────────────────────────────────────────┐
│                    RESPUESTA CORTA                               │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Base de Datos: PostgreSQL (localhost:5432)                     │
│  Tabla:         cita_medica                                      │
│  Columna:       estado_administrativo                            │
│                                                                  │
│  Valores posibles:                                               │
│  ├─ 'PAGO_VALIDADO'   → Pago aprobado ✅                       │
│  └─ 'PAGO_PENDIENTE'  → Pago rechazado ❌                      │
│                                                                  │
│  Campo adicional:                                                │
│  └─ observacion_administrativa → Razón del rechazo              │
│     ├─ "Pago con tarjeta validado correctamente"                │
│     ├─ "Saldo insuficiente"                                     │
│     ├─ "Cobertura de seguro validada correctamente"             │
│     └─ "Póliza no vigente/no cubierta"                          │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 7. MAPA MENTAL: FLUJO COMPLETO

```
                    ┌─ PACIENTE DE TRIAGE ─────┐
                    │ (CU 2.0 - Urgencia)      │
                    │ ✓ Signos vitales         │
                    │ ✓ Prioridad              │
                    │ ✗ NO cita automática     │
                    └──────────┬─────────────┘
                               │
                    ¿Crear cita médica?
                               │
                ┌──────────────┴──────────────┐
                │                             │
           Sí (CU04)                      No
                │                           │
    ScheduleAppointment              Esperar en
         API Request                  urgencias
                │
        ┌─────────────────────┐
        │ Validar Datos Cita  │
        └──────────┬──────────┘
                   │
        ┌─────────────────────┐
        │ Validar Pago ⭐      │ ←  TARJETA o  SEGURO
        │ (Simulado)          │
        └──────────┬──────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
    ✅ APROBADO        ❌ RECHAZADO
        │                     │
    PAGO_VALIDADO         PAGO_PENDIENTE
        │                     │
    ┌───┴──────────┐      ┌────┴────────┐
    │ Guarda en    │      │ Guarda en   │
    │ cita_medica  │      │ cita_medica │
    │              │      │             │
    │ estado:      │      │ estado:     │
    │ PROGRAMADA   │      │ PROGRAMADA  │
    │              │      │             │
    │ pago:        │      │ pago:       │
    │ VALIDADO ✅  │      │ PENDIENTE❌ │
    └───┬──────────┘      └────┬────────┘
        │                      │
        ↓                      ↓
    ┌─────────────┐       ┌──────────────┐
    │ APARECE EN  │       │ NO APARECE   │
    │ COLA (CU06) │       │ EN LA COLA   │
    │             │       │              │
    │ Doctor ve   │       │ Paciente     │
    │ paciente en │       │ puede:       │
    │ /doctor/... │       │ ├─ Pagar sin │
    │             │       │ │  sistema   │
    │ [Iniciar    │       │ ├─ Crear     │
    │ atención]   │       │ │  otra cita │
    │             │       │ └─ Ir a      │
    └──────┬──────┘       │   urgencias │
           │              └──────┬───────┘
           │                     │
       POST /open                │
           │              (cita nunca
       EN_CURSO           entra en
           │               cola)
      Evaluar & │
      Diagnóstico
           │
      PATCH /close
           │
        ATENDIDA
```

---

**Diagrama generado:** 2026-05-21
