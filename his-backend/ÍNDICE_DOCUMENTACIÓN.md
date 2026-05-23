#  Documentación: Sistema de Atención Médica (CU06) y Persistencia de Pagos

##  Tu Pregunta Original

> **Mi duda es porque aquí no aparecen pacientes para atender**, que deberían de ser los de consultas agendadas y los que entraron con triage, **ambos ya validados el pago que si lo realizaron**.
>
> **En dónde se guarda o se persiste la información de pago?**

---

## ✅ Respuesta Directa

### ¿Dónde se guarda el pago?

**PostgreSQL → Tabla `cita_medica` → Campo `estado_administrativo`**

```sql
-- Aquí se persiste el estado del pago
ALTER TABLE cita_medica ADD COLUMN estado_administrativo VARCHAR(50) NOT NULL;
-- Valores: 'PAGO_VALIDADO' o 'PAGO_PENDIENTE'

-- También hay un campo para el mensaje
ALTER TABLE cita_medica ADD COLUMN observacion_administrativa VARCHAR(300);
-- Contiene el motivo del rechazo (ej: "saldo insuficiente")
```

### ¿Por qué no aparecen pacientes?

Hay 3 razones posibles:

1. **Las citas no existen en la BD** → Crear vía API
2. **Las citas tienen `PAGO_PENDIENTE`** → El pago fue rechazado
3. **Las citas no son `PROGRAMADA`** → Ya fueron atendidas o canceladas

---

##  Documentos Creados

### 1.  **RESPUESTA_PREGUNTA_PAGO.md** ← ⭐ EMPIEZA AQUÍ

**Contenido:**
- Respuesta directa a tu pregunta
- Dónde se persiste el pago (arquitectura general)
- Cómo funciona la cola de pacientes
- Validación de pagos (tarjeta vs seguro)
- Posibles razones del problema
- Soluciones paso a paso
- Preguntas frecuentes

**Cuándo leerlo:** Si quieres una respuesta rápida y clara

---

### 2.  **DIAGNÓSTICO_CITAS.md**

**Contenido:**
- Análisis técnico detallado
- Cómo funciona CU06 en profundidad
- Criterios para que aparezca una cita
- Dónde se persiste cada información
- Validación de pagos (código fuente explicado)
- 4 posibles causas del problema con soluciones
- Queries SQL para verificar cada causa

**Cuándo leerlo:** Si necesitas entender en detalle cómo funciona el sistema

---

### 3.  **GUÍA_CREAR_CITAS.md**

**Contenido:**
- Cómo obtener IDs necesarios (paciente, doctor, especialidad)
- Ejemplos de curl para crear citas
- Ejemplos con pago APROBADO (tarjeta y seguro)
- Ejemplos con pago RECHAZADO
- Script PowerShell para crear 5 citas automáticamente
- Tabla resumen de qué aparece y qué no
- Cómo obtener JWT token

**Cuándo leerlo:** Si no hay citas en la BD y necesitas crearlas

---

### 4.  **DIAGRAMA_VALIDACIÓN_PAGO.md**

**Contenido:**
- Arquitectura general en diagrama ASCII
- Flujo de creación de cita con validación de pago
- Flujo de consulta de cola de atención
- Tabla comparativa (qué aparece, qué no)
- Árboles de decisión para validación
- Mapa mental del flujo completo

**Cuándo leerlo:** Si eres visual y necesitas ver cómo fluyen los datos

---

### 5. ✅ **CHECKLIST_CU06.md**

**Contenido:**
- Checklist paso a paso para verificar el sistema
- Fase 1: Verificación inicial (backend, frontend, BD)
- Fase 2: Diagnóstico de citas (queries)
- Fase 3: Crear citas de prueba
- Fase 4: Verificar desde frontend
- Fase 5: Debugging si algo falla
- Fase 6: Verificación final
- Tabla de solución rápida

**Cuándo leerlo:** Si necesitas hacer un diagnóstico paso a paso

---

### 6.  **check_appointments.ps1**

