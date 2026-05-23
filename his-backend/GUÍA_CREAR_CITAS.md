#  Cómo Crear Citas de Prueba para CU06

## Requisitos Previos

1. ✅ Backend running en `http://localhost:8080`
2. ✅ Frontend running en `http://localhost:5173`
3. ✅ PostgreSQL con base de datos `his`
4. ✅ Datos de base ejecutados (pacientes, doctores, especialidades)

---

## 1️⃣ Obtener IDs Necesarios

Primero, necesitas conocer:
- `pacienteId` - ID del paciente
- `medicoPersonalId` - ID del personal (doctor)
- `especialidadId` - ID de la especialidad (opcional)

### Opción A: Desde la BD (PostgreSQL)

```sql
-- Ver pacientes
SELECT paciente_id, nombre_completo, dpi FROM paciente LIMIT 5;

-- Ver doctores
SELECT personal_id, nombre_completo, rol FROM personal_hospitalario WHERE rol = 'DOCTOR';

-- Ver especialidades
SELECT especialidad_medica_id, nombre FROM especialidad_medica LIMIT 5;
```

### Opción B: Desde la API

```bash
# Listar pacientes (requiere autenticación de admin)
curl -X GET http://localhost:8080/api/catalogs/patient-genders \
  -H "Authorization: Bearer YOUR_TOKEN"

# Listar doctores por especialidad
curl -X GET http://localhost:8080/api/catalogs/doctors \
  -H "Authorization: Bearer YOUR_TOKEN"

# Listar especialidades
curl -X GET http://localhost:8080/api/catalogs/specialties \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 2️⃣ Crear Cita con Pago APROBADO (Tarjeta)

### ✅ Tarjeta válida (terminada diferente a 0000)

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
    "motivoConsulta": "Control general de salud",
    "metodoPago": "TARJETA",
    "bancoTarjeta": "Banco Industrial",
    "numeroTarjeta": "4111111111111111",
    "fechaVencimientoTarjeta": "12/29",
    "nombreTitularTarjeta": "Juan Pérez",
    "cvc": "123"
  }'
```

**Respuesta esperada (201 Created):**
```json
{
  "citaMedicaId": 1,
  "pacienteId": 1,
  "pacienteNombre": "Paciente Test",
  "medicoPersonalId": 1,
  "medicoNombre": "Dr. Test",
  "fechaCita": "2026-05-25",
  "horaCita": "09:00",
  "motivoConsulta": "Control general de salud",
  "metodoPago": "TARJETA",
  "costoConsulta": 175.0,
  "estadoCita": "PROGRAMADA",
  "estadoAdministrativo": "PAGO_VALIDADO",  ✅ VALIDADO!
  "pagoValidado": true,
  "mensajeValidacion": "Pago con tarjeta validado correctamente"
}
```

---

## 3️⃣ Crear Cita con Pago RECHAZADO (Tarjeta)

### ❌ Tarjeta RECHAZADA (termina en 0000)

```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "pacienteId": 1,
    "medicoPersonalId": 1,
    "especialidadId": 1,
    "fechaCita": "2026-05-26",
    "horaCita": "10:00",
    "motivoConsulta": "Control general",
    "metodoPago": "TARJETA",
    "bancoTarjeta": "Banco Industrial",
    "numeroTarjeta": "4111111111110000",
    "fechaVencimientoTarjeta": "12/29",
    "nombreTitularTarjeta": "Juan Pérez",
    "cvc": "123"
  }'
```

**Respuesta esperada (201 Created):**
```json
{
  "citaMedicaId": 2,
  "estadoCita": "PROGRAMADA",
  "estadoAdministrativo": "PAGO_PENDIENTE",  ❌ PENDIENTE!
  "pagoValidado": false,
  "mensajeValidacion": "Pago con tarjeta pendiente: simulacion de saldo insuficiente"
}
```

---

## 4️⃣ Crear Cita con Pago APROBADO (Seguro)

### ✅ Póliza válida (no empieza con X, sin RECHAZADA)

