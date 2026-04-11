# 🚀 GUÍA DE USO - AUTENTICACIÓN JWT

## 📋 REQUISITOS

- Java 17+
- Maven 3.8+
- PostgreSQL 12+ (o H2 para testing)
- Postman o Insomnia (para probar endpoints)

---

## 🏃 EJECUCIÓN

### **1. Compilar el proyecto**
```bash
cd his-backend
mvn clean compile
```

### **2. Ejecutar la aplicación**
```bash
mvn spring-boot:run
```

O si prefieres con IDE (IntelliJ):
- Click derecho en `HisBackendApplication.java`
- Run 'HisBackendApplication'

**Salida esperada:**
```
Started HisBackendApplication in X.XXX seconds
Server running on: http://localhost:8080
```

---

## 🧪 ENDPOINTS PARA PROBAR

### **Base URL:** `http://localhost:8080/api/auth`

---

### **1. REGISTRO DE USUARIO (USER)**

**Endpoint:** `POST /api/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan@example.com",
  "password": "Password123!@#"
}
```

**Validaciones:**
- Nombre: 2-50 caracteres
- Email: formato válido, único en BD
- Contraseña: mín 6 caracteres, 1 mayúscula, 1 número, 1 símbolo especial

**Response (201 Created):**
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

### **2. REGISTRO DE ADMINISTRADOR (ADMIN)**

**Endpoint:** `POST /api/auth/register/admin`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "firstName": "Dr.",
  "lastName": "García",
  "email": "doctor@hospital.com",
  "password": "SecurePass123!@#",
  "telefono": "50271234567",
  "direccion": "Calle Principal 123, Ciudad",
  "dpi": "1234567890123"
}
```

**Validaciones:**
- Teléfono: 8-15 dígitos
- DPI: exactamente 13 dígitos
- Dirección: 5-255 caracteres
- Password: igual que USER

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 2,
    "email": "doctor@hospital.com",
    "firstName": "Dr.",
    "lastName": "García",
    "role": "ADMIN"
  }
}
```

---

### **3. AUTENTICACIÓN (LOGIN)**

**Endpoint:** `POST /api/auth/authenticate`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "email": "juan@example.com",
  "password": "Password123!@#"
}
```

**Response (200 OK):**
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

### **4. LOGOUT (Revocar Token)**

**Endpoint:** `POST /api/auth/logout`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Body:** (vacío)

**Response (200 OK):**
```json
{
  "errorMessage": "Sesión cerrada exitosamente"
}
```

---

### **5. ACCEDER A RECURSO PROTEGIDO (Ejemplo)**

**Endpoint:** `GET /api/usuarios/perfil` (ejemplo futuro)

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**¿Qué pasa?**
- JwtFilter extrae el token
- Valida que no esté expirado
- Valida que no esté en blacklist
- Autentica al usuario
- Deja pasar la petición ✅

---

## ⚙️ CONFIGURACIÓN

### **File:** `src/main/resources/application.properties`

```properties
# JWT Configuration
jwt.secret.key=base64_encoded_secret_key_min_256_bits
jwt.expiration.hours=24

# Base de Datos
spring.datasource.url=jdbc:postgresql://localhost:5432/his_db
spring.datasource.username=postgres
spring.datasource.password=tu_password

# Logs
logging.level.his.infrastructure.security=DEBUG
logging.level.org.springframework.security=INFO
```

**Para generar secret key Base64:**
```bash
# Linux/Mac:
echo "tu-secret-key-super-segura-minimo-256-bits" | base64

# Windows PowerShell:
[Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes("tu-secret-key-super-segura")) | Out-String
```

---

## 🔍 FLUJO COMPLETO DE PRUEBA

### **Paso 1: Registrar usuario**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "juan@example.com",
    "password": "Password123!@#"
  }'
```

**Respuesta:** Obtén el `token`

---

### **Paso 2: Usar el token**
```bash
curl -X GET http://localhost:8080/api/recurso-protegido \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

### **Paso 3: Hacer logout**
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

### **Paso 4: Intentar usar token revocado**
```bash
curl -X GET http://localhost:8080/api/recurso-protegido \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Respuesta:** 401 Unauthorized (Token revocado)

---

## 📊 CONTENIDO DEL JWT

### **Header:**
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### **Payload (lo que importa):**
```json
{
  "role": "USER",
  "sub": "juan@example.com",
  "idUser": 1,
  "iat": 1712333333,
  "exp": 1712419733
}
```

