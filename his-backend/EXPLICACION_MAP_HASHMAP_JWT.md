# 🍎 EXPLICACIÓN CON MANZANAS: Map, HashMap, Claims y JWT

## 1️⃣ ¿QUÉ ES UN MAP?

### **La Analogía de las Manzanas 🍎**

Imagina que tienes una **canasta de manzanas** donde cada manzana tiene:
- Una **etiqueta** (la llave/key)
- Una **información** escrita en la etiqueta (el valor/value)

```
Canasta (Map):
┌─────────────────────────┐
│ "color" → "rojo"        │  ← clave: valor
│ "peso" → "200 gramos"   │
│ "tipo" → "Fuji"         │
│ "precio" → "$5.00"      │
└─────────────────────────┘
```

**En código:**
```java
Map<String, Object> manzana = new HashMap<>();
manzana.put("color", "rojo");        // Poner una etiqueta
manzana.put("peso", "200 gramos");
System.out.println(manzana.get("color")); // Leer: "rojo"
```

---

## 2️⃣ ¿QUÉ ES UN HASHMAP?

### **La Implementación Rápida 🚀**

Un `HashMap` es una **forma específica** de llenar la canasta que es **MUY RÁPIDA**.

```
HashMap vs Array:

Array (Lento si hay muchas manzanas):
┌──────────────────────────────────────┐
│ [0] "rojo"   (revisar 1)             │
│ [1] "verde"  (revisar 2)             │
│ [2] "amarillo" (revisar 3)           │ ← Tengo que revisar todas hasta encontrar
│ [3] "naranja" (revisar 4)            │
│ [4] "morado" (revisar 5) ← ENCONTRÉ │
└──────────────────────────────────────┘

HashMap (MUY RÁPIDO - acceso directo):
┌─────────────────────────┐
│ "color" → [índice 5]    │ ← Directo, sin revisar todas
│ "peso"  → [índice 2]    │
│ "tipo"  → [índice 8]    │
└─────────────────────────┘
```

**Por qué es rápido:**
- Usa un "hash" (número único por llave)
- Busca directo en lugar de revisar todo
- Tiempo de búsqueda: O(1) vs O(n)

---

## 3️⃣ ¿QUÉ SON LOS CLAIMS EN JWT?

### **Los Claims = Las Etiquetas de las Manzanas 🏷️**

Un **JWT (JSON Web Token)** es una canasta de manzanas **firmada digitalmente** que contiene información sobre un usuario.

Los **claims** son las etiquetas (información) dentro de ese JWT.

```
JWT = Encabezado.Payload.Firma

Payload (lo importante - es donde van los claims):
{
  "sub": "usuario@email.com",        ← Claim: quién es (subject)
  "role": "ADMIN",                   ← Claim: qué rol tiene
  "email": "usuario@email.com",      ← Claim: email
  "idUser": 123,                     ← Claim: ID personalizado
  "iat": 1712333333,                 ← Claim: fecha de creación
  "exp": 1712419733                  ← Claim: fecha de expiración
}
```

---

## 4️⃣ ¿CÓMO SE USA TODO JUNTO?

### **El Flujo Completo 📊**

