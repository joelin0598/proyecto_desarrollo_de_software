# 📖 GUÍA DE USO - Sistema de Triaje Mejorado v2

## Introducción

El nuevo sistema de triaje implementa un flujo inteligente que permite a las enfermeras:
1. Seleccionar el tipo de ingreso del paciente
2. Buscar si el paciente ya existe (mediante DPI)
3. Saltarse fases si ya tiene datos registrados

---

## 👥 Acceso al Sistema

### Para Enfermeras/Personal de Triaje:
1. Iniciar sesión en http://localhost:5173/login/personal
2. Usar credenciales con rol ENFERMERA o superior
3. En AdminDashboard, hacer clic en:
   - **"Registro de Pacientes"** (CU02)
   - O acceder directamente a http://localhost:5173/triage

---

## 🔄 Flujos Disponibles

### OPCIÓN A: Paciente con Cita Programada

**Paso 1**: En la página de Consulta, seleccionar:
```
📅 Llega con cita programada
```

**Paso 2**: Se mostrará:
- ✅ FASE 1: Datos Personales
- En el campo "Cita medica ID", ingresar el ID de la cita
- Botón "Buscar cita pagada por ID"

**Paso 3**: Si la cita existe y tiene pago validado:
- Los datos se cargan automáticamente
- Sistema salta a FASE 4: Signos Vitales
- Solo ingresar presión, frecuencia cardíaca, temperatura, etc.

**Paso 4**: Guardar triaje

---

### OPCIÓN B: Paciente sin Cita (Búsqueda por DPI)

**Paso 1**: En la página de Consulta, seleccionar:
```
⚕️ Ingreso por Triaje (sin cita)
```

**Paso 2**: En el campo de búsqueda, ingresar:
- DPI: 13 dígitos exactos (ej: 1234567890101)
- Hacer clic en "Buscar"

**Escenario A - Paciente Encontrado ✓**:
- Sistema muestra: "✓ Paciente encontrado. Redirigiendo..."
- Se carga automáticamente FASE 3: Validación de Pago
- Todos los campos de datos personales están deshabilitados (verificados)
- Solo completar tipo de pago (seguro/tarjeta)
- Luego ingresar FASE 4: Signos Vitales
- Guardar triaje

**Escenario B - Paciente No Encontrado ✗**:
- Sistema muestra: "No se encontró registro previo con este DPI..."
- Espera 2 segundos y navega a FASE 1: Datos Personales
- Ingresar todos los datos del paciente nuevo:
  - Nombre completo
  - DPI (los 13 dígitos)
  - Fecha de nacimiento
  - Género
  - Teléfono
  - Email
  - Dirección
- Luego FASE 2: Contacto de Emergencia
- Luego FASE 3: Validación de Pago
- Luego FASE 4: Signos Vitales
- Guardar triaje (nuevo registro + signos vitales)

---

## 📋 FASES DEL FORMULARIO

### FASE 1: Datos Personales
```
✓ Nombre completo (sin números)
✓ DPI: CUI (13 dígitos, sin espacios)
✓ Fecha de nacimiento
✓ Género (Masculino/Femenino/No especifica)
✓ Teléfono (8 dígitos)
✓ Email (@dominio.com)
✓ Dirección de residencia
```

**Nota**: Si viene de búsqueda por DPI, estos campos estarán en gris (deshabilitados).

### FASE 2: Contacto de Emergencia
```
✓ Nombre del contacto (sin números)
✓ Teléfono del contacto (8 dígitos)
```

**Nota**: Si viene de búsqueda por DPI, se salta esta fase.

### FASE 3: Validación de Pago
```
Opción 1 - Con Seguro:
  ✓ Seleccionar aseguradora
  ✓ Ingresar número de póliza

Opción 2 - Sin Seguro (Tarjeta de Crédito):
  ✓ Banco propietario de la tarjeta
  ✓ Número de tarjeta (13-19 dígitos)
  ✓ Fecha de vencimiento (MM/YY)
  ✓ Nombre del titular
  ✓ CVC (3-4 dígitos)
```

### FASE 4: Signos Vitales (Clasificación de Urgencia)
```
✓ Presión Sistólica (50-300 mmHg)
✓ Presión Diastólica (30-200 mmHg)
✓ Frecuencia Cardíaca (20-250 bpm)
✓ Temperatura (10-50 °C)
✓ Saturación de Oxígeno (0-100 %)
✓ Peso (1-999 kg)
✓ Talla (30-300 cm)

El sistema calculará automáticamente la PRIORIDAD:
  🔴 ROJO (Urgencia máxima)
  🟠 NARANJA (Urgencia media-alta)
  🟡 AMARILLO (Urgencia media)
  🟢 VERDE (Urgencia baja)
```

---

