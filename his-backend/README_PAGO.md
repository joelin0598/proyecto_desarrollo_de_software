#  RESPUESTA: ¿Por qué no aparecen pacientes para atender?

##  TU PREGUNTA

> Mi duda es porque aquí no aparecen pacientes para atender, que deberían de ser los de consultas agendadas y los que entraron con triage, ambos ya validados el pago que si lo realizaron: http://localhost:5173/doctor/appointments/attention
>
> ¿En dónde se guarda o se persiste la información de pago?

---

## ✅ RESPUESTA DIRECTA

### 1. ¿Dónde se persiste la información de pago?

** PostgreSQL → Tabla `cita_medica` → Columna `estado_administrativo`**

```
┌─ BASE DE DATOS: PostgreSQL
│  ├─ Tabla: cita_medica
│  │  ├─ Columna: estado_administrativo
│  │  │  ├─ Valor: 'PAGO_VALIDADO'   → ✅ Aparece en cola
│  │  │  └─ Valor: 'PAGO_PENDIENTE'  → ❌ No aparece
│  │  │
│  │  ├─ Columna: observacion_administrativa
│  │  │  └─ Razón del rechazo (mensaje)
│  │  │
│  │  ├─ Columna: metodo_pago
│  │  │  ├─ 'TARJETA'
│  │  │  └─ 'SEGURO'
│  │  │
│  │  └─ Columna: costo_consulta
│  │     └─ 175.00 (siempre)
```

---

### 2. ¿Por qué no aparecen pacientes?

**Hay 3 razones posibles:**

#### ❌ Razón 1: Las citas tienen `PAGO_PENDIENTE`
El pago fue rechazado en la validación:
- **Tarjeta:** Termina en `0000` (simulación de saldo insuficiente)
- **Seguro:** Empieza con `X` o contiene "RECHAZADA"

**Solución:** Crear citas con datos de pago válidos

#### ❌ Razón 2: No hay citas en la BD
No se han creado citas aún.

**Solución:** Crear citas vía API

#### ❌ Razón 3: Las citas no son `PROGRAMADA`
Ya fueron atendidas, canceladas o están en curso.

**Solución:** Verificar estado de citas en BD

---

##  Documentación Generada

Se han creado **7 documentos** para ayudarte:

###  Documento 1: **RESPUESTA_PREGUNTA_PAGO.md** ⭐ EMPIEZA AQUÍ
**Cuándo leerlo:** Si quieres la respuesta rápida y clara  
**Contiene:**
- Respuesta a tu pregunta
- Dónde se persiste el pago
- Cómo funciona la cola
- Validación de pagos
- Soluciones rápidas

---

###  Documento 2: **DIAGNSTICO_CITAS.md** (Diagnóstico)
**Cuándo leerlo:** Si necesitas entender en detalle  
**Contiene:**
- Análisis técnico profundo
- Código fuente explicado
- 4 causas posibles con soluciones
- Queries SQL para cada caso

---

###  Documento 3: **GUA_CREAR_CITAS.md** (Guía)
**Cuándo leerlo:** Si no hay citas y necesitas crearlas  
**Contiene:**
- Ejemplos de curl para crear citas
- Pago aprobado vs rechazado
- Script PowerShell automatizado
- Cómo obtener JWT token

---

###  Documento 4: **DIAGRAMA_VALIDACIN_PAGO.md** (Diagrama)
**Cuándo leerlo:** Si eres visual  
**Contiene:**
- Arquitectura en diagrama ASCII
- Flujo de creación de cita
- Flujo de consulta de cola
- Tablas comparativas
- Árboles de decisión

---

### ✅ Documento 5: **CHECKLIST_CU06.md**
**Cuándo leerlo:** Para verificar paso a paso  
**Contiene:**
- Checklist en 6 fases
- Verificación inicial
- Diagnóstico de citas
- Crear citas de prueba
- Debugging si algo falla

---

