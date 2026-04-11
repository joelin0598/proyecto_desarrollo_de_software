# 🧪 Ejemplos de Uso de la API de Autenticación

## Configuración Inicial

```bash
# URL base de la API
BASE_URL=http://localhost:8080

# Headers comunes
HEADER_JSON="Content-Type: application/json"
```

---

## 1️⃣ Registro de Usuario Normal

### ✅ Solicitud Exitosa
```bash
curl -X POST ${BASE_URL}/api/auth/register \
  -H "${HEADER_JSON}" \
  -d '{
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "juan@example.com",
    "password": "Password123!"
  }'
```

**Respuesta (201 Created):**
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

### ❌ Error: Nombre vacío
```bash
curl -X POST ${BASE_URL}/api/auth/register \
  -H "${HEADER_JSON}" \
  -d '{
    "firstName": "",
    "lastName": "Pérez",
    "email": "juan@example.com",
    "password": "Password123!"
  }'
```

**Respuesta (400 Bad Request):**
```json
{
  "errorMessage": "firstName: El nombre es obligatorio"
}
```

---

### ❌ Error: Email inválido
```bash
curl -X POST ${BASE_URL}/api/auth/register \
  -H "${HEADER_JSON}" \
  -d '{
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "correo-invalido",
    "password": "Password123!"
  }'
```

**Respuesta (400 Bad Request):**
```json
{
  "errorMessage": "email: El email debe ser válido"
}
```

---

### ❌ Error: Contraseña débil
```bash
curl -X POST ${BASE_URL}/api/auth/register \
  -H "${HEADER_JSON}" \
  -d '{
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "juan@example.com",
    "password": "12345"
  }'
```

**Respuesta (400 Bad Request):**
```json
{
  "errorMessage": "Formato de contraseña inválido: mínimo 6 caracteres, una mayúscula, un número y un símbolo especial."
}
```

---

### ❌ Error: Email duplicado
```bash
curl -X POST ${BASE_URL}/api/auth/register \
  -H "${HEADER_JSON}" \
  -d '{
    "firstName": "Otro",
    "lastName": "Usuario",
    "email": "juan@example.com",
    "password": "AnotherPass123!"
  }'
```

**Respuesta (400 Bad Request):**
```json
{
  "errorMessage": "El correo electrónico ya está en uso"
}
```

---

## 2️⃣ Registro de Administrador

### ✅ Solicitud Exitosa
```bash
curl -X POST ${BASE_URL}/api/auth/register/admin \
  -H "${HEADER_JSON}" \
  -d '{
    "firstName": "Carlos",
    "lastName": "López",
    "email": "carlos@example.com",
    "password": "AdminPass123!",
    "direccion": "Calle Principal 123, Apartamento 5",
    "telefono": "71234567",
    "dpi": "1234567890123"
  }'
```

**Respuesta (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 2,
    "email": "carlos@example.com",
    "firstName": "Carlos",
    "lastName": "López",
    "role": "ADMIN" 
  }
}
```

---

### ❌ Error: Teléfono con caracteres no numéricos
```bash
curl -X POST ${BASE_URL}/api/auth/register/admin \
  -H "${HEADER_JSON}" \
  -d '{
    "firstName": "Carlos",
    "lastName": "López",
    "email": "carlos@example.com",
    "password": "AdminPass123!",
    "direccion": "Calle Principal 123, Apartamento 5",
    "telefono": "712-34567",
    "dpi": "1234567890123"
  }'
```

**Respuesta (400 Bad Request):**
```json
{
  "errorMessage": "telefono: El teléfono debe contener solo números (8-15 dígitos)"
}
```

---

### ❌ Error: DPI con longitud incorrecta
```bash
curl -X POST ${BASE_URL}/api/auth/register/admin \
  -H "${HEADER_JSON}" \
  -d '{
    "firstName": "Carlos",
    "lastName": "López",
    "email": "carlos@example.com",
    "password": "AdminPass123!",
    "direccion": "Calle Principal 123, Apartamento 5",
    "telefono": "71234567",
    "dpi": "123456789"
  }'
