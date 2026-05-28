# AGENTS.md

## Alcance y punto de entrada
- El código activo está en `his-develop/` (backend Spring Boot + frontend React/Vite).
- `his-develop/frontend/app/` parece un scaffold Vite aislado; la app real usa `his-develop/frontend/src/`.
- Arquitectura por capas en backend: `adapters` (REST) -> `application` (usecases/services/dto) -> `domain` (models/ports) -> `infrastructure` (security/persistence).

## Mapa rápido del sistema
- API backend: `his-develop/src/main/java/his` con seguridad JWT stateless (`infrastructure/security/SecurityConfig.java`, `JwtFilter.java`).
- Frontend: rutas y guards en `his-develop/frontend/src/App.tsx`, sesión en `context/AuthContext.tsx`, llamadas HTTP centralizadas en `services/api.ts`.
- Persistencia: puertos de dominio (`domain/ports/*Repository.java`) implementados por adaptadores SQL (`infrastructure/persistence/adapter/Sql*Repository.java`) usando JPA entities + mappers.
- Casos de uso clínicos clave: triaje (`TriageService`, `PatientFlowService`), citas (`AppointmentService`), atención médica en curso (`MedicalAppointmentAttentionService`).

## Flujos críticos (cómo se conectan frontend y backend)
- Auth: frontend guarda `token`/`user` en `sessionStorage`; interceptor axios agrega `Authorization` y emite `auth:unauthorized` al recibir 401 (`services/api.ts`).
- Guardas de ruta: `ProtectedRoute`/`PublicOnlyRoute` redirigen por rol usando `getDefaultRouteForRole` (`/admin` para personal, `/portal` para paciente).
- Triaje CU02: UI principal en `pages/TriageIntake.tsx`; para walk-in hace `triageAPI.register` y luego `triageAPI.create`; para cita usa solo `triageAPI.create` con `citaMedicaId`.
- Backend mantiene compatibilidad de endpoints: triaje legacy en `POST /api/triage` y flujo nuevo en `POST /api/patients/triage` (`TriageController.java`, `PatientFlowController.java`).
- Atención médica CU06: cola priorizada `GET /api/appointments/attention/queue`, abrir `POST /open`, cerrar `PATCH /{id}/close` (doctor-only).

## Convenciones específicas del proyecto
- Roles exactos (en mayúsculas) definidos en `domain/models/Role.java`; frontend usa `HOSPITAL_STAFF_ROLES` (`services/api.ts`).
- La validación de prioridad clínica se replica en frontend para UX (`resolvePriority`), pero la fuente de verdad sigue en backend (`PatientFlowService.calculatePriority`, `TriageService.calculatePriority`).
- Muchas respuestas de error usan `ErrorResponse.errorMessage`; en frontend se lee `errorMessage` y fallback `message`.
- Estilos y shell administrativo reutilizan `AdminSidebar` + `useSidebarPreference` (estado en `sessionStorage` con prefijo `his:sidebar:`).
- Alias `@` apunta a `frontend/src` (`vite.config.ts`, `frontend/tsconfig.json`).

## Workflows de desarrollo (verificados por configuración)
- Backend (desde `his-develop/`): `./mvnw spring-boot:run`, tests `./mvnw test`.
- Frontend (desde `his-develop/frontend/`): `npm install`, `npm run dev`, `npm run build`, `npm run test`.
- Frontend espera API en `VITE_API_URL` o fallback `http://localhost:8080/api` (`services/api.ts`); Vite también proxya `/api` a `http://localhost:8080` (`vite.config.ts`).
- Tests backend de integración usan perfil `test` + H2 en memoria (`src/test/resources/application-test.properties`, `@ActiveProfiles("test")`).

## Integraciones y dependencias externas
- Backend: Spring Boot 4, Security, JPA, PostgreSQL runtime, H2 tests, OpenAPI (`pom.xml`).
- Auth JWT con blacklist de logout (`AuthController.logout`, `TokenBlacklistService`, `JwtFilter`).
- Validación de pagos es simulada (tarjeta/seguro) en `PaymentValidationService`; reglas de negocio dependen de terminaciones de tarjeta y póliza.
- Hay scripts SQL en `src/main/resources/db/migration/`, pero además existe reparación runtime de constraints en `DbConstraintRepairRunner` (PostgreSQL).

## Precauciones para agentes
- No asumir que "triaje" es un único endpoint: revisar ambos controladores antes de cambiar contratos.
- No romper compatibilidad de rutas legacy (`/api/auth/register/admin`, `/api/triage`).
- Mantener consistencia de enums backend/frontend (`Role`, estados de cita, prioridad, método de pago) para evitar errores 400/403.
- Evitar tocar secretos hardcodeados en `application.properties` salvo petición explícita de refactor de configuración.

