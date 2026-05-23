# 🏥 RESUMEN EJECUTIVO: ¿Por qué no aparecen pacientes para atender?

## 📌 La Pregunta

> Mi duda es porque aquí no aparecen pacientes para atender, que deberían de ser los de consultas agendadas y los que entraron con triage, ambos ya validados el pago que si lo realizaron: http://localhost:5173/doctor/appointments/attention

> ¿En dónde se guarda o se persiste la información de pago?

---

## ✅ Respuesta Corta

### ¿Dónde se persiste el pago?

**En la tabla `cita_medica` de PostgreSQL:**

| Campo | Valor | Significado |
|-------|-------|------------|
| `estado_administrativo` | `PAGO_VALIDADO` | ✅ Pago aprobado, paciente aparece en cola |
| `estado_administrativo` | `PAGO_PENDIENTE` | ❌ Pago rechazado, paciente NO aparece |
| `observacion_administrativa` | Mensaje | Razón del rechazo (ej: "saldo insuficiente") |

**Otros campos relacionados:**
- `metodoPago` → TARJETA o SEGURO
- `costo_consulta` → Siempre 175.00 quetzales
- `transaccion_id` → Identificador único de la transacción

---

### ¿Por qué no aparecen pacientes?

**Hay 3 razones posibles:**

#### 1️⃣ **No hay citas en la BD**
- No se han creado citas aún
- El script de datos de prueba no se ejecutó
- **Solución**: Crear citas vía API POST `/api/appointments`

#### 2️⃣ **Las citas tienen `PAGO_PENDIENTE`**
- El pago fue rechazado en la validación
- Tarjeta termina en `0000` (simulación de saldo insuficiente)
- Póliza de seguro empieza con `X` o contiene "RECHAZADA"
- **Solución**: Crear citas con datos de pago válidos (tarjeta que no termina en 0000, póliza normal)

#### 3️⃣ **Las citas no están asignadas al doctor autenticado**
- El `personal_id` de la cita no coincide con el del doctor logueado
- **Solución**: Verificar que las citas fueron creadas con el médico correcto

---

## 🔍 Cómo Verificar el Estado Actual

### Opción 1: Ejecutar queries SQL

```sql
-- Ver el estado de TODAS las citas
SELECT cita_medica_id, paciente_id, personal_id, estado_cita, 
       estado_administrativo, observacion_administrativa
FROM cita_medica
WHERE is_active = true
ORDER BY fecha_cita DESC;

-- Ver SOLO las citas que deberían aparecer en la cola del doctor
SELECT cita_medica_id, paciente_id, personal_id, fecha_cita, hora_cita
FROM cita_medica
WHERE estado_cita = 'PROGRAMADA'
  AND estado_administrativo = 'PAGO_VALIDADO'
  AND personal_id = 1  -- Reemplazar con el ID del doctor
ORDER BY fecha_cita ASC;
```

### Opción 2: Ejecutar el script PowerShell

```bash
.\check_appointments.ps1
```

Este script mostrará:
- ✅ Citas disponibles para atención (cola del doctor)
- ⏳ Citas con pago pendiente
- 👨‍⚕️ Doctores registrados
- 👥 Pacientes registrados

---

## 🚀 Cómo Resolver en 3 pasos

### Paso 1: Verificar la BD

```sql
SELECT COUNT(*) FROM cita_medica WHERE estado_administrativo = 'PAGO_VALIDADO';
```

Si el resultado es `0`, necesitas crear citas.

### Paso 2: Crear citas vía API

**Cita con pago APROBADO (tarjeta):**
```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "pacienteId": 1,
    "medicoPersonalId": 1,
    "especialidadId": 1,
    "fechaCita": "2026-05-25",
    "horaCita": "09:00",
    "motivoConsulta": "Control general",
    "metodoPago": "TARJETA",
    "bancoTarjeta": "Banco Industrial",
    "numeroTarjeta": "4111111111111111",
    "fechaVencimientoTarjeta": "12/29",
    "nombreTitularTarjeta": "Test User",
    "cvc": "123"
  }'
```

**Cita con pago APROBADO (seguro):**
```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "pacienteId": 1,
    "medicoPersonalId": 1,
    "especialidadId": 1,
    "fechaCita": "2026-05-25",
    "horaCita": "10:00",
    "motivoConsulta": "Control por seguro",
    "metodoPago": "SEGURO",
    "aseguradoraId": 1,
    "numeroPoliza": "POL2024001234"
  }'
```

### Paso 3: Verificar en el frontend

1. Login como doctor en `http://localhost:5173`
2. Ve a `/doctor/appointments/attention`
3. Deberías ver las citas que acabas de crear en la cola

---

## 💾 Tabla de Persistencia

```
PostgreSQL (Base de Datos)
    ↓
Tabla: cita_medica
    ↓
Columnas:
  - cita_medica_id (PK)
  - paciente_id (FK)
  - personal_id (FK) ← doctor/médico
  - estado_cita ← PROGRAMADA, EN_CURSO, ATENDIDA, CANCELADA
  - estado_administrativo ← PAGO_VALIDADO o PAGO_PENDIENTE ⭐
  - observacion_administrativa ← Mensaje de error
  - metodo_pago ← TARJETA o SEGURO
  - costo_consulta ← 175.00
    ↓
Java Entity: MedicalAppointmentJpaEntity
    ↓
Domain Model: MedicalAppointment
    ↓
Repository: SqlMedicalAppointmentRepository
    ↓
Service: MedicalAppointmentAttentionService
    ↓
REST Controller: AppointmentAttentionController
    ↓
Frontend: AppointmentAttentionWorkspace.tsx
```