```

**Respuesta (400 Bad Request):**
```json
{
  "errorMessage": "dpi: El DPI debe contener exactamente 13 dígitos"
}
```

---

### ❌ Error: Dirección muy corta
```bash
curl -X POST ${BASE_URL}/api/auth/register/admin \
  -H "${HEADER_JSON}" \
  -d '{
    "firstName": "Carlos",
    "lastName": "López",
    "email": "carlos@example.com",
    "password": "AdminPass123!",
    "direccion": "Calle 5",
    "telefono": "71234567",
    "dpi": "1234567890123"
  }'
```

**Respuesta (400 Bad Request):**
```json
{
  "errorMessage": "direccion: La dirección debe tener entre 5 y 255 caracteres"
}
```

---

## 3️⃣ Autenticación

### ✅ Solicitud Exitosa
```bash
curl -X POST ${BASE_URL}/api/auth/authenticate \
  -H "${HEADER_JSON}" \
  -d '{
    "email": "juan@example.com",
    "password": "Password123!"
  }'
```

**Respuesta (200 OK):**
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

### ❌ Error: Email vacío
```bash
curl -X POST ${BASE_URL}/api/auth/authenticate \
  -H "${HEADER_JSON}" \
  -d '{
    "email": "",
    "password": "Password123!"
  }'
```

**Respuesta (400 Bad Request):**
```json
{
  "errorMessage": "email: El email es obligatorio"
}
```

---

### ❌ Error: Usuario no encontrado
```bash
curl -X POST ${BASE_URL}/api/auth/authenticate \
  -H "${HEADER_JSON}" \
  -d '{
    "email": "noexiste@example.com",
    "password": "Password123!"
  }'
```

**Respuesta (403 Forbidden):**
```json
{
  "errorMessage": "Usuario no registrado"
}
```

---

### ❌ Error: Contraseña incorrecta
```bash
curl -X POST ${BASE_URL}/api/auth/authenticate \
  -H "${HEADER_JSON}" \
  -d '{
    "email": "juan@example.com",
    "password": "IncorrectPass123!"
  }'
```

**Respuesta (403 Forbidden):**
```json
{
  "errorMessage": "Contraseña incorrecta"
}
```

---

## 🔐 Usar el Token en Solicitudes Posteriores

```bash
# Guardar el token de la respuesta anterior
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Usar el token en el header Authorization
curl -X GET ${BASE_URL}/api/usuarios/perfil \
  -H "Authorization: Bearer ${TOKEN}"
```

---

## 📊 Tabla de Validaciones

| Campo | Tipo | Validaciones | Ejemplo Válido | Ejemplo Inválido |
|-------|------|--------------|---|---|
| firstName | String | NotBlank, Size(2-50) | "Juan" | "", "J", "A muy largo nombre que excede los 50 caracteres" |
| lastName | String | NotBlank, Size(2-50) | "Pérez" | "", "P" |
| email | String | NotBlank, Email | "juan@example.com" | "", "invalid-email", "@example.com" |
| password | String | NotBlank, Size(6+), Pattern | "Pass123!" | "", "12345", "nouppercaseorspec123" |
| direccion | String | NotBlank, Size(5-255) | "Calle 123" | "Cll", "", "A".repeat(300) |
| telefono | String | NotBlank, Pattern(8-15 dígitos) | "71234567" | "712-3456", "7123", "712345678901234567" |
| dpi | String | NotBlank, Pattern(13 dígitos) | "1234567890123" | "123456789", "12345678901234", "DPI1234567890" |

---

## 🛠️ Tips Útiles

### Convertir Token JWT en JSON (para ver el contenido)
```bash
# El token tiene 3 partes separadas por puntos: header.payload.signature
# Para ver el payload (sin validar), base64-decode la segunda parte

TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c3VhcmlvQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.signature"

# En Linux/Mac:
echo ${TOKEN##*.} | base64 -d | jq '.'

# En Windows PowerShell:
$payload = ($TOKEN -split '\.')[1]
[System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($payload))
```

### Validar Formato de JSON
```bash
# Instalar jq en el sistema
sudo apt-get install jq  # Linux
brew install jq          # Mac
choco install jq         # Windows

# Validar respuesta
curl -s ${BASE_URL}/api/auth/register ... | jq '.'
```