```
REGISTRO/LOGIN
│
├─ Usuario envía email + password
│
├─ Servidor valida credenciales
│
├─ Servidor crea un Map con claims:
│  ┌──────────────────────────────────┐
│  │ Map<String, Object> claims =     │
│  │   new HashMap<>();               │
│  │ claims.put("role", "ADMIN");     │
│  │ claims.put("sub", email);        │
│  │ claims.put("idUser", 123);       │
│  └──────────────────────────────────┘
│
├─ Servidor convierte el Map → JWT firmado:
│  └─ eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
│    eyJyb2xlIjoiQURNSU4iLCJzdWIiOiJ1c2VyQGVtYWlsLmNvbSIsImlkVXNlciI6MTIzfQ.
│    FIRMA_DIGITAL_SEGURA
│
├─ Servidor envía token al cliente
│
└─ Cliente guarda en localStorage / sessionStorage

SIGUIENTE PETICIÓN
│
├─ Cliente envía: Authorization: Bearer <JWT>
│
├─ Servidor extrae el JWT
│
├─ Servidor valida:
│  ├─ ¿La firma es correcta? (¿fue modificado?)
│  ├─ ¿No está expirado?
│  └─ ¿El usuario existe en BD?
│
├─ Si está OK, servidor extrae los claims (el Map original):
│  ┌────────────────────────────────────┐
│  │ String role = jwt.get("role");     │ → "ADMIN"
│  │ String email = jwt.get("sub");     │ → "usuario@email.com"
│  │ Long idUser = jwt.get("idUser");   │ → 123
│  └────────────────────────────────────┘
│
└─ Usuario autenticado ✅
```

---

## 5️⃣ EJEMPLO PRÁCTICO DEL CÓDIGO

### **En AuthService (Tu Código):**

```java
// Paso 1: Crear un Map (la canasta)
Map<String, Object> claims = new HashMap<>();

// Paso 2: Llenar el Map con etiquetas (claims)
claims.put("role", user.getRole().name());           // "ADMIN" o "USER"
claims.put("sub", user.getEmail());                  // "usuario@email.com"

if (idUser != null) {
    claims.put("idUser", idUser);                    // 123 (ID del usuario genérico)
}

// Paso 3: Pasar el Map a la función para crear el JWT
String token = generateToken(claims, user);
//                           ↑
//                    El HashMap convertido a JWT

// El JWT generado se ve así:
// eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
// eyJyb2xlIjoiQURNSU4iLCJzdWIiOiJ1c2VyQGVtYWlsLmNvbSIsImlkVXNlciI6MTIzLCJpYXQiOjE3MTIzMzMzMzMsImV4cCI6MTcxMjQxOTczM30.
// FIRMA_SEGURA_AQUI
```

### **En JwtFilter (Tu Código):**

```java
// Recibimos el JWT del cliente
final String jwt = authHeader.substring(BEARER_LENGTH);

// Extraemos información (claims) del JWT
final String userEmail = jwtService.getUserName(jwt);
// ↑ Aquí estamos leyendo del Map original el claim "sub"

// Cargamos el usuario de BD
UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

// Validamos el JWT
if (jwtService.validateToken(jwt, userDetails)) {
    // ✅ El JWT es válido, usuario autenticado
    // Los claims (role, idUser, etc.) están disponibles en el token
}
```

---

## 6️⃣ ¿POR QUÉ ESTO ES MEJOR QUE COOKIES?

### **JWT vs Cookies 🍪**

```
COOKIES:
┌─────────────────────────────────────────┐
│ Guardadas en el servidor                │
│ ├─ Se necesita BD para cada validación  │
│ ├─ No escalable en microservicios       │
│ └─ Requiere sincronización              │
└─────────────────────────────────────────┘

JWT (Lo que usas):
┌──────────────────────────────────────────┐
│ Guardadas en el cliente                  │
│ ├─ Servidor solo valida la firma        │
│ ├─ Escalable (sin estado - stateless)   │
│ ├─ Perfecto para microservicios         │
│ └─ El cliente lleva su "pasaporte"      │
└──────────────────────────────────────────┘
```

Imagina:
- **Cookie** = Un recepcionista debe buscar en un archivo cada vez
- **JWT** = Un pasaporte que el cliente lleva y solo verificas que sea real

---

## 7️⃣ ¿QUÉ PASÓ EN LAS MEJORAS?

### **Mejora 1: Validar Null en Expiración**

```java
// ❌ ANTES (peligroso):
Date expirationDate = getAllClaims(token).getExpiration();
boolean expired = expirationDate.before(new Date()); // ¡Si expirationDate es null, crash!

// ✅ DESPUÉS (seguro):
Date expirationDate = getAllClaims(token).getExpiration();
if (expirationDate == null) {
    return true; // Token inválido si no tiene expiración
}
boolean expired = expirationDate.before(new Date()); // Seguro
```

