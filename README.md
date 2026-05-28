# Proyecto de Desarrollo de Software

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-6DB33F?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=0B1020)
![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6?logo=typescript&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql&logoColor=white)
![Azure](https://img.shields.io/badge/Azure-Cloud-0078D4?logo=microsoftazure&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-CI/CD-2088FF?logo=githubactions&logoColor=white)

## Vista rápida del stack

<p align="left">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" alt="Java" width="44" height="44" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg" alt="Spring" width="44" height="44" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/react/react-original.svg" alt="React" width="44" height="44" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/typescript/typescript-original.svg" alt="TypeScript" width="44" height="44" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/vitejs/vitejs-original.svg" alt="Vite" width="44" height="44" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/postgresql/postgresql-original.svg" alt="PostgreSQL" width="44" height="44" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/maven/maven-original.svg" alt="Maven" width="44" height="44" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/github/github-original.svg" alt="GitHub" width="44" height="44" />
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/azure/azure-original.svg" alt="Azure" width="44" height="44" />
</p>

## Descripción General
Este repositorio implementa un **Sistema de Información Hospitalaria (HIS)** orientado a la gestión clínica y administrativa de una institución de salud.

Con base en la estructura actual del proyecto, el sistema cubre flujos de:

- autenticación y control de acceso por roles,
- registro de pacientes y triaje,
- gestión y seguimiento de citas,
- atención médica en clínica,
- laboratorio (muestras y resultados),
- farmacia (despacho y recordatorios),
- mantenimiento de usuarios internos.

La solución está organizada como un monorepo con frontend y backend separados, permitiendo escalar y desplegar cada componente en servicios distintos.

## Objetivo del Proyecto
### Objetivos funcionales
- Digitalizar procesos hospitalarios críticos (admisión, consulta, laboratorio y farmacia).
- Reducir tiempos de atención mediante colas y priorización clínica (triaje).
- Mejorar la trazabilidad de pacientes, citas y actos médicos.
- Fortalecer la seguridad y el control de acceso según perfil profesional.

### Objetivos técnicos
- Implementar una arquitectura mantenible por capas/módulos.
- Exponer APIs REST seguras para integración con frontend web.
- Mantener despliegues continuos para backend y frontend en Azure.
- Estandarizar documentación técnica y visual del sistema.

## Tecnologías Utilizadas
### Backend (`his-develop`)
- **Java 17**: lenguaje principal del backend.
- **Spring Boot (starter parent 4.0.4)**: framework principal para servicios REST.
- **Spring Web MVC**: controladores y endpoints HTTP.
- **Spring Security + JWT (jjwt 0.11.5)**: autenticación y autorización stateless.
- **Spring Data JPA**: acceso a datos y repositorios.
- **PostgreSQL**: base de datos principal.
- **Flyway (migraciones SQL en `src/main/resources/db/migration`)**: versionado del esquema.
- **Springdoc OpenAPI**: documentación de API (Swagger).
- **Maven Wrapper (`mvnw`)**: build y ciclo de vida del proyecto.
- **H2 (tests)**: base en memoria para pruebas.

### Frontend (`frontend`)
- **React 18 + TypeScript**: interfaz principal.
- **Vite**: bundler y servidor de desarrollo.
- **React Router DOM**: enrutamiento de vistas.
- **Axios**: consumo de API con interceptores de token.
- **Tailwind CSS + PostCSS**: estilos utilitarios.
- **Vitest + Testing Library + jsdom**: pruebas unitarias de frontend.

### DevOps y plataforma
- **GitHub Actions**: CI/CD.
  - Backend: `main_hospitalinformationsystem.yml`.
  - Frontend: `azure-staticwebapp.yml`.
- **Azure Web App**: despliegue del backend Java.
- **Azure Static Web Apps**: despliegue del frontend React.
- **Git / GitHub**: control de versiones y colaboración.

## Arquitectura del Sistema
El backend sigue una estructura modular por capas con elementos de estilo hexagonal:

- `adapters`: entrada/salida (REST y manejo de excepciones).
- `application`: casos de uso, servicios y DTOs.
- `domain`: modelo de negocio y puertos (interfaces de repositorio).
- `infrastructure`: persistencia JPA, mappers, entidades y seguridad.

Flujo general:

1. El frontend React consume endpoints REST del backend.
2. El backend valida autenticación JWT y permisos por rol.
3. Los casos de uso ejecutan reglas de negocio.
4. Los adaptadores de persistencia guardan/consultan en PostgreSQL.
5. Los módulos clínicos/administrativos se coordinan por estados de cita, triaje y atención.

### Módulos principales
- **Acceso y seguridad**: login, registro, JWT, RBAC.
- **Pacientes y triaje**: admisión, signos vitales, prioridad.
- **Citas y atención médica**: agenda, cola, apertura/cierre de atención.
- **Laboratorio**: órdenes, toma de muestra, resultados.
- **Farmacia**: recetas, inventario, dispensación y recordatorios.
- **Mantenimiento de usuarios**: alta/actualización/suspensión de personal.

## Estructura del Proyecto
```text
proyecto_desarrollo_de_software_v2/
├── .github/
│   └── workflows/                        # Pipelines CI/CD (Azure)
├── documentación/                        # Manuales, diagramas y entregables académicos
├── frontend/                             # Aplicación web React + TypeScript
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   ├── context/
│   │   ├── pages/
│   │   ├── services/                     # Cliente API (axios)
│   │   └── test/
│   └── package.json
└── his-develop/                          # Backend Spring Boot
    ├── src/main/java/his/
    │   ├── adapters/
    │   ├── application/
    │   ├── domain/
    │   └── infrastructure/
    ├── src/main/resources/
    │   ├── application.properties
    │   └── db/migration/                 # Scripts SQL versionados
    ├── src/test/
    ├── pom.xml
    └── mvnw / mvnw.cmd
```

## Instalación y Configuración
> Nota: estos pasos reflejan la configuración observada en el repositorio actual.

### 1) Clonar repositorio
```bash
git clone <URL_DEL_REPOSITORIO>
cd proyecto_desarrollo_de_software_v2
```

### 2) Backend local (Spring Boot)
```bash
cd his-develop
./mvnw clean package
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
Set-Location C:\GitHub\proyecto_desarrollo_de_software_v2\his-develop
.\mvnw.cmd clean package
.\mvnw.cmd spring-boot:run
```

API local esperada: `http://localhost:8080`

### 3) Frontend local (React)
```bash
cd frontend
npm install
npm run dev
```

En Windows PowerShell:

```powershell
Set-Location C:\GitHub\proyecto_desarrollo_de_software_v2\frontend
npm install
npm run dev
```

Frontend local esperado: `http://localhost:5173`

### 4) Variables de entorno recomendadas (frontend)
Ejemplo para apuntar al backend desplegado:

```env
VITE_API_URL=https://<tu-backend-azure>/api
```

### 5) Despliegue en Azure (resumen)
- Backend: pipeline `/.github/workflows/main_hospitalinformationsystem.yml` publica JAR en Azure Web App mediante `publish-profile`.
- Frontend: pipeline `/.github/workflows/azure-staticwebapp.yml` publica en Azure Static Web Apps usando token del recurso.

## Contribución
Para colaborar de forma ordenada:

1. Crear una rama de trabajo (`feature/*`, `fix/*` o `docs/*`).
2. Mantener commits atómicos y descriptivos.
3. Ejecutar pruebas locales antes de abrir PR:

```powershell
Set-Location C:\GitHub\proyecto_desarrollo_de_software_v2\his-develop
.\mvnw.cmd test
```

```powershell
Set-Location C:\GitHub\proyecto_desarrollo_de_software_v2\frontend
npm run test
```

4. Abrir Pull Request con:
   - contexto del cambio,
   - impacto funcional/técnico,
   - evidencia (capturas, logs o resultados de pruebas).
5. Reportar incidencias mediante Issues con pasos de reproducción claros.

## Licencia
Actualmente no se encontró una licencia explícita en la raíz del repositorio (por ejemplo `LICENSE`).

Recomendación: definir una licencia formal (MIT, Apache-2.0 o política institucional) antes de distribución externa.

## Créditos
Desarrollado en el contexto académico/profesional del proyecto **Sistema de Información Hospitalaria (HIS)**.

Reconocimientos a:

- equipo de desarrollo del frontend y backend,
- responsables de análisis y documentación técnica,
- colaboradores de despliegue y pruebas en Azure.

---

Si deseas, este `README.md` puede evolucionar con una sección de "Quick Start" por entorno (`dev`, `test`, `prod`) y una matriz de roles/permisos enlazada a los casos de uso documentados.