---

## 🔐 Validación de Pagos (Simulada)

### 💳 TARJETA

```
SI termina en 0000
  → RECHAZADA (saldo insuficiente)
  → estado_administrativo = PAGO_PENDIENTE

SI NO termina en 0000
  → APROBADA
  → estado_administrativo = PAGO_VALIDADO
```

**Ejemplos:**
- `4111111111111111` → ✅ APROBADA
- `4111111111110000` → ❌ RECHAZADA
- `5500005500005500` → ✅ APROBADA
- `5555555555550000` → ❌ RECHAZADA

### 🏥 SEGURO

```
SI póliza empieza con X O contiene "RECHAZADA"
  → RECHAZADA (póliza no vigente)
  → estado_administrativo = PAGO_PENDIENTE

SI NO
  → APROBADA
  → estado_administrativo = PAGO_VALIDADO
```

**Ejemplos:**
- `POL2024001234` → ✅ APROBADA
- `XPOL2024001234` → ❌ RECHAZADA
- `POL-RECHAZADA` → ❌ RECHAZADA
- `POL2024RECHAZADA` → ❌ RECHAZADA

---

## 🔄 Flujo Completo de una Cita

```
1. TRIAGE (CU2.0)
   ├─ Paciente entra de urgencia
   ├─ Se registran signos vitales
   ├─ Se asigna prioridad (ROJO, NARANJA, AMARILLO, VERDE)
   └─ Se crea un registro en tabla signos_vitales

2. APPOINTMENT SCHEDULING (CU04)
   ├─ POST /api/appointments (crear cita)
   ├─ Validar pago (tarjeta o seguro)
   │  ├─ SI aprobado → PAGO_VALIDADO ✅
   │  └─ SI rechazado → PAGO_PENDIENTE ❌
   ├─ Persistir en tabla cita_medica
   └─ Responder al frontend con estado

3. APPOINTMENT ATTENTION (CU06) ← AQUÍ ESTÁS TÚ
   ├─ GET /api/appointments/attention/queue
   │  └─ Filtrar: estado_cita='PROGRAMADA' + estado_administrativo='PAGO_VALIDADO'
   ├─ Mostrar en cola (SOLO las citas con pago validado)
   ├─ POST /api/appointments/attention/open
   │  └─ Cambiar estado a EN_CURSO
   └─ PATCH /api/appointments/attention/{id}/close
      └─ Cambiar estado a ATENDIDA + registrar evaluación
```

---

## 📚 Documentación Completa

Se han creado 3 documentos en el repositorio:

### 1. `DIAGNÓSTICO_CITAS.md`
Documento técnico detallado con:
- Cómo funciona la cola de pacientes
- Criterios para que aparezca una cita
- Dónde se persiste el pago
- Cómo se validan los pagos
- Posibles razones de problemas
- Cómo resolver cada caso

### 2. `GUÍA_CREAR_CITAS.md`
Guía práctica con:
- Cómo obtener IDs necesarios
- Ejemplos de curl para cada tipo de pago
- Script PowerShell para crear 5 citas de prueba
- Tabla de resumen
- Cómo obtener JWT token

### 3. `check_appointments.ps1`
Script PowerShell que muestra:
- Resumen de citas por estado
- Citas disponibles para atención
- Citas con pago pendiente
- Doctores registrados
- Pacientes registrados

---

## ❓ Preguntas Frecuentes

### P1: ¿Las citas del triage aparecen automáticamente en la cola?
**R:** No. El triage solo crea signos vitales. Después necesitas crear una cita médica (CU04).

### P2: ¿Dónde se guarda el pago? ¿Hay una tabla de transacciones?
**R:** El estado del pago se guarda en `cita_medica.estado_administrativo`. No hay tabla de transacciones persistida (es simulado), solo el estado en la cita.

### P3: ¿Cómo aparecen pacientes del triage en la cola?
**R:** El triaje crea un registro de signos vitales. Para que aparezca en la cola de atención:
1. Médico crea una cita para el paciente
2. El pago debe ser validado
3. Entonces aparece en `/doctor/appointments/attention`

### P4: ¿Puedo ver el historial de pagos rechazados?
**R:** Sí, puedes verlos con:
```sql
SELECT * FROM cita_medica 
WHERE estado_administrativo = 'PAGO_PENDIENTE'
  AND is_active = true;
```

### P5: ¿Cómo autorizo manualmente un pago rechazado?
**R:** Actualmente no hay endpoint para esto. Necesitarías:
1. Actualizar directamente en la BD
2. O crear una nueva cita con datos de pago válidos

---

## 🎯 Próximos Pasos Recomendados

1. ✅ **Ejecutar `check_appointments.ps1`** para ver el estado actual
2. ✅ **Si no hay citas**: Crear algunas vía API (ver ejemplos en GUÍA_CREAR_CITAS.md)
3. ✅ **Si hay citas pero no aparecen**: Revisar `estado_administrativo` en BD
4. ✅ **Verificar en frontend**: `http://localhost:5173/doctor/appointments/attention`

---

**Documento generado:** 2026-05-21  
**Última actualización:** 2026-05-21


