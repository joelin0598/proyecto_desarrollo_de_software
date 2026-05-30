# Plan de Pruebas de Software

**Sistema:** Sistema de Información Hospitalaria – HIS  
**Caso de Uso:** 0.0 CU Portal  
**Fecha:** 27/05/2026  

---

## Historial de versiones
| Fecha     | Versión | Autor       | Organización | Descripción              |
|-----------|---------|-------------|--------------|--------------------------|
| 27-05-26  | 1.0     | Jonathan G. | UMG          | Prueba de Calidad CU 0.0 |
|           |         |             |              |                          |

---

## Información del proyecto
- **Empresa / Organización:** UMG  
- **Proyecto:** Sistema de Información Hospitalaria – HIS  
- **Fecha de preparación:** 27-05-2026  
- **Cliente:** Hospital  
- **Patrocinador principal:** HIS Hospital Team  
- **Gerente / Líder de proyecto:** Jonathan Guamuch  
- **Gerente / Líder de pruebas de software:** Jonathan Guamuch  

---

## Aprobaciones
| Nombre y Apellido | Cargo       | Departamento / Organización | Fecha  | Firma |
|-------------------|-------------|-----------------------------|--------|-------|
| Edy Ramirez       | Catedrático | UMG                         | 30-05  |       |

---

## Resumen ejecutivo
El presente documento constituye el Plan de Pruebas de Software detallado para el Portal del Paciente (WEB), siendo el componente inicial de la UI en el Sistema de Información Hospitalaria (HIS). El propósito fundamental de este plan es definir, estructurar y guiar la estrategia de control de calidad (QA) para asegurar que se cumplan con los requisitos técnicos, funcionales y de seguridad.

---

## Alcance de las pruebas
- **Flujo Básico:** Autenticación y navegación del Panel Personal de salud.  
- **Flujos Alternos:** Control de credenciales incorrectas, caducidad automática de sesión por inactividad, formulario de registro de nuevos pacientes.  

### Elementos de prueba
- Componente de Interfaz Web  
- Módulo de Navegación del Expediente Clínico  

### Nuevas funcionalidades a probar
- Ingreso y despliegue de la pantalla de bienvenida  
- Formulario de registro para pacientes nuevos  
- Autenticación  
- Navegación del expediente clínico  

---

## Casos de Prueba

### Elementos del caso de uso
| Elemento            | Caso de Prueba                                                                 |
|----------------------|--------------------------------------------------------------------------------|
| **Datos de entrada** | Validar obligatoriedad de campos: Nombre, DPI, Fecha de Nacimiento, Teléfono, Correo, Emergencia |
| **Reglas de negocio**| RN01, RN08                                                                    |
| **Flujos alternos**  | FA01 (Credenciales Incorrectas), FA02 (Sesión Cerrada), FA03 (Registro Nuevo Paciente) |
| **Flujos de excepción** | FE01 (Bypass de Seguridad), RN11 (Unicidad de Identidad Profesional)        |
| **Flujo básico**     | Flujo Básico 0.0 CU Portal                                                    |
| **Generalidades**    | Valores predefinidos en scripts de prueba (Ej: DPI: 2026101010101, Monto: Q175.00) |

---

### Caso de Prueba No. 0.0
- **Versión de ejecución:** 1.0  
- **Fecha de ejecución:** 27-05-2026  
- **Caso de uso:** Portal – Sistema HIS  
- **Módulo del sistema:** Portal Web - Registro Paciente Nuevo  
- **Descripción:** Validar registro de nuevo paciente (FA03), verificando obligatoriedad de datos personales, emergencia y carga dinámica de aseguradoras (RN08).  

#### Precondiciones
- DPI y correo no deben existir previamente en la base de datos (RN11).  

#### Pasos de la prueba
1. Ingresar al Portal Web: `https://victorious-sky-00d51bd10.7.azurestaticapps.net/register`  
2. Seleccionar opción "Registrarse".  
3. Diligenciar datos personales: Nombre, DPI, Fecha de Nacimiento, Género, Teléfono, Correo, Dirección.  
4. Introducir contraseña válida (mínimo 6 caracteres).  
5. Ingresar información de emergencia: Nombre contacto y Teléfono.  
6. Verificar listado dinámico de aseguradoras.  
7. Presionar "Crear cuenta" y validar redirección al dashboard.  

#### Resultados
- **Hallazgos:** Ninguno  
- **Veredicto:** Aprobado  
- **Observaciones:** RN01 opera correctamente para salvaguardar confidencialidad clínica.  
- **Analista QA:** Jonathan Guamuch  
- **Firma:** Jonathan Guamuch – 27-05-2026  

---

### Caso de Prueba No. 0.1 – Autenticación
- **Versión de ejecución:** 1.0  
- **Fecha de ejecución:** 27-05-2026  
- **Caso de uso:** Portal – Sistema HIS  
- **Módulo del sistema:** Portal Web - Autenticación  
- **Descripción:** Validar acceso a módulos clínicos y administrativos mediante usuario y contraseña.  

#### Precondiciones
- Estar registrado como paciente activo en HIS.  

#### Pasos de la prueba
1. Ingresar al Portal Web: `https://victorious-sky-00d51bd10.7.azurestaticapps.net/`  
2. Seleccionar opción "Iniciar sesión".  
3. Validar acceso a opciones: agendar cita, ver expediente, consultar resultados.  

#### Resultados
- **Veredicto:** Aprobado  
- **Observaciones:** RN01 opera correctamente.  
- **Analista QA:** Jonathan Guamuch  
- **Firma:** Jonathan Guamuch – 27-05-2026  

---

### Caso de Prueba No. 0.1 – Credenciales Incorrectas
- **Versión de ejecución:** 1.0  
- **Fecha de ejecución:** 27-05-2026  
- **Caso de uso:** Portal – Sistema HIS  
- **Módulo del sistema:** Portal Web - Autenticación  
- **Descripción:** Validar que el sistema detenga el flujo de inicio de sesión ante credenciales incorrectas.  

#### Precondiciones
- Tener sesión activa.  
- Estar registrado como paciente activo.  

#### Pasos de la prueba
1. Ingresar al Portal Web: `https://victorious-sky-00d51bd10.7.azurestaticapps.net/`  
2. Seleccionar "Iniciar sesión" y colocar contraseña incorrecta.  
3. Verificar que no se permita acceso.  

#### Resultados
- **Veredicto:** Aprobado  
- **Observaciones:** RN01 opera correctamente.  
- **Analista QA:** Jonathan Guamuch  
- **Firma:** Jonathan Guamuch – 27-05-2026  
