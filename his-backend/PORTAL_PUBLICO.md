# 🏥 Portal Web del Sistema de Información Hospitalario

## ¿Qué se ha implementado?

Se ha agregado un **Portal Web Público** completo al proyecto HIS que permite a los visitantes:

### 1. **Página de Inicio (Landing Page)**
- Interfaz moderna y profesional con Tailwind CSS
- Muestra todos los servicios disponibles del hospital:
  - 📅 **Agendar Cita**: Reserva de consultas médicas
  - 🧪 **Laboratorio**: Solicitud y consulta de exámenes clínicos
  - 💊 **Recetas y Medicamentos**: Control de medicinas
  - 📋 **Historial Médico**: Acceso a registros clínicos
  - 💳 **Pago en Línea**: Estado de cuenta y pagos
  - ❤️ **Controles Médicos**: Monitoreo de signos vitales

- Sección de **Especialidades y Médicos** con disponibilidad
- Información de seguridad, disponibilidad 24/7 y accesibilidad
- Botones de **Iniciar Sesión** y **Registrarse** bien visibles

### 2. **Componentes Principales**

#### **Header (Navegación Global)**
- Logo y marca del hospital
- Navegación responsive (desktop y mobile)
- Muestra información del usuario cuando está autenticado
- Botones de acción dinámicos según estado de autenticación
- Menú móvil colapsable

#### **Footer (Pie de Página)**
- Información de la empresa
- Enlaces a servicios y documentos
- Contacto y horarios
- Enlaces legales (Privacidad, Términos, Cookies)

#### **Landing Page**
- Hero section con llamada a la acción
- Grid de servicios disponibles
- Sección informativa con iconos
- Catálogo de especialidades médicas
- Diseño responsivo

### 3. **Flujo de Navegación**

```
INICIO (/)
    ↓
┌─────────────────────────────────────────┐
│  Portal Público (Landing Page)          │
│  - Ver servicios disponibles            │
│  - Ver especialidades y médicos         │
│  - Opción: Iniciar Sesión              │
│  - Opción: Registrarse                 │
└─────────────────────────────────────────┘
    ↓
    ├─→ LOGIN (/login)
    │   └─→ Autenticar usuario
    │       └─→ ADMIN → Admin Dashboard (/admin)
    │       └─→ USER → User Portal (/user)
    │
    └─→ REGISTER (/register)
        └─→ Crear cuenta
            └─→ Redirigir a User Portal (/user)
```

### 4. **Protección de Rutas**

- ✅ Las rutas `/admin` y `/user` están protegidas
- ✅ Solo usuarios autenticados pueden acceder
- ✅ Validación por rol (ADMIN vs USER)
- ✅ Redirección automática al login si no está autenticado

### 5. **Características de Seguridad**

- 🔐 JWT Token para autenticación
- 🔐 LocalStorage para persistencia de sesión
- 🔐 Componentes protegidos con validación de rol
- 🔐 Logout disponible desde cualquier página

## 🛠️ Cambios Técnicos Realizados

### Archivos Creados:
```
frontend/src/
├── pages/
│   └── LandingPage.tsx          ✨ Nueva página de inicio
├── components/
│   ├── Header.tsx               ✨ Navegación global
│   └── Footer.tsx               ✨ Pie de página
```

### Archivos Modificados:
```
frontend/src/
├── App.tsx                       ✅ Actualizado - Ruta "/" → LandingPage
├── pages/
│   ├── Login.tsx                 ✅ Mejorado - Incluye Header
│   ├── Register.tsx              ✅ Mejorado - Incluye Header
│   ├── AdminDashboard.tsx        ✅ Mejorado - Incluye Header
│   └── UserPortal.tsx            ✅ Mejorado - Incluye Header
├── styles/
│   └── index.css                 ✅ Mejorado - Estilos adicionales
└── package.json                  ✅ Actualizado - Versiones compatibles
```

## 📊 Dependencias Corregidas

Se actualizó `package.json` para resolver conflictos de versiones:

```json
"@vitejs/plugin-react": "^4.2.1"  // ← Cambio de ^5.0.0 a ^4.2.1
"vite": "^5.0.0"                  // ← Cambio de ^8.0.3 a ^5.0.0
```

**Solución**: Usó `npm install --legacy-peer-deps` para resolver la incompatibilidad

## 🚀 Cómo Ejecutar

### 1. Instalar dependencias:
```bash
cd frontend
npm install --legacy-peer-deps
```

### 2. Ejecutar servidor de desarrollo:
```bash
npm run dev
```

### 3. Acceder a:
```
http://localhost:5173
```

## 📋 Usuarios de Prueba

**Admin:**
- Email: `admin@hospital.com`
- Contraseña: `AdminPass123!@#`
- Rol: ADMIN

**Usuario Normal:**
- Email: `user@example.com`
- Contraseña: `UserPass123!@#`
- Rol: USER

## ✨ Características del Portal Público

### Para Visitantes No Autenticados:
- ✅ Explorar servicios disponibles
- ✅ Ver especialidades y médicos
- ✅ Información sobre el hospital
- ✅ Acceso a login/registro

### Para Usuarios Autenticados (USER):
- ✅ Ver próximas citas
- ✅ Historial médico
- ✅ Documentos y resultados
- ✅ Información personal
- ✅ Solicitar ayuda

### Para Administradores (ADMIN):
- ✅ Dashboard con estadísticas
- ✅ Usuarios registrados recientes
- ✅ Actividad del sistema
- ✅ Gestión de usuarios
- ✅ Acceso a reportes

## 🎨 Diseño Responsivo

- ✅ Desktop (1024px+)
- ✅ Tablet (768px - 1023px)
- ✅ Mobile (< 768px)
- ✅ Menú mobile colapsable
- ✅ Componentes adaptables

## 🔄 Próximas Mejoras Sugeridas

1. **Módulos de Servicios**: Implementar componentes para:
   - Agendar citas
   - Consultar resultados de laboratorio
   - Ver recetas y medicamentos

2. **Dashboard de Admin**: Expandir con:
   - Gráficos de estadísticas
   - Gestión de usuarios
   - Reportes detallados

3. **Perfil de Usuario**: Añadir:
   - Edición de datos personales
   - Cambio de contraseña
   - Preferencias de notificación

4. **Integración con Backend**: Conectar todos los endpoints

---

**Estado**: ✅ Portal público completamente funcional y listo para integración con backend.