### **Signature:**
```
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret_key
)
```

---

## ❌ ERRORES Y SOLUCIONES

### **Error: "El correo electrónico ya está en uso"**
```json
{
  "errorMessage": "El correo electrónico ya está en uso"
}
```
**Solución:** Usa otro email que no exista

---

### **Error: "Formato de contraseña inválido"**
```json
{
  "errorMessage": "Formato de contraseña inválido: mínimo 6 caracteres, una mayúscula, un número y un símbolo especial."
}
```
**Solución:** Contraseña debe cumplir: `Password123!`

---

### **Error: "Token JWT expirado"**
```json
{
  "error": "Token JWT expirado",
  "status": 401
}
```
**Solución:** Haz login nuevamente para obtener un token fresco

---

### **Error: "Token revocado"**
```json
{
  "error": "Token revocado. Por favor, inicia sesión nuevamente",
  "status": 401
}
```
**Solución:** Hiciste logout, debes hacer login nuevamente

---

### **Error: "Contraseña incorrecta"**
```json
{
  "errorMessage": "Contraseña incorrecta"
}
```
**Solución:** Verifica que escribiste la contraseña correcta

---

### **Error: "Usuario no registrado"**
```json
{
  "errorMessage": "Usuario no registrado"
}
```
**Solución:** El email no existe, regístrate primero

---

## 🧬 ESTRUCTURA DE BD

### **Tabla: users**
```sql
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'USER' o 'ADMIN'
    created_at TIMESTAMP DEFAULT NOW()
);
```

### **Relaciones:**
- `users` → `user_generic_entity` (1:many, para ADMIN)
- `users` → `user_generic_entity_visit` (1:many, para USER)

---

## 📈 MONITOREO EN LOGS

### **Nivel DEBUG:**
```
[DEBUG] JWT token extracted successfully
[DEBUG] Creando token con expiración en 24 horas
[DEBUG] Clave de firma JWT inicializada correctamente
```

### **Nivel INFO:**
```
[INFO] Generando token para usuario: juan@example.com
[INFO] User successfully authenticated via JWT
[INFO] Token agregado a blacklist
```

### **Nivel WARN:**
```
[WARN] Expired JWT token attempted
[WARN] Token revocado. Intento de uso posterior
[WARN] Email duplicado: ya existe
```

### **Nivel ERROR:**
```
[ERROR] Error al generar token para usuario: juan@example.com
[ERROR] Firma JWT inválida
```

---

## 🔐 RECOMENDACIONES DE SEGURIDAD

### **En Desarrollo:**
- ✅ JWT en localStorage es OK
- ✅ HTTPS no es obligatorio (localhost)
- ✅ Secret key simple es OK

### **En Producción:**
- ✅ Usar HTTPS (certificado SSL)
- ✅ JWT en HttpOnly cookies si es posible
- ✅ Secret key de 256+ bits en AWS Secrets Manager
- ✅ Cambiar a RS256 en lugar de HS256
- ✅ Refresh tokens con expiración corta
- ✅ Token blacklist en Redis distribuido

---

## 📚 DOCUMENTACIÓN ADICIONAL

- `ANALISIS_JWT_Y_RECOMENDACIONES.md` - Análisis técnico detallado
- `CHECKLIST_MEJORAS.md` - Mejoras aplicadas
- `EXPLICACION_MAP_HASHMAP_JWT.md` - Conceptos explicados con manzanas
- `HELP.md` - Ayuda general del proyecto

---

## 💡 TIPS

1. **Guardar token en frontend:**
   ```javascript
   localStorage.setItem('token', response.token);
   ```

2. **Enviar token en peticiones:**
   ```javascript
   headers: {
     'Authorization': `Bearer ${localStorage.getItem('token')}`
   }
   ```

3. **Decodificar JWT (frontend):**
   - Usa: `jwt-decode` library
   - ```javascript
     import jwtDecode from 'jwt-decode';
     const decoded = jwtDecode(token);
     console.log(decoded.role); // "USER" o "ADMIN"
     ```

4. **Extraer claims en el servidor:**
   ```java
   Claims claims = jwtService.getClaim(token, Claims::getBody);
   String role = (String) claims.get("role");
   Long idUser = ((Number) claims.get("idUser")).longValue();
   ```

---

**¿Necesitas más ayuda? Revisa los otros archivos .md o abre un issue** 🚀

