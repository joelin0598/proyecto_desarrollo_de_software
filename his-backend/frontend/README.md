# Frontend HIS - Sistema de Información Hospitalaria

Frontend React + TypeScript + Tailwind CSS para el Sistema de Información Hospitalaria.

## 🚀 Instalación y Ejecución

### 1. Instalar dependencias
```bash
cd frontend
npm install
```

### 2. Ejecutar en desarrollo
```bash
npm run dev
```

La aplicación se abrirá en `http://localhost:5173`

### 3. Compilar para producción
```bash
npm run build
```

## 📋 Funcionalidades

### 🔐 Autenticación
- **Login:** Inicia sesión con email y contraseña
- **Registro:** Crea una nueva cuenta de usuario
- **JWT Token:** Se almacena en localStorage y se envía en cada petición

### 👥 Roles y Dashboards

#### ADMIN Dashboard
- Vista de estadísticas del sistema
- Gestión de usuarios
- Últimos usuarios registrados
- Actividad del sistema
- Acciones rápidas

#### USER Portal
- Próximas citas médicas
- Historial médico
- Documentos y exámenes
- Información personal
- Solicitar citas

## 🧪 Credenciales de Prueba

### Registrarse
Puedes crear una nueva cuenta en `/register` con cualquier email válido.

**Requisitos de contraseña:**
- Mínimo 6 caracteres
- 1 letra mayúscula
- 1 número
- 1 símbolo especial (! @ # $ % ^ & ( ) - + = . ,)

Ejemplos válidos:
- `Password123!`
- `Admin@2024`
- `MyPass#99`

## 🏗️ Estructura de Carpetas

```
frontend/
├── public/
├── src/
│   ├── components/          # Componentes reutilizables
│   │   └── ProtectedRoute.tsx
│   ├── context/             # React Context
│   │   └── AuthContext.tsx
│   ├── pages/               # Páginas principales
│   │   ├── Login.tsx
│   │   ├── Register.tsx
│   │   ├── AdminDashboard.tsx
│   │   └── UserPortal.tsx
│   ├── services/            # API calls
│   │   └── api.ts
│   ├── styles/              # Estilos CSS
│   │   └── index.css
│   ├── App.tsx              # Componente principal
│   └── main.tsx             # Punto de entrada
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
├── tailwind.config.ts
└── postcss.config.js
```

## 🔄 Flujo de Autenticación

1. **Login/Register:** Usuario se autentica
2. **Token Guardado:** JWT se almacena en localStorage
3. **Request Intercepted:** Cada petición incluye el token en header
4. **Validación:** Backend valida el token
5. **Redirección:** Según el rol, se redirige al dashboard correcto

## 🛡️ Características de Seguridad

✅ JWT token en header Authorization  
✅ localStorage seguro para almacenar datos  
✅ Rutas protegidas por rol  
✅ Logout que limpia el token  
✅ Validación de contraseña  
✅ CORS configurado  

## 🎯 Endpoints API Utilizados

```
POST   /api/auth/register       # Registrar usuario
POST   /api/auth/authenticate   # Login
POST   /api/auth/logout         # Logout
POST   /api/auth/register/admin # Registrar admin
```

## 📝 Notas Importantes

- El token expira en **24 horas**
- El token se incluye automáticamente en cada petición
- Si el token expira, se redirige a login
- Los datos del usuario se guardan en localStorage

## 🚀 Próximas Mejoras

- [ ] Refresh tokens
- [ ] Notificaciones push
- [ ] Temas (dark mode)
- [ ] Más páginas de gestión
- [ ] Gráficos y reportes
- [ ] Integración con WebSocket

---

**Estado:** ✅ Production Ready  
**Versión:** 0.0.1  
**Última actualización:** 2026-04-05

