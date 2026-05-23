# ✅ CHECKLIST DE VERIFICACIÓN - Citas en Cola de Atención (CU06)

##  Objetivo
Verificar por qué no aparecen pacientes en `http://localhost:5173/doctor/appointments/attention` y resolverlo.

---

##  FASE 1: VERIFICACIÓN INICIAL

### 1️⃣ Verificar que el Backend está corriendo

- [ ] Backend running en `http://localhost:8080`
- [ ] Ver logs de Spring Boot (no hay errores)
- [ ] PostgreSQL está conectado
- [ ] Base de datos `his` existe

```bash
# Verificar backend
curl -X GET http://localhost:8080/api/catalogs/specialties
# Debe responder con lista de especialidades (puede requerir token)
```

### 2️⃣ Verificar que el Frontend está corriendo

- [ ] Frontend running en `http://localhost:5173`
- [ ] Página carga sin errores en consola
- [ ] Puedes hacer login como doctor

### 3️⃣ Verificar datos en PostgreSQL

```sql
-- Conectar a PostgreSQL
psql -U postgres -d his

-- Contar registros
SELECT COUNT(*) as pacientes FROM paciente;
SELECT COUNT(*) as doctores FROM personal_hospitalario WHERE rol = 'DOCTOR';
SELECT COUNT(*) as citas FROM cita_medica;

-- Verificar estructura de tabla cita_medica
\d cita_medica
```

**Checklist:**
- [ ] Existe tabla `cita_medica`
- [ ] Existen pacientes en la BD
- [ ] Existen doctores en la BD
- [ ] Campos presentes: `estado_cita`, `estado_administrativo`

---

##  FASE 2: DIAGNÓSTICO DE CITAS

### Query 1: Ver todas las citas

```sql
SELECT 
  cita_medica_id,
  paciente_id,
  personal_id,
  estado_cita,
  estado_administrativo,
  observacion_administrativa,
  fecha_cita,
  hora_cita
FROM cita_medica
WHERE is_active = true
ORDER BY fecha_cita DESC;
```

**Checklist:**
- [ ] ¿Hay citas en la BD?
  - [ ] SÍ → Continuar con Query 2
  - [ ] NO → Ver FASE 3: CREAR CITAS

### Query 2: Ver citas con estado PAGO_VALIDADO

```sql
SELECT 
  cita_medica_id,
  paciente_id,
  personal_id,
  estado_cita,
  estado_administrativo,
  fecha_cita,
  hora_cita
FROM cita_medica
WHERE estado_administrativo = 'PAGO_VALIDADO'
  AND is_active = true
ORDER BY fecha_cita ASC;
```

**Checklist:**
- [ ] ¿Hay citas con PAGO_VALIDADO?
  - [ ] SÍ (una o más) → Continuar con Query 3
  - [ ] NO → Todas tienen PAGO_PENDIENTE → Ver FASE 4

### Query 3: Ver citas disponibles en la cola del doctor actual

```sql
-- Reemplazar 1 con el personal_id del doctor autenticado
SELECT 
  cm.cita_medica_id,
  p.nombre_completo as paciente,
  hs.nombre_completo as doctor,
  cm.estado_cita,
  cm.estado_administrativo,
  cm.fecha_cita,
  cm.hora_cita
FROM cita_medica cm
JOIN paciente p ON cm.paciente_id = p.paciente_id
JOIN personal_hospitalario hs ON cm.personal_id = hs.personal_id
WHERE cm.personal_id = 1  -- ← Cambiar por el ID del doctor
  AND cm.estado_cita = 'PROGRAMADA'
  AND cm.estado_administrativo = 'PAGO_VALIDADO'
  AND cm.is_active = true
ORDER BY cm.fecha_cita ASC;
```

**Checklist:**
- [ ] ¿Aparecen citas en esta query?
  - [ ] SÍ → El backend debería mostrarlas. Ver FASE 5
  - [ ] NO → El personal_id puede ser incorrecto. Ver FASE 4

### Query 4: Ver detalles del doctor

```sql
-- Ver ID del doctor
SELECT 
  personal_id,
  nombre_completo,
  rol,
  usuario_id
FROM personal_hospitalario
WHERE rol = 'DOCTOR';

-- Ver usuario del doctor
SELECT 
  usuario_id,
  email,
  rol
FROM usuario
WHERE email = 'doctor@example.com';  -- Tu email de doctor
```

**Checklist:**
- [ ] ¿Cuál es el `personal_id` del doctor autenticado?
  - [ ] Anotarlo: `personal_id = ___`
- [ ] ¿Cuál es su email?
  - [ ] Anotarlo: `email = ___`

---

##  FASE 3: CREAR CITAS DE PRUEBA

Si no hay citas, o todas tienen `PAGO_PENDIENTE`, crear citas nuevas.

### Obtener el JWT Token

1. **Opción A: Desde el frontend**
   ```javascript
   // Abre DevTools (F12) → Console
   sessionStorage.getItem('token')
   // Copia el valor
   ```

2. **Opción B: Desde Postman/curl**
   ```bash
   curl -X POST http://localhost:8080/api/auth/authenticate \
     -H "Content-Type: application/json" \
     -d '{
       "email": "doctor@example.com",
       "password": "password"
     }'
   # Copia el "token" de la respuesta
   ```

**Checklist:**
- [ ] Token obtenido: `token = eyJ...`

### Crear Cita 1: PAGO APROBADO (Tarjeta)

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
    "nombreTitularTarjeta": "Test",
    "cvc": "123"
  }'
