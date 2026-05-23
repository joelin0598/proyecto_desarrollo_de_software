#  Diagnóstico: ¿Por qué no aparecen pacientes para atender?

##  Problema
En la URL `http://localhost:5173/doctor/appointments/attention`, **no aparecen pacientes para que el médico atienda**.

---

##  Análisis Técnico

### 1. **¿Cómo funciona la cola de pacientes?** (CU06)

El frontend llamaa `GET /api/appointments/attention/queue` que es manejado por:
- **Controlador**: `AppointmentAttentionController` (línea 35-39)
- **Servicio**: `MedicalAppointmentAttentionService.getPatientQueue()` (línea 45-52)

```java
// El servicio filtra las citas por:
appointmentRepository.findPendingQueueByDoctor(doctor.getPersonalId())
```

---

### 2. **¿Qué criterios debe cumplir una cita para aparecer?**

En `SqlMedicalAppointmentRepository.findPendingQueueByDoctor()` (línea 68-77):

```java
medicalAppointmentJpaRepository.findByPersonalPersonalIdAndEstadoCitaAndEstadoAdministrativoAndIsActiveTrueOrderByFechaCitaAscHoraCitaAsc(
    personalId,
    StatusAppointment.PROGRAMADA,           // ✅ Estado debe ser PROGRAMADA
    AdministrativeAppointmentStatus.PAGO_VALIDADO  // ✅ Pago debe estar VALIDADO
)
```

**Una cita aparecerá en la cola SOLO SI:**
1. ✅ `estado_cita = 'PROGRAMADA'`
2. ✅ `estado_administrativo = 'PAGO_VALIDADO'`
3. ✅ `is_active = true`
4. ✅ `personal_id = [ID del médico autenticado]`

---

### 3. **¿Dónde se persiste la información de pago?**

#### Base de Datos (`cita_medica` table)

```
CREATE TABLE cita_medica (
    cita_medica_id          BIGSERIAL PRIMARY KEY,
    paciente_id             BIGINT NOT NULL,
    personal_id             BIGINT NOT NULL,
    especialidad_id         BIGINT,
    fecha_cita              DATE NOT NULL,
    hora_cita               TIME NOT NULL,
    motivo_consulta         VARCHAR(500) NOT NULL,
    metodo_pago             VARCHAR(255) NOT NULL,    -- TARJETA o SEGURO
    costo_consulta          DOUBLE PRECISION NOT NULL, -- 175.00
    estado_cita             VARCHAR(255) NOT NULL,    -- PROGRAMADA, EN_CURSO, CANCELADA, ATENDIDA
    estado_administrativo   VARCHAR(255) NOT NULL,    -- PAGO_VALIDADO o PAGO_PENDIENTE
    observacion_administrativa VARCHAR(300),          -- Mensaje de validación
    is_active              BOOLEAN DEFAULT TRUE,
    created_at             TIMESTAMP,
    updated_at             TIMESTAMP
);
```

**Campos relevantes al pago:**
- `metodo_pago`: TARJETA o SEGURO (tipo de pago)
- `estado_administrativo`: **PAGO_VALIDADO** o PAGO_PENDIENTE (decisivo)
- `observacion_administrativa`: Mensaje de por qué fue rechazado

---

### 4. **¿Cómo se valida y establece el pago?**

En `AppointmentService.scheduleAppointment()` (línea 58-74):

```java
PaymentValidationResult paymentValidation = validateAndSimulatePayment(request);

MedicalAppointment appointment = MedicalAppointment.builder()
    // ...
    .estadoAdministrativo(paymentValidation.approved
        ? AdministrativeAppointmentStatus.PAGO_VALIDADO    // ✅ Aprobado
        : AdministrativeAppointmentStatus.PAGO_PENDIENTE)   // ❌ Rechazado
    .observacionAdministrativa(paymentValidation.message)
    .build();
```

---

### 5. **¿Cómo se validan los pagos?**

####  Pago con TARJETA (línea 166-187)

**Validaciones requeridas:**
- Banco, número, vencimiento, titular, CVC deben estar presentes
- Fecha de vencimiento no puede estar expirada

**Lógica de aprobación (simulada):**
```java
boolean approved = !request.getNumeroTarjeta().trim().endsWith("0000");
```
- ❌ **Rechazada** si termina en **0000** (simula saldo insuficiente)
- ✅ **Aprobada** en otros casos

####  Pago con SEGURO (línea 189-205)

**Validaciones requeridas:**
- `aseguradoraId` debe ser válido
- `numeroPoliza` debe estar presente

**Lógica de aprobación (simulada):**
```java
boolean approved = !(policy.startsWith("X") || policy.contains("RECHAZADA"));
```
- ❌ **Rechazada** si póliza empieza con **X** o contiene **RECHAZADA**
- ✅ **Aprobada** en otros casos

---

## ❓ Posibles Razones por las que NO aparecen pacientes

### **Causa 1: Las citas tienen estado `PAGO_PENDIENTE`**

**¿Cómo verificar?**
```sql
SELECT cita_medica_id, paciente_id, personal_id, estado_cita, estado_administrativo, observacion_administrativa
FROM cita_medica
WHERE estado_cita = 'PROGRAMADA'
ORDER BY created_at DESC;
```

**¿Por qué pasaría?**
- La tarjeta usada termina en **0000** (simulación de saldo insuficiente)
- La póliza de seguro empieza con **X** o contiene **RECHAZADA**
- Los datos de pago son incompletos o inválidos

