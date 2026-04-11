# 📱 Flujo Visual del Portal Público HIS

## 1. LANDING PAGE (Página de Inicio) - Sin Autenticación

```
┌─────────────────────────────────────────────────────────────────┐
│ 🏥 HIS   Hospital Info System    [Iniciar Sesión] [Registrarse] │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│      ╔═══════════════════════════════════════════════════════╗  │
│      ║  Bienvenido al Sistema de Información Hospitalario    ║  │
│      ║                                                       ║  │
│      ║  Plataforma digital para gestionar tu salud de forma  ║  │
│      ║           segura y remota                            ║  │
│      ║                                                       ║  │
│      ║  [Iniciar Sesión]  [Registrarse]                     ║  │
│      ╚═══════════════════════════════════════════════════════╝  │
│                                                                   │
│                    SERVICIOS DISPONIBLES                         │
│  ┌──────────────┬──────────────┬──────────────┐                 │
│  │   📅 Agendar │  🧪 Lab.    │  💊 Recetas  │                 │
│  │    Cita      │              │              │                 │
│  ├──────────────┼──────────────┼──────────────┤                 │
│  │  📋 Historial│  💳 Pago en  │  ❤️ Controles│                 │
│  │    Médico    │   Línea      │   Médicos    │                 │
│  └──────────────┴──────────────┴──────────────┘                 │
│                                                                   │
│           INFORMACIÓN IMPORTANTE DEL HOSPITAL                    │
│  ┌──────────────┬──────────────┬──────────────┐                 │
│  │  24/7        │   🔒         │    📱        │                 │
│  │  Disponible  │  Seguridad   │  Accesible   │                 │
│  └──────────────┴──────────────┴──────────────┘                 │
│                                                                   │
│           ESPECIALIDADES Y MÉDICOS DISPONIBLES                   │
│  ┌────────────────────────────────────────────┐                 │
│  │ Medicina General   |  Cardiología          │                 │
│  │ Pediatría          |  Odontología          │                 │
│  │ [Ver todos los médicos]                    │                 │
│  └────────────────────────────────────────────┘                 │
│                                                                   │
├─────────────────────────────────────────────────────────────────┤
│ © 2026 Hospital Information System. Todos los derechos...      │
│ [Privacidad] [Términos] [Cookies]                              │
└─────────────────────────────────────────────────────────────────┘
```

## 2. LOGIN PAGE (/login)

```
┌─────────────────────────────────────────────────────────────────┐
│ 🏥 HIS   Hospital Info System    [Inicio] [Registrarse]        │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│                    ╔═════════════════════════╗                  │
│                    ║  Iniciar Sesión         ║                  │
│                    ║  Accede a tu cuenta HIS ║                  │
│                    ║                         ║                  │
│                    ║ Email:                  ║                  │
│                    ║ [________________________]                  │
│                    ║                         ║                  │
│                    ║ Contraseña:             ║                  │
│                    ║ [________________________]                  │
│                    ║ Min 6 caracteres...     ║                  │
│                    ║                         ║                  │
│                    ║ [Iniciar Sesión]        ║                  │
│                    ║                         ║                  │
│                    ║ ¿No tienes cuenta?      ║                  │
│                    ║ [Registrarse]           ║                  │
│                    ║                         ║                  │
│                    ║ 📝 Test Usuarios:       ║                  │
│                    ║ Admin: admin@hosp...   ║                  │
│                    ║ User: user@example...  ║                  │
│                    ╚═════════════════════════╝                  │
│                                                                   │
├─────────────────────────────────────────────────────────────────┤
│ © 2026 Hospital Information System...                           │
└─────────────────────────────────────────────────────────────────┘
```

## 3. REGISTER PAGE (/register)