###  Script 6: **check_appointments.ps1**
**Cuándo usarlo:** Para ver rápidamente el estado de la BD  
**Ejecutar:**
```bash
.\check_appointments.ps1
```

**Muestra:**
- Resumen de citas por estado
- Citas disponibles para atención
- Citas con pago pendiente
- Doctores y pacientes registrados

---

###  Índice 7: **NDICE_DOCUMENTACIN.md** (Índice)
**Cuándo leerlo:** Para navegar toda la documentación  
**Contiene:**
- Mapa de todos los documentos
- Tabla de referencia rápida
- Árbol de decisiones
- Atajos útiles

---

##  CÓMO EMPEZAR (3 PASOS)

### Paso 1: Leer RESPUESTA_PREGUNTA_PAGO.md
 5 minutos  
Entenderás dónde se guarda el pago y por qué no aparecen pacientes.

### Paso 2: Ejecutar check_appointments.ps1
⚡ 30 segundos
```bash
.\check_appointments.ps1
```
Sabrás cuántas citas hay y cuántas tienen pago validado.

### Paso 3: Según el resultado...

**Si no hay citas:**
→ Lee GUA_CREAR_CITAS.md  
→ Copia un ejemplo de curl  
→ Crea 2-3 citas de prueba

**Si hay citas pero no aparecen:**
→ Sigue CHECKLIST_CU06.md  
→ Verifica BD con las queries

**Si necesitas entender todo:**
→ Lee DIAGNSTICO_CITAS.md

**Si eres visual:**
→ Lee DIAGRAMA_VALIDACIN_PAGO.md

---

##  Resumen: Persistencia de Pago

```
FRONTEND (React)
  ↓ POST /api/appointments
  ↓ {numeroTarjeta: "4111111111111111", ...}
  
BACKEND (Spring Boot)
  ↓ AppointmentService.scheduleAppointment()
  ↓ validateAndSimulatePayment()
  ├─ SI aprobado → PAGO_VALIDADO ✅
  └─ SI rechazado → PAGO_PENDIENTE ❌
  
PERSISTENCIA
  ↓ INSERT INTO cita_medica (
  │   ...,
  │   estado_administrativo = 'PAGO_VALIDADO' / 'PAGO_PENDIENTE',
  │   observacion_administrativa = "...",
  │   ...
  │ )
  
CONSULTA (GET /api/appointments/attention/queue)
  ↓ SELECT * FROM cita_medica
  │ WHERE estado_administrativo = 'PAGO_VALIDADO'
  │   AND estado_cita = 'PROGRAMADA'
  │   AND personal_id = ?
  
RESULTADO
  ↓ SOLO las citas con PAGO_VALIDADO aparecen en la cola
```

---

##  Validación de Pago (Simulada)

###  TARJETA
```
SI numeroTarjeta.endsWith("0000")
  → ❌ RECHAZADA (saldo insuficiente)
SI NO
  → ✅ APROBADA
```

**Ejemplos:**
- `4111111111111111` → ✅ APROBADA
- `4111111111110000` → ❌ RECHAZADA
- `5500005500005500` → ✅ APROBADA

###  SEGURO
```
SI póliza.startsWith("X") OR póliza.contains("RECHAZADA")
  → ❌ RECHAZADA (póliza no vigente)
SI NO
  → ✅ APROBADA
```

**Ejemplos:**
- `POL2024001234` → ✅ APROBADA
- `XPOL2024001234` → ❌ RECHAZADA
- `POL-RECHAZADA` → ❌ RECHAZADA

---

##  Criterios para aparecer en la cola

Una cita aparecerá SOLO SI cumple TODOS estos criterios:

```
✓ estado_cita = 'PROGRAMADA'
✓ estado_administrativo = 'PAGO_VALIDADO'  ← CRUCIAL
✓ personal_id = [ID del doctor autenticado]
✓ is_active = true
```

---

##  Flujo Completo

