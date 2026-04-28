# 📋 Documentación de Validaciones - Sistema de Autenticación

## ✅ Validaciones Implementadas por Endpoint

### 1️⃣ **POST /api/auth/register** (Registro de Usuario Normal)

#### Campos Validados:
```
✓ firstName
  - @NotBlank: No puede estar vacío
  - @Size(min=2, max=50): Entre 2 y 50 caracteres

✓ lastName
  - @NotBlank: No puede estar vacío
  - @Size(min=2, max=50): Entre 2 y 50 caracteres

✓ email
  - @NotBlank: No puede estar vacío
  - @Email: Debe ser formato válido (ejemplo@domain.com)

✓ password
  - @NotBlank: No puede estar vacío
  - @Size(min=6): Mínimo 6 caracteres
  - PLUS: Validación en AuthService (mayúsculas, números, símbolos especiales)
```

#### Ejemplo de Solicitud Válida:
```json
{
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan@example.com",
  "password": "Password123!"
}
```

#### Errores Posibles:
- `400 Bad Request` - Campo vacío o formato inválido
- `400 Bad Request` - Email duplicado
- `400 Bad Request` - Contraseña débil

---

### 2️⃣ **POST /api/auth/register/admin** (Registro de Administrador)

#### Campos Validados:
```
✓ firstName
  - @NotBlank: No puede estar vacío
  - @Size(min=2, max=50): Entre 2 y 50 caracteres

✓ lastName
  - @NotBlank: No puede estar vacío
  - @Size(min=2, max=50): Entre 2 y 50 caracteres

✓ email
  - @NotBlank: No puede estar vacío
  - @Email: Debe ser formato válido

✓ password
  - @NotBlank: No puede estar vacío
  - @Size(min=6): Mínimo 6 caracteres
  - PLUS: Validación en AuthService (mayúsculas, números, símbolos especiales)

✓ direccion
  - @NotBlank: No puede estar vacío
  - @Size(min=5, max=255): Entre 5 y 255 caracteres

✓ telefono
  - @NotBlank: No puede estar vacío
  - @Pattern: Solo números, 8-15 dígitos
  - Ejemplo válido: "12345678" o "123456789012345"

✓ dpi
  - @NotBlank: No puede estar vacío
  - @Pattern: Exactamente 13 dígitos
  - Ejemplo válido: "1234567890123"
```

#### Ejemplo de Solicitud Válida:
```json
{
  "firstName": "Carlos",
  "lastName": "López",
  "email": "carlos@example.com",
  "password": "AdminPass123!",
  "direccion": "Calle Principal 123, Apartamento 5",
  "telefono": "71234567",
  "dpi": "1234567890123"
}
```

#### Errores Posibles:
- `400 Bad Request` - Campo vacío
- `400 Bad Request` - Teléfono con caracteres no numéricos
- `400 Bad Request` - DPI no es exactamente 13 dígitos
- `400 Bad Request` - Email duplicado

---

### 3️⃣ **POST /api/auth/authenticate** (Autenticación)

#### Campos Validados:
```
✓ email
  - @NotBlank: No puede estar vacío
  - @Email: Debe ser formato válido

✓ password
  - @NotBlank: No puede estar vacío
```

#### Ejemplo de Solicitud Válida:
```json
{
  "email": "juan@example.com",
  "password": "Password123!"
}
```

#### Errores Posibles:
- `400 Bad Request` - Email vacío o formato inválido
- `400 Bad Request` - Contraseña vacía
- `403 Forbidden` - Usuario no encontrado
- `403 Forbidden` - Contraseña incorrecta

---

## 🛡️ Validaciones en AuthService (Backend)

### Validación de Contraseña Fuerte
```regex
^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#$%^&()\\-+=.,])(?=\\S+$).{6,}$
```

Requisitos:
- ✓ Mínimo 6 caracteres
- ✓ Al menos 1 número (0-9)
- ✓ Al menos 1 mayúscula (A-Z)
- ✓ Al menos 1 símbolo especial: !@#$%^&()-+=.,"
- ✓ Sin espacios en blanco

Ejemplos válidos:
- `Abc123!`
- `MyPassword@2024`
- `Secure$Pass99`

Ejemplos inválidos:
- `abc123!` (sin mayúscula)
- `ABC123!` (sin minúscula)
- `AbcPass!` (sin número)
- `Abc12345` (sin símbolo especial)

### Validación de Email Duplicado
- Se verifica que el email no exista en la base de datos
- Si existe → `400 Bad Request: El correo electrónico ya está en uso`

---

## 📊 Códigos de Respuesta HTTP

| Código | Descripción |
|--------|-------------|
| `201 Created` | Registro exitoso |
| `200 OK` | Autenticación exitosa |
| `400 Bad Request` | Validación fallida o email duplicado |
| `403 Forbidden` | Error de autenticación (usuario no encontrado o contraseña incorrecta) |
| `500 Internal Server Error` | Error inesperado del servidor |

---

## 🔄 Flujo de Validación

### Registro/Autenticación:
```
1. Cliente envía datos en JSON
   ↓
2. Spring valida con @Valid
   ├─ Si falla → 400 Bad Request
   ├─ Si pasa → continúa
   ↓
3. Controlador recibe datos validados
   ↓
4. AuthService aplica validaciones adicionales
   ├─ Verifica email duplicado (registro)
   ├─ Valida formato de contraseña (registro)
   ├─ Valida credenciales (autenticación)
   ↓
5. Si todo es correcto → Genera JWT y retorna 200/201
```

---

## 🔐 Respuesta Exitosa

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "email": "juan@example.com",
    "firstName": "Juan",
    "lastName": "Pérez",
    "role": "USER"
  }
}
```

---

## ❌ Respuesta de Error

```json
{
  "errorMessage": "El email debe ser válido"
}
```

---

## 📝 Notas Importantes

1. **Todos los campos son obligatorios**
   - No se permite null o valores vacíos
   - Se rechaza con mensajes descriptivos

2. **Validaciones en dos niveles**
   - Nivel 1: DTOs con anotaciones Jakarta
   - Nivel 2: Lógica de negocio en AuthService

3. **Logging completo**
   - Se registran intentos de registro/autenticación
   - Se registran errores con mensajes descriptivos
   - Los logs incluyen email del usuario para auditoría

4. **Seguridad**
   - Contraseñas cifradas con BCrypt
   - Validación de contraseñas fuertes
   - Tokens JWT con expiración de 24 horas
   - CORS configurado para frontend en localhost:5173