Es como preguntarle "¿cuál es la fecha de vencimiento?" - primero verifica que tenga respuesta.

---

### **Mejora 2: TokenBlacklist (Logout)**

```java
// Cuando usuario hace logout:
tokenBlacklistService.addToBlacklist(token);

// En siguientes peticiones:
if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
    // ❌ Token revocado, rechaza la petición
    return;
}

// Es como tener una "lista negra" de pasaportes cancelados
```

---

### **Mejora 3: @Transactional**

```java
// ✅ DESPUÉS (transaccional):
@Transactional
public AuthResponse register(RegisterRequest request) {
    // Si falla CUALQUIER paso, TODO se revierte
    var user = userRepository.save(user);      // Paso 1
    var client = userGenericVisitRepository.save(client); // Paso 2
    var token = jwtService.generateToken(user);           // Paso 3
    
    // Si Paso 2 falla, Paso 1 se revierte (rollback)
}
```

Es como:
- **Sin @Transactional**: Creas usuario pero no sus datos → BD inconsistente ❌
- **Con @Transactional**: "Todo o nada" → BD siempre consistente ✅

---

## 8️⃣ RESUMEN VISUAL

```
Mi App
   │
   ├─ Usuario
   │   │
   │   ├─ "Voy a registrarme"
   │   │   │
   │   │   └─> AuthController
   │   │        │
   │   │        └─> AuthService
   │   │            ├─ Validar credenciales
   │   │            ├─ Crear usuario
   │   │            ├─ ✅ Crear Map (HashMap con claims)
   │   │            ├─ ✅ Convertir a JWT
   │   │            └─ Devolver token
   │   │
   │   └─ "Aquí está tu JWT"
   │       (Lo guarda en localStorage)
   │
   ├─ Usuario
   │   │
   │   ├─ "Voy a acceder a un recurso protegido"
   │   │   (Envía: Authorization: Bearer JWT)
   │   │
   │   └─> JwtFilter
   │        ├─ Extrae el JWT
   │        ├─ Verifica que no esté en blacklist
   │        ├─ Valida la firma (¿es real?)
   │        ├─ ✅ Extrae los claims (rol, email, idUser)
   │        ├─ Carga el usuario de BD
   │        └─ Autentica en SecurityContext
   │
   └─ "✅ Acceso permitido"
```

---

## 9️⃣ PREGUNTA: ¿CUÁNDO USAR QUÉ?

### **¿Cuándo usar Map?**
- Cuando necesitas pares clave-valor flexibles
- En configuraciones, DTOs, response/request
- Cuando la estructura es variable

### **¿Cuándo usar HashMap?**
- Cuando necesitas acceso rápido O(1)
- Cuando no necesitas orden (HashMap no lo mantiene)
- Para cachés, tokens, configuraciones

### **¿Cuándo usar Claims?**
- Dentro de JWT para información de usuario
- Datos que quieres que viaje en el token
- Información que no necesita actualizarse en BD

---

## 🔟 MEJORA FUTURA: REFRESH TOKENS

```
Idea: Dos manzanas 🍎🍎

Primera Manzana (Access Token - corta vida):
├─ Expira en: 15 minutos
├─ Usada para: Acceder a recursos
└─ Claims: role, email, idUser

Segunda Manzana (Refresh Token - larga vida):
├─ Expira en: 7 días
├─ Usada para: Obtener nueva Access Token
└─ Claims: solo para verificar

Flujo:
1. Usuario login → recibe ambos tokens
2. Accede a recurso con Access Token (15 min)
3. Access Token expira → USA Refresh Token
4. Servidor valida Refresh Token → emite nuevo Access Token
5. Continúa sin que usuario tenga que login nuevamente
```

---

**¿Tienes alguna duda sobre Map, HashMap, Claims o JWT? ¡Pregunta! 🚀**