## 🎯 EJEMPLO PRÁCTICO

### Caso 1: Don Juan ya fue atendido antes
```
1. Seleccionar "Ingreso por Triaje (sin cita)"
2. Ingresar DPI: 1234567890101
3. ✓ Encontrado!
4. Se cargan automáticamente:
   - Nombre: Juan Pérez García
   - DPI: 1234567890101
   - Nacimiento: 15/03/1975
   - Género: Masculino
   - Teléfono: 78456321
   - Email: juan@example.com
   - Dirección: Zona 10 Guatemala
   - Emergencia: María Pérez
   - Tel Emergencia: 78456322
5. Sistema salta a Validación de Pago
6. Seleccionar modalidad de pago
7. Ingresar signos vitales
8. Guardar
```

### Caso 2: Doña María es primera vez
```
1. Seleccionar "Ingreso por Triaje (sin cita)"
2. Ingresar DPI: 9876543210987
3. ✗ No encontrado
4. Sistema abre FASE 1: Datos Personales
5. Ingresar todos los datos de María
6. Siguiente → FASE 2: Emergencia
7. Ingresar contacto de emergencia
8. Siguiente → FASE 3: Validación de Pago
9. Seleccionar modalidad de pago e ingresar datos
10. Siguiente → FASE 4: Signos Vitales
11. Ingresar signos vitales
12. Guardar
```

---

## ⚠️ VALIDACIONES IMPORTANTES

### Campo "DPI":
- ❌ Debe tener **exactamente 13 dígitos**
- ❌ No se permiten espacios, guiones, puntos
- ❌ Solo números
- ✅ El sistema automáticamente limpia caracteres no numéricos

### Campo "Teléfono":
- ❌ Debe tener **exactamente 8 dígitos**
- ❌ Solo números de Guatemala

### Campo "Email":
- ✅ Formato: usuario@dominio.com
- ❌ Debe contener @ y al menos un punto

### Campo "Nombre":
- ❌ No se permiten números
- ✅ Solo letras y espacios

### Búsqueda por DPI:
- ❌ Solo si la cita tiene **ESTADO PAGO_VALIDADO**
- ❌ Si el paciente existe pero sin cita pagada, se trata como nuevo registro

---

## 📊 PANTALLA DE CONFIRMACIÓN

Después de guardar el triaje, se muestra:

```
✅ Triaje Registrado Correctamente

Información del Paciente:
  • Paciente ID: 1234
  • DPI: 1234567890101
  • Nombre: Juan Pérez

Contacto de Emergencia:
  • Nombre: María Pérez
  • Teléfono: 78456322

Signos Vitales:
  • P. Sistólica: 120 mmHg
  • P. Diastólica: 80 mmHg
  • F. Cardíaca: 72 bpm
  • Temperatura: 37 °C
  • O2: 98 %
  • Peso: 75 kg
  • Talla: 170 cm

🟢 Prioridad: VERDE (Urgencia baja)

[Registrar Nuevo Triaje]  [<< Regresar al Dashboard]
```

---

## 🔧 SOLUCIÓN DE PROBLEMAS

### Problema: "El sistema no encuentra al paciente"
**Causa**: El DPI fue registrado pero sin cita pagada
**Solución**: Crear nuevo registro or verificar base de datos

### Problema: "Error conectando al backend"
**Causa**: Servidor backend no está corriendo
**Solución**: Iniciar backend con `mvnw spring-boot:run`

### Problema: "El DPI debe tener exactamente 13 dígitos"
**Causa**: DPI incompleto o con caracteres inválidos
**Solución**: Verificar formato y reintentar

### Problema: "Teléfono debe tener exactamente 8 dígitos"
**Causa**: Teléfono con menos de 8 dígitos
**Solución**: Ingresar 8 dígitos exactos (ej: 78456321)

### Problema: "Correo electrónico inválido"
**Causa**: Falta @ o dominio incompleto
**Solución**: Usar formato user@domain.com

---

## 💾 GUARDAR Y RETROCEDER

- **Botón "Anterior"**: Retrocede a la fase anterior sin guardar
- **Botón "Siguiente"**: Avanza a la siguiente fase (valida primero)
- **Botón "Limpiar"**: Reinicia el formulario
- **Botón "Guardar triaje"**: Guarda todo (última fase)
- **Botón "Regresar al Dashboard"**: Vuelve a /admin

---

## 📞 SOPORTE

Si encuentras problemas:
1. Verificar que creaste sesión correctamente
2. Verificar que tienes rol ENFERMERA o superior
3. Verificar conexión a internet y backend
4. Si el problema persiste, contactar al administrador del sistema

---

**Última actualización**: 27 de Mayo de 2026
**Versión**: 2.0 - Sistema de Triaje Mejorado