```

**Checklist:**
- [ ] Respuesta: 201 Created
- [ ] `estadoAdministrativo`: `"PAGO_VALIDADO"` ✅
- [ ] `pagoValidado`: `true`
- [ ] Anotар `citaMedicaId`: ___

### Crear Cita 2: PAGO APROBADO (Seguro)

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

**Checklist:**
- [ ] Respuesta: 201 Created
- [ ] `estadoAdministrativo`: `"PAGO_VALIDADO"` ✅
- [ ] `pagoValidado`: `true`

### Crear Cita 3: PAGO RECHAZADO (Tarjeta)

```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "pacienteId": 1,
    "medicoPersonalId": 1,
    "especialidadId": 1,
    "fechaCita": "2026-05-25",
    "horaCita": "11:00",
    "motivoConsulta": "Control",
    "metodoPago": "TARJETA",
    "bancoTarjeta": "Banco Industrial",
    "numeroTarjeta": "4111111111110000",
    "fechaVencimientoTarjeta": "12/29",
    "nombreTitularTarjeta": "Test",
    "cvc": "123"
  }'
```

**Checklist:**
- [ ] Respuesta: 201 Created
- [ ] `estadoAdministrativo`: `"PAGO_PENDIENTE"` ❌
- [ ] `pagoValidado`: `false`
- [ ] `mensajeValidacion`: Contiene "saldo insuficiente"

---

##  FASE 4: VERIFICAR DESDE EL FRONTEND

### En el navegador

1. **Ir a**: `http://localhost:5173`
2. **Login como doctor**
   - Email: `doctor@example.com`
   - Password: (tu password)
3. **Navegar a**: `/doctor/appointments/attention`
4. **Verificar cola**

**Checklist:**
- [ ] Página carga sin errores
- [ ] Sección "Cola de pacientes" visible
- [ ] ¿Aparecen pacientes?
  - [ ] SÍ, aparecen 2+ → ✅ LISTO (Ir a FASE 6)
  - [ ] NO, dice "No hay pacientes pendientes" → Ver FASE 5

### Si no aparecen pacientes:

Abre DevTools (F12) → Console y verifica:

```javascript
// Ver errores de red
// Chequear que la API responde correctamente
```

**Checklist:**
- [ ] ¿Hay errores en la consola?
  - [ ] SÍ → Anotarlos y reportar
  - [ ] NO → Continuar

---

##  FASE 5: DEBUGGING

### Habilitar logs del Backend

En `application.properties`:

```properties
logging.level.his.application.services=DEBUG
logging.level.his.infrastructure.persistence=DEBUG
```

Reinicia el backend y ejecuta nuevamente.

**Checklist:**
- [ ] ¿Hay logs de debug?
  - [ ] Buscar mensajes con "CU06" o "getPatientQueue"
  - [ ] ¿Qué retorna la query?

### Query de Debugging Final

```sql
-- Ver exactamente qué hace el backend
SELECT 
  cm.cita_medica_id,
  cm.personal_id,
  cm.estado_cita,
  cm.estado_administrativo,
  cm.is_active
FROM cita_medica cm
WHERE cm.personal_id = 1
  AND cm.estado_cita = 'PROGRAMADA'
  AND cm.estado_administrativo = 'PAGO_VALIDADO'
  AND cm.is_active = true
ORDER BY cm.fecha_cita ASC, cm.hora_cita ASC;
```

**Checklist:**
- [ ] ¿Qué retorna esta query?
  - [ ] 0 filas → Las citas no cumplen criterios. Revisar FASE 3
  - [ ] 1+ filas → Backend debería mostrarlas. Ver logs

---

## ✅ FASE 6: VERIFICACIÓN FINAL

### Criterios de éxito

- [ ] ✅ En BD: Existen citas con `PAGO_VALIDADO`
- [ ] ✅ En BD: Citas tienen `estado_cita = 'PROGRAMADA'`
- [ ] ✅ En BD: Citas están asignadas al doctor actual
- [ ] ✅ En API: GET `/api/appointments/attention/queue` retorna citas
- [ ] ✅ En Frontend: Aparecen en la cola
- [ ] ✅ Usuario puede hacer clic en "Iniciar atención"
- [ ] ✅ Abre la sección de atención en curso

### Funcionalidad de Atención

- [ ] Clic en "Iniciar atención" cambia estado a `EN_CURSO`
- [ ] Aparece el formulario de evaluación y diagnóstico
- [ ] Puede llenar y cerrar la atención
- [ ] Estado cambia a `ATENDIDA`
- [ ] Cita desaparece de la cola

---

##  SI ALGO FALLA

### Información a recopilar

1. **Logs del Backend**
   ```bash
   # Copiar los últimos 50 líneas de logs
   tail -50 nohup.out
   ```

2. **Query de Diagnóstico**
   ```sql
   SELECT * FROM cita_medica WHERE is_active = true;
   SELECT * FROM personal_hospitalario WHERE rol = 'DOCTOR';
   SELECT * FROM usuario WHERE rol = 'DOCTOR';
   ```

3. **Error en Frontend**
   ```javascript
   // DevTools → Console → Copy full error
   ```

4. **Información del usuario**
   - Email usado para login: ___
   - Nombre del doctor: ___
   - `personal_id` del doctor: ___

---

##  RESUMEN RÁPIDO

| Problema | Solución |
|----------|----------|
| **No hay citas en BD** | Crear citas vía API (FASE 3) |
| **Citas con PAGO_PENDIENTE** | Crear citas con datos válidos (no tarjeta 0000) |
| **Citas de otro doctor** | Verificar `personal_id` coincida |
| **Citas no son PROGRAMADA** | Solo aparecen las que son PROGRAMADA |
| **Frontend no muestra** | Verificar API retorna datos (DevTools) |
| **Errores en logs** | Revisar logs del backend |

---

**Documento:** Checklist CU06 - Atención Médica  
**Última actualización:** 2026-05-21  
**Estado:** ✅ Listo para usar