**Contenido:**
- Script PowerShell que consulta la BD
- Muestra resumen de citas por estado
- Lista citas disponibles para atención
- Lista citas con pago pendiente
- Lista doctores registrados
- Lista pacientes registrados

**Cómo usarlo:**
```bash
.\check_appointments.ps1
```

**Cuándo usarlo:** Si quieres ver rápidamente el estado de la BD

---

##  Cómo Empezar (3 pasos)

### Paso 1: Leer RESPUESTA_PREGUNTA_PAGO.md
- Entiende dónde se guarda el pago
- Entiende por qué no aparecen pacientes
- Elige la solución que te aplica

### Paso 2: Ejecutar check_appointments.ps1
```bash
.\check_appointments.ps1
```
- Ver el estado actual de la BD
- Saber cuántas citas hay
- Saber cuántas tienen pago validado

### Paso 3: Según el resultado...

**Si no hay citas:**
→ Lee GUÍA_CREAR_CITAS.md y crea citas

**Si hay citas pero no aparecen:**
→ Sigue CHECKLIST_CU06.md (Fase 2-5)

**Si necesitas entender en detalle:**
→ Lee DIAGNÓSTICO_CITAS.md

**Si eres visual:**
→ Lee DIAGRAMA_VALIDACIÓN_PAGO.md

---

## ️ Mapa de Decisiones

```
¿No aparecen pacientes en /doctor/appointments/attention?
│
├─ ¿Hay citas en la BD?
│  ├─ NO → Crear citas (GUÍA_CREAR_CITAS.md)
│  │       Ejecutar ejemplos de curl o PowerShell
│  │
│  └─ SÍ → Continuar
│
├─ ¿Las citas tienen estado_cita = 'PROGRAMADA'?
│  ├─ NO → Solo aparecen PROGRAMADA
│  │       Query: SELECT * FROM cita_medica
│  │
│  └─ SÍ → Continuar
│
├─ ¿Las citas tienen estado_administrativo = 'PAGO_VALIDADO'?
│  ├─ NO → Tienen PAGO_PENDIENTE
│  │       Crear nuevas citas con datos válidos
│  │       Pago Rechazado? Ver DIAGNÓSTICO_CITAS.md
│  │
│  └─ SÍ → Continuar
│
├─ ¿Las citas están asignadas al doctor actual?
│  ├─ NO → personal_id no coincide
│  │       Verificar personal_id del doctor autenticado
│  │
│  └─ SÍ → Continuar
│
└─ Si todo es correcto pero no aparecen
   → Seguir CHECKLIST_CU06.md (Fase 5: Debugging)
   → Revisar logs del backend
   → Verificar llamada API: GET /api/appointments/attention/queue
```

---

##  Tabla Rápida: Dónde está cada concepto

| Concepto | Archivo | Sección |
|----------|---------|---------|
| **Dónde se guarda pago** | RESPUESTA_PREGUNTA_PAGO.md | "¿Dónde se persiste el pago?" |
| **Cómo validar pago** | DIAGNÓSTICO_CITAS.md | Sección 4 |
| **Crear citas** | GUÍA_CREAR_CITAS.md | Sección 2-5 |
| **Flujo completo** | DIAGRAMA_VALIDACIÓN_PAGO.md | Sección 2-3 |
| **Verificar BD** | CHECKLIST_CU06.md | FASE 2 |
| **Debug paso a paso** | CHECKLIST_CU06.md | FASE 1-6 |
| **Script automático** | check_appointments.ps1 | Ejecutar directamente |

---

##  Puntos Clave

### ✅ Dónde se persiste el pago

```
┌─ PostgreSQL
│  └─ Tabla: cita_medica
│     ├─ Columna: estado_administrativo
│     │  ├─ Valor: 'PAGO_VALIDADO'   ← ✅ Aparece en cola
│     │  └─ Valor: 'PAGO_PENDIENTE'  ← ❌ No aparece
│     │
│     ├─ Columna: observacion_administrativa
│     │  └─ Contiene razón del rechazo
│     │
│     ├─ Columna: metodo_pago
│     │  ├─ Valor: 'TARJETA'
│     │  └─ Valor: 'SEGURO'
│     │
│     └─ Columna: costo_consulta
│        └─ Siempre: 175.00
```