```
1. TRIAGE (CU 2.0)
   Paciente entra → Registra signos vitales → Asigna prioridad
   (NO crea cita automáticamente)

2. APPOINTMENT SCHEDULING (CU 04)
   Crear cita → Validar pago → Persistir
   SI pago aprobado → PAGO_VALIDADO ✅
   SI pago rechazado → PAGO_PENDIENTE ❌

3. APPOINTMENT ATTENTION (CU 06) ← TÚ ESTÁS AQUÍ
   Doctor ve cola → SOLO citas con PAGO_VALIDADO
   Doctor inicia atención → Estado pasa a EN_CURSO
   Doctor cierra atención → Estado pasa a ATENDIDA
   (Desaparece de la cola)
```

---

##  Verificar Rápidamente

### Query SQL
```sql
-- Ver citas disponibles para tu doctor
SELECT cita_medica_id, paciente_id, estado_cita, 
       estado_administrativo, observacion_administrativa
FROM cita_medica
WHERE estado_cita = 'PROGRAMADA'
  AND estado_administrativo = 'PAGO_VALIDADO'
  AND is_active = true
ORDER BY fecha_cita ASC;
```

### Ejecutar Script
```bash
.\check_appointments.ps1
```

### Ver en Frontend
```
http://localhost:5173/doctor/appointments/attention
```

---

## ❓ Preguntas Frecuentes

### P1: ¿Las citas del triage aparecen automáticamente en la cola?
**R:** No. El triage solo crea signos vitales. Debes crear una cita médica (CU04) después.

### P2: ¿Dónde se guarda el pago? ¿Hay tabla de transacciones?
**R:** En `cita_medica.estado_administrativo`. No hay tabla de transacciones persistida (es simulado).

### P3: ¿Cómo aparecen pacientes del triage en la cola?
**R:** 
1. Médico crea cita para el paciente (CU04)
2. El pago debe ser validado
3. Entonces aparece en `/doctor/appointments/attention` (CU06)

### P4: ¿Cómo autorizo manualmente un pago rechazado?
**R:** No hay endpoint para esto. Crea una nueva cita con datos de pago válidos.

---

##  Estructura de Archivos

```
C:\GitHub\proyecto_desarrollo_de_software_v2\his-backend\
├─ RESPUESTA_PREGUNTA_PAGO.md         ← ⭐ EMPIEZA AQUÍ
├─ DIAGNSTICO_CITAS.md                ← Análisis técnico
├─ GUA_CREAR_CITAS.md                 ← Ejemplos de API
├─ DIAGRAMA_VALIDACIN_PAGO.md         ← Diagramas visuales
├─ CHECKLIST_CU06.md                  ← Verificación paso a paso
├─ NDICE_DOCUMENTACIN.md              ← Índice completo
├─ check_appointments.ps1             ← Script de diagnóstico
└─ README_PAGO.md                     ← Este archivo
```

---

##  Próximos Pasos

1. **Abre:** RESPUESTA_PREGUNTA_PAGO.md
2. **Ejecuta:** check_appointments.ps1
3. **Según resultado:**
   - Sin citas → GUA_CREAR_CITAS.md
   - Con problemas → CHECKLIST_CU06.md
   - Necesitas detalle → DIAGNSTICO_CITAS.md

---

## ✨ Generado Automáticamente

Todos estos documentos fueron generados automáticamente basados en tu pregunta y en el análisis del código fuente del proyecto.

**Fecha:** 2026-05-21  
**Componente:** CU06 - Atención Médica  
**Base de Datos:** PostgreSQL  
**Persistencia:** Tabla `cita_medica`  
**Estado:** ✅ Completo y listo para usar

---

##  Apoyo

Si después de leer la documentación tienes dudas:

1. Verifica que completaste CHECKLIST_CU06.md
2. Revisa los logs del backend: `logging.level.his=DEBUG`
3. Ejecuta: `.\check_appointments.ps1`
4. Ejecuta las queries SQL de DIAGNSTICO_CITAS.md

---

**¡Buena suerte! **