**Solución:**
- Crear citas con datos de pago válidos:
  - Tarjeta: no terminar en 0000
  - Seguro: no empezar con X, no contener "RECHAZADA"

---

### **Causa 2: Las citas no tienen el médico asignado correcto**

**¿Cómo verificar?**
```sql
SELECT hs.nombre_completo, cm.cita_medica_id, cm.estado_cita, cm.estado_administrativo
FROM cita_medica cm
JOIN personal_hospitalario hs ON cm.personal_id = hs.personal_id
WHERE cm.estado_cita = 'PROGRAMADA'
  AND cm.estado_administrativo = 'PAGO_VALIDADO'
ORDER BY cm.fecha_cita ASC;
```

**¿Por qué pasaría?**
- El doctor autenticado tiene un `personal_id` diferente al de las citas
- Las citas están asignadas a otro médico

**Solución:**
- Asegurar que al crear citas, se use el `personal_id` del médico correcto

---

### **Causa 3: Las citas no son `PROGRAMADA` (ya fueron atendidas)**

**¿Cómo verificar?**
```sql
SELECT estado_cita, COUNT(*) as cantidad
FROM cita_medica
GROUP BY estado_cita
ORDER BY cantidad DESC;
```

**¿Por qué pasaría?**
- Las citas ya fueron atendidas (estado = `ATENDIDA`)
- Las citas fueron canceladas (estado = `CANCELADA`)

---

### **Causa 4: No hay datos de prueba en la BD**

**¿Cómo verificar?**
```sql
SELECT COUNT(*) FROM cita_medica;
SELECT COUNT(*) FROM personal_hospitalario WHERE rol = 'DOCTOR';
SELECT COUNT(*) FROM paciente;
```

**Solución:**
- Ejecutar el script `V3__insert_test_data_for_cu06.sql` manualmente
- O crear citas a través de la API POST `/api/appointments`

---

##  Cómo Resolver

### **Paso 1: Verificar la BD**
```sql
-- Conectarse a PostgreSQL
psql -U postgres -d his

-- Ver el estado de las citas
SELECT cita_medica_id, paciente_id, personal_id, fecha_cita, 
       estado_cita, estado_administrativo
FROM cita_medica
WHERE estado_cita = 'PROGRAMADA'
ORDER BY fecha_cita ASC;
```

### **Paso 2: Si no hay citas, crearlas vía API**

**Endpoint**: `POST /api/appointments`

**Payload ejemplo (pago con tarjeta - APROBADO):**
```json
{
  "pacienteId": 1,
  "medicoPersonalId": 1,
  "especialidadId": 1,
  "fechaCita": "2026-05-25",
  "horaCita": "09:00",
  "motivoConsulta": "Control general",
  "metodoPago": "TARJETA",
  "bancoTarjeta": "Banco",
  "numeroTarjeta": "4111111111111111",
  "fechaVencimientoTarjeta": "12/29",
  "nombreTitularTarjeta": "Test User",
  "cvc": "123"
}
```

**Payload ejemplo (pago con seguro - APROBADO):**
```json
{
  "pacienteId": 1,
  "medicoPersonalId": 1,
  "especialidadId": 1,
  "fechaCita": "2026-05-25",
  "horaCita": "10:00",
  "motivoConsulta": "Control general",
  "metodoPago": "SEGURO",
  "aseguradoraId": 1,
  "numeroPoliza": "POL123456"
}
```

### **Paso 3: Verificar desde el frontend**
1. Ir a `http://localhost:5173/doctor/appointments/attention`
2. Las citas con `PAGO_VALIDADO` deberían aparecer en la cola

---

##  Flujo Completo de una Cita

```
1. TRIAGE (CU2.0) - Paciente entra de urgencia
   └─ Se registran signos vitales y prioridad
   └─ NO crea cita automáticamente

2. APPOINTMENT SCHEDULING (CU04) - Se agenda una cita
   ├─ POST /api/appointments
   ├─ Valida pago (tarjeta o seguro)
   ├─ Si pago ✅ APROBADO  → estado_administrativo = PAGO_VALIDADO
   └─ Si pago ❌ RECHAZADO → estado_administrativo = PAGO_PENDIENTE

3. APPOINTMENT ATTENTION (CU06) - Doctor atiende citas
   ├─ GET /api/appointments/attention/queue
   │  └─ Filtra: estado_cita='PROGRAMADA' + estado_administrativo='PAGO_VALIDADO'
   ├─ POST /api/appointments/attention/open (abre la atención)
   │  └─ Cambia estado a EN_CURSO
   └─ PATCH /api/appointments/attention/{id}/close (cierra la atención)
      └─ Cambia estado a ATENDIDA + registra evaluación y diagnóstico
```

---

##  Resumen: Dónde se persiste el pago

| Aspecto | Ubicación |
|---------|----------|
| **Tabla** | `cita_medica` |
| **Columna estado** | `estado_administrativo` |
| **Valores** | `PAGO_VALIDADO` \| `PAGO_PENDIENTE` |
| **Mensaje** | `observacion_administrativa` |
| **Lógica de validación** | `AppointmentService.validateAndSimulatePayment()` |
| **Persistencia** | `SqlMedicalAppointmentRepository.findPendingQueueByDoctor()` |

---

##  Siguientes Pasos

1. **Verificar BD**: Ejecutar queries SQL para ver qué citas existen
2. **Si no hay citas**: Crear datos de prueba vía API
3. **Si hay citas pero no aparecen**: Revisar el `estado_administrativo`
4. **Si todo está bien**: Revisar logs del backend para errores