### ✅ Criterios para aparecer en cola

Para que una cita aparezca en `/doctor/appointments/attention`:

1. `estado_cita = 'PROGRAMADA'` ✓
2. `estado_administrativo = 'PAGO_VALIDADO'` ✓ ← **CRUCIAL**
3. `personal_id = [ID del doctor autenticado]` ✓
4. `is_active = true` ✓

### ✅ Validación de pago (Simulada)

**Tarjeta:**
- ❌ Rechazada si termina en `0000` (saldo insuficiente)
- ✅ Aprobada en otros casos

**Seguro:**
- ❌ Rechazada si empieza con `X` o contiene `RECHAZADA`
- ✅ Aprobada en otros casos

---

##  Arquitectura (Resumen)

```
Frontend (React)
  ↓
AppointmentAttentionWorkspace.tsx
  ↓ GET /api/appointments/attention/queue
  ↓
Backend (Spring Boot)
  ↓
AppointmentAttentionController
  ↓
MedicalAppointmentAttentionService
  ↓
SqlMedicalAppointmentRepository
  ↓ SQL Filter: PROGRAMADA + PAGO_VALIDADO
  ↓
PostgreSQL (cita_medica table)
  ↓
Retorna: List<MedicalAppointmentQueueItemResponse>
  ↓
Frontend muestra en cola
```

---

##  Atajos Útiles

### Ver estado de citas en BD
```bash
.\check_appointments.ps1
```

### Crear citas de prueba
```bash
# Ver GUÍA_CREAR_CITAS.md sección "Crear Cita 1"
curl -X POST http://localhost:8080/api/appointments ...
```

### Query rápida
```sql
SELECT * FROM cita_medica 
WHERE estado_administrativo = 'PAGO_VALIDADO'
  AND estado_cita = 'PROGRAMADA';
```

### Ver en frontend
```
http://localhost:5173/doctor/appointments/attention
```

---

##  Soporte

Si después de leer la documentación aún tienes dudas:

1. **Verifica que completaste CHECKLIST_CU06.md** hasta Fase 6
2. **Revisa los logs del backend** con `logging.level.his=DEBUG`
3. **Ejecuta check_appointments.ps1** y comparte los resultados
4. **Verifica las queries SQL** de DIAGNÓSTICO_CITAS.md

---

##  Notas

- **Simulación de pago:** El sistema valida SIMULANDO aprobaciones/rechazos
- **No hay tabla de transacciones persistida:** Solo el estado en cita_medica
- **Triage NO crea cita automática:** Solo crea signos vitales
- **Cita DEBE ser PROGRAMADA:** EN_CURSO y ATENDIDA no aparecen en cola
- **Pago DEBE ser VALIDADO:** PAGO_PENDIENTE no aparece en cola

---

## ✨ Documentación Completa Generada

Se han creado estos archivos en `C:\GitHub\proyecto_desarrollo_de_software_v2\his-backend\`:

1. ✅ `RESPUESTA_PREGUNTA_PAGO.md` - Respuesta directa
2. ✅ `DIAGNÓSTICO_CITAS.md` - Análisis técnico detallado
3. ✅ `GUÍA_CREAR_CITAS.md` - Cómo crear citas
4. ✅ `DIAGRAMA_VALIDACIÓN_PAGO.md` - Diagramas visuales
5. ✅ `CHECKLIST_CU06.md` - Verificación paso a paso
6. ✅ `check_appointments.ps1` - Script de diagnóstico
7. ✅ `ÍNDICE_DOCUMENTACIÓN.md` - Este archivo

---

**Generado:** 2026-05-21  
**Versión:** 1.0  
**Estado:** ✅ Completo y listo para usar

