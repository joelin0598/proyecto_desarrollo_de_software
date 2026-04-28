# 🚀 Guía Rápida de Ejecución - HIS Frontend

## Paso 1: Instalar Dependencias

```powershell
cd C:\GitHub\proyecto_desarrollo_de_software\his-backend\frontend
npm install --legacy-peer-deps
```

**¿Por qué `--legacy-peer-deps`?**
- Vite v5 no es compatible con @vitejs/plugin-react v4.7.0
- Esta bandera permite que npm ignore ciertos conflictos de versiones

## Paso 2: Ejecutar el Servidor de Desarrollo

```powershell
npm run dev
```

**Salida esperada:**
```
VITE v5.0.0 ready in XXX ms

➜  Local:   http://localhost:5173
➜  Network: use --host to expose
```

## Paso 3: Acceder a la Aplicación

Abre tu navegador en:
```
http://localhost:5173
```

## ✨ Qué Verás

### Página de Inicio (/)
- Portal público con todos los servicios del hospital
- Botones para Iniciar Sesión y Registrarse
- Información sobre especialidades y médicos

### Login (/login)
Usa estos datos de prueba:

**Administrador:**
```
Email: admin@hospital.com
Contraseña: AdminPass123!@#
```

**Usuario Normal:**
```
Email: user@example.com
Contraseña: UserPass123!@#
```

### Después de Autenticarse

**Si eres ADMIN:**
→ Serás redirigido a `/admin` (Dashboard de Administrador)

**Si eres USER:**
→ Serás redirigido a `/user` (Portal del Usuario)

## 🔧 Solución de Problemas

### Error: "TSCONFIG_ERROR: Failed to load tsconfig"
**Solución:** El archivo `tsconfig.json` ya está configurado. Asegúrate de que existe.

### Error: "vite no se reconoce como comando"
**Solución:** Las dependencias no están instaladas. Ejecuta:
```powershell
npm install --legacy-peer-deps
```

### Error de dependencias de Vite
**Solución:** Usa la bandera `--legacy-peer-deps`:
```powershell
npm install --legacy-peer-deps
```

### Error: "@/styles/index.css not found"
**Solución:** El archivo está en `src/styles/index.css`. Si el problema persiste:
```powershell
npm run build
npm run dev
```

## 📊 Estructura del Frontend

```
frontend/
├── src/
│   ├── App.tsx                    # Componente principal
│   ├── main.tsx                   # Entry point
│   ├── pages/
│   │   ├── LandingPage.tsx        # Página de inicio pública
│   │   ├── Login.tsx              # Login
│   │   ├── Register.tsx           # Registro
│   │   ├── AdminDashboard.tsx     # Dashboard admin
│   │   └── UserPortal.tsx         # Portal usuario
│   ├── components/
│   │   ├── Header.tsx             # Navegación global
│   │   ├── Footer.tsx             # Pie de página
│   │   ├── ProtectedRoute.tsx     # Rutas protegidas
│   │   └── ...
│   ├── context/
│   │   └── AuthContext.tsx        # Contexto de autenticación
│   ├── services/
│   │   └── api.ts                 # Llamadas a API
│   └── styles/
│       └── index.css              # Estilos globales
├── package.json                   # Dependencias
├── tsconfig.json                  # Config TypeScript
├── vite.config.ts                 # Config Vite
└── ...
```

## 🎯 Navegación Principal

```
Visitante                    Usuario Autenticado
    ↓                               ↓
Landing Page (/)             Header con opciones
    ↓                         ↓
┌─────────────────────────────────────┐
│ Login (/login)  o  Register (/reg) │
└─────────────────────────────────────┘
    ↓                                 ↓
Admin Dashboard (/admin)      User Portal (/user)
```

## 🔐 Seguridad

- ✅ **JWT Token**: Almacenado en localStorage
- ✅ **Rutas Protegidas**: Solo usuarios autenticados acceden
- ✅ **Validación por Rol**: Admin vs User
- ✅ **Auto-redirección**: Redirige al login si token expira

## 💡 Tips

1. **Ver la consola del navegador** (F12) para debugging
2. **Revisar Application > LocalStorage** para ver el token
3. **Usar el menú móvil** para probar responsividad
4. **Probar logout** desde cualquier página

## 🛑 Detener el Servidor

En la terminal donde corre `npm run dev`, presiona:
```
Ctrl + C
```

---

**¿Necesitas ayuda?** Revisa los archivos de documentación en el proyecto.