```
┌─────────────────────────────────────────────────────────────────┐
│ 🏥 HIS   Hospital Info System    [Inicio] [Iniciar Sesión]     │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│                    ╔═════════════════════════╗                  │
│                    ║  Registrarse            ║                  │
│                    ║  Crea tu cuenta         ║                  │
│                    ║                         ║                  │
│                    ║ Nombre:    | Apellido:  ║                  │
│                    ║ [_______] | [_______]  ║                  │
│                    ║                         ║                  │
│                    ║ Email:                  ║                  │
│                    ║ [________________________]                  │
│                    ║                         ║                  │
│                    ║ Contraseña:             ║                  │
│                    ║ [________________________]                  │
│                    ║ Mín 6 caracteres...     ║                  │
│                    ║                         ║                  │
│                    ║ [Registrarse]           ║                  │
│                    ║                         ║                  │
│                    ║ ¿Ya tienes cuenta?      ║                  │
│                    ║ [Inicia sesión]         ║                  │
│                    ╚═════════════════════════╝                  │
│                                                                   │
├─────────────────────────────────────────────────────────────────┤
│ © 2026 Hospital Information System...                           │
└─────────────────────────────────────────────────────────────────┘
```

## 4. USER PORTAL (/user) - Usuario Autenticado

```
┌─────────────────────────────────────────────────────────────────┐
│ 🏥 HIS   Hospital Info System    Juan Pérez  [Mi Portal] [Logout]
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│ ╔═════════════════════════════════════════════════════════════╗ │
│ ║ ¡Bienvenido, Juan!                                          ║ │
│ ║ Accede a tu información médica y gestiona tus citas         ║ │
│ ╚═════════════════════════════════════════════════════════════╝ │
│                                                                   │
│  ┌──────────────────┬──────────────────┬──────────────────┐    │
│  │ 📅 Próximas      │ 📋 Historial     │ 📄 Documentos    │    │
│  │ Citas            │ Médico           │                  │    │
│  │                  │                  │                  │    │
│  │ Dr. García       │ ✓ Última: 3/04   │ Examen Lab 2/04  │    │
│  │ 6/04 - 10:00 AM  │ ✓ Exámenes: 2/04 │ Radiografía 1/04 │    │
│  │                  │ ✓ Medicinas: 3   │ Receta 31/03     │    │
│  │ Dr. Martínez     │ ✓ Alergias:      │                  │    │
│  │ 12/04 - 2:30 PM  │   Penicilina     │ [Ver todos]      │    │
│  │                  │                  │                  │    │
│  │ [Ver todas]      │ [Descargar]      │                  │    │
│  └──────────────────┴──────────────────┴──────────────────┘    │
│                                                                   │
│  ┌──────────────────────────┬──────────────────────────┐       │
│  │ 👤 Información Personal  │ 🆘 Necesitas Ayuda?     │       │
│  │                          │                         │       │
│  │ Nombre: Juan Pérez       │ [Contactar Soporte]     │       │
│  │ Email: juan@example.com  │ [Ver FAQs]              │       │
│  │ Rol: USER                │ [Solicitar Cita]        │       │
│  │                          │                         │       │
│  │ [Editar Perfil]          │                         │       │
│  └──────────────────────────┴──────────────────────────┘       │
│                                                                   │
├─────────────────────────────────────────────────────────────────┤
│ © 2026 Hospital Information System...                           │
└─────────────────────────────────────────────────────────────────┘
```

## 5. ADMIN DASHBOARD (/admin) - Administrador