```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "pacienteId": 1,
    "medicoPersonalId": 1,
    "especialidadId": 1,
    "fechaCita": "2026-05-27",
    "horaCita": "11:00",
    "motivoConsulta": "Consulta por seguro",
    "metodoPago": "SEGURO",
    "aseguradoraId": 1,
    "numeroPoliza": "POL-2024-001234"
  }'
```

**Respuesta esperada:**
```json
{
  "citaMedicaId": 3,
  "estadoCita": "PROGRAMADA",
  "estadoAdministrativo": "PAGO_VALIDADO",  ✅ VALIDADO!
  "pagoValidado": true,
  "metodoPago": "SEGURO",
  "mensajeValidacion": "Cobertura de seguro validada correctamente"
}
```

---

## 5️⃣ Crear Cita con Pago RECHAZADO (Seguro)

### ❌ Póliza RECHAZADA (empieza con X o contiene RECHAZADA)

```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "pacienteId": 1,
    "medicoPersonalId": 1,
    "especialidadId": 1,
    "fechaCita": "2026-05-28",
    "horaCita": "12:00",
    "motivoConsulta": "Consulta por seguro rechazado",
    "metodoPago": "SEGURO",
    "aseguradoraId": 1,
    "numeroPoliza": "XPOL123456"
  }'
```

**Respuesta esperada:**
```json
{
  "citaMedicaId": 4,
  "estadoCita": "PROGRAMADA",
  "estadoAdministrativo": "PAGO_PENDIENTE",  ❌ PENDIENTE!
  "pagoValidado": false,
  "mensajeValidacion": "Cobertura pendiente: simulacion de poliza no vigente/no cubierta"
}
```

---

##  Script PowerShell para crear 5 citas de prueba

Crea un archivo `create_appointments.ps1`:

```powershell
# Variables de configuración
$apiUrl = "http://localhost:8080/api/appointments"
$token = "YOUR_JWT_TOKEN"  # Obtener del login

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Datos de base
$pacienteId = 1
$medicoId = 1
$especialidadId = 1

# Función para crear cita
function Create-Appointment {
    param(
        [string]$FechaCita,
        [string]$HoraCita,
        [string]$Motivo,
        [object]$Pago
    )
    
    $body = @{
        pacienteId = $pacienteId
        medicoPersonalId = $medicoId
        especialidadId = $especialidadId
        fechaCita = $FechaCita
        horaCita = $HoraCita
        motivoConsulta = $Motivo
        metodoPago = $Pago.metodoPago
    } + $Pago.detalles
    
    $response = Invoke-RestMethod -Uri $apiUrl -Method Post -Headers $headers -Body ($body | ConvertTo-Json)
    return $response
}

# Cita 1: Tarjeta válida (APROBADO)
Write-Host "Creando cita 1: Tarjeta válida..." -ForegroundColor Green
$result1 = Create-Appointment -FechaCita "2026-05-25" -HoraCita "09:00" -Motivo "Control general" -Pago @{
    metodoPago = "TARJETA"
    detalles = @{
        bancoTarjeta = "Banco Industrial"
        numeroTarjeta = "4111111111111111"
        fechaVencimientoTarjeta = "12/29"
        nombreTitularTarjeta = "Test User"
        cvc = "123"
    }
}
Write-Host "✅ Cita 1 creada: ID=$($result1.citaMedicaId), Pago=$($result1.estadoAdministrativo)" -ForegroundColor Green

# Cita 2: Tarjeta rechazada (RECHAZADO)
Write-Host "Creando cita 2: Tarjeta rechazada..." -ForegroundColor Yellow
$result2 = Create-Appointment -FechaCita "2026-05-25" -HoraCita "10:00" -Motivo "Control checkup" -Pago @{
    metodoPago = "TARJETA"
    detalles = @{
        bancoTarjeta = "Banco Industrial"
        numeroTarjeta = "4111111111110000"
        fechaVencimientoTarjeta = "12/29"
        nombreTitularTarjeta = "Test User"
        cvc = "123"
    }
}
Write-Host "⏳ Cita 2 creada: ID=$($result2.citaMedicaId), Pago=$($result2.estadoAdministrativo)" -ForegroundColor Yellow

# Cita 3: Seguro válido (APROBADO)
Write-Host "Creando cita 3: Seguro válido..." -ForegroundColor Green
$result3 = Create-Appointment -FechaCita "2026-05-25" -HoraCita "11:00" -Motivo "Control por seguro" -Pago @{
    metodoPago = "SEGURO"
    detalles = @{
        aseguradoraId = 1
        numeroPoliza = "POL2024001234"
    }
}
Write-Host "✅ Cita 3 creada: ID=$($result3.citaMedicaId), Pago=$($result3.estadoAdministrativo)" -ForegroundColor Green

# Cita 4: Seguro rechazado (RECHAZADO)
Write-Host "Creando cita 4: Seguro rechazado..." -ForegroundColor Yellow
$result4 = Create-Appointment -FechaCita "2026-05-25" -HoraCita "13:00" -Motivo "Control seguro rechazado" -Pago @{
    metodoPago = "SEGURO"
    detalles = @{
        aseguradoraId = 1
        numeroPoliza = "XPOL123456"
    }
}
Write-Host "⏳ Cita 4 creada: ID=$($result4.citaMedicaId), Pago=$($result4.estadoAdministrativo)" -ForegroundColor Yellow

# Cita 5: Tarjeta válida (APROBADO)
Write-Host "Creando cita 5: Otra tarjeta válida..." -ForegroundColor Green
$result5 = Create-Appointment -FechaCita "2026-05-26" -HoraCita "09:00" -Motivo "Seguimiento" -Pago @{
    metodoPago = "TARJETA"
    detalles = @{
        bancoTarjeta = "Banco Industrial"
        numeroTarjeta = "5500005500005500"
        fechaVencimientoTarjeta = "06/30"
        nombreTitularTarjeta = "Test User"
        cvc = "456"
    }
}
Write-Host "✅ Cita 5 creada: ID=$($result5.citaMedicaId), Pago=$($result5.estadoAdministrativo)" -ForegroundColor Green

Write-Host "`n✅ Todas las citas creadas!" -ForegroundColor Green
Write-Host "Esperadas en la cola de atención (pago validado): 3 citas"
```

---

##  Resumen

| # | Fecha | Hora | Tarjeta/Póliza | Estado Pago | ✅ Aparecerá en cola? |
|---|-------|------|----------------|-------------|----------------------|
| 1 | 2026-05-25 | 09:00 | 4111111111111111 | VALIDADO | ✅ Sí |
| 2 | 2026-05-25 | 10:00 | 4111111111110000 | PENDIENTE | ❌ No |
| 3 | 2026-05-25 | 11:00 | POL2024001234 | VALIDADO | ✅ Sí |
| 4 | 2026-05-25 | 13:00 | XPOL123456 | PENDIENTE | ❌ No |
| 5 | 2026-05-26 | 09:00 | 5500005500005500 | VALIDADO | ✅ Sí |

---

##  Cómo obtener el JWT token

### Opción 1: Login con Postman

```bash
POST http://localhost:8080/api/auth/authenticate
Content-Type: application/json

{
  "email": "doctor@example.com",
  "password": "password123"
}
```

La respuesta contendrá:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": 1,
    "email": "doctor@example.com",
    "firstName": "Dr",
    "lastName": "Test",
    "role": "DOCTOR"
  }
}
```

### Opción 2: Copiar desde el navegador

1. Login en `http://localhost:5173`
2. Abre DevTools (F12)
3. En la consola, ejecuta:
   ```javascript
   sessionStorage.getItem('token')
   ```

---

## ✅ Verificar que las citas aparecen

1. Login como doctor en `http://localhost:5173`
2. Ve a `/doctor/appointments/attention`
3. Deberías ver 3 citas (las que tienen `PAGO_VALIDADO`)
4. Haz clic en "Iniciar atención" para una de ellas