```
┌─────────────────────────────────────────────────────────────────┐
│ 🏥 HIS   Hospital Info System    Admin User [Dashboard] [Logout]│
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌────────────┬────────────┬────────────┬────────────┐          │
│  │ Pacientes  │ Médicos    │ Citas Hoy  │ Consultas  │          │
│  │   1,234    │    45      │     28     │    12      │          │
│  │ +12% ↑     │ Disponible │ Confirmada │ Pendientes │          │
│  └────────────┴────────────┴────────────┴────────────┘          │
│                                                                   │
│  ┌─────────────────────────────────────────────────────┐        │
│  │ Últimos Usuarios Registrados                        │        │
│  │ ┌──────────────────────────────────────────────┐   │        │
│  │ │ Juan Pérez          | USER      | Hace 2h  │   │        │
│  │ │ juan@example.com    |           |          │   │        │
│  │ ├──────────────────────────────────────────────┤   │        │
│  │ │ María García        | DOCTOR    | Hace 5h  │   │        │
│  │ │ maria@hospital.com  |           |          │   │        │
│  │ ├──────────────────────────────────────────────┤   │        │
│  │ │ Carlos López        | USER      | Hace 1d  │   │        │
│  │ │ carlos@example.com  |           |          │   │        │
│  │ └──────────────────────────────────────────────┘   │        │
│  └─────────────────────────────────────────────────────┘        │
│                                                                   │
│  ┌──────────────────┐    ┌──────────────────────────┐           │
│  │ 👤 Tu Info       │    │ 🆘 Acciones Rápidas     │           │
│  │                  │    │                         │           │
│  │ Email: admin...  │    │ [Gestionar Usuarios]    │           │
│  │ Rol: ADMIN       │    │ [Ver Reportes]          │           │
│  │ Desde: 5/04/2026 │    │ [Configuración]         │           │
│  │ 🟢 Activo        │    │                         │           │
│  │                  │    └──────────────────────────┘           │
│  └──────────────────┘                                            │
│                                                                   │
│  ┌──────────────────────────────────────────────────┐           │
│  │ 🔒 Seguridad                                     │           │
│  │ Tu sesión está protegida con JWT                │           │
│  │ Token expira en 24 horas                         │           │
│  │ Último acceso: Ahora                            │           │
│  └──────────────────────────────────────────────────┘           │
│                                                                   │
├─────────────────────────────────────────────────────────────────┤
│ © 2026 Hospital Information System...                           │
└─────────────────────────────────────────────────────────────────┘
```

## 6. Flujo de Navegación Completo

```
                    ┌─────────────────┐
                    │  LANDING PAGE   │
                    │      (/)        │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
         [Login]        [Register]    [Ver Servicios]
              │              │
              ↓              ↓
          ┌────────┐    ┌──────────┐
          │ LOGIN  │    │ REGISTER │
          │/login  │    │ /register│
          └───┬────┘    └────┬─────┘
              │              │
       ┌──────┴──────┐       │
       │             │       │
   ┌───▼──┐   ┌──────▼──┐   │
   │ ADMIN│   │ USER    │   │
   │/admin│   │ /user   │◄──┘
   └──────┘   └─────────┘
       │
    [Logout]
       │
       └─────► LANDING PAGE (/)
```

## 7. Características por Página

### Landing Page (/)
- ✅ Header con navegación
- ✅ Hero section
- ✅ Grid de 6 servicios
- ✅ Info de hospital
- ✅ Catálogo de especialidades
- ✅ Footer
- ✅ Responsive design

### Login (/login)
- ✅ Header con navegación
- ✅ Formulario de login
- ✅ Validación de campos
- ✅ Manejo de errores
- ✅ Link a registro
- ✅ Datos de prueba

### Register (/register)
- ✅ Header con navegación
- ✅ Formulario completo
- ✅ Validación de campos
- ✅ Manejo de errores
- ✅ Link a login
- ✅ Campos de nombre y apellido

### User Portal (/user)
- ✅ Header con info de usuario
- ✅ Welcome section
- ✅ Próximas citas
- ✅ Historial médico
- ✅ Documentos
- ✅ Información personal
- ✅ Soporte y ayuda

### Admin Dashboard (/admin)
- ✅ Header con info de admin
- ✅ Estadísticas (4 cards)
- ✅ Usuarios recientes
- ✅ Actividad del sistema
- ✅ Información admin
- ✅ Acciones rápidas
- ✅ Seguridad info

---

**Notas importantes:**
- Todos los botones navegan correctamente
- Responsive en mobile, tablet y desktop
- Protección de rutas por rol
- Logout disponible desde header

