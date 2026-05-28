# proyecto_desarrollo_de_software

Repositorio principal del sistema HIS (Hospital Information System).

## Estructura principal

- `.github/workflows/`: pipelines CI/CD.
- `his-develop/`: backend Spring Boot + frontend React/Vite.
- `AGENTS.md`: guía para asistentes/agents de desarrollo.

## Deploy backend a Azure Web App

El pipeline `main_hospitalinformationsystem.yml` despliega el backend Java cuando hay push a `main`.

### Requisitos

- Web App Linux/Java creada en Azure (app: `HospitalInformationSystem`).
- Secrets de GitHub configurados:
  - `AZUREAPPSERVICE_CLIENTID_DF28B06C85D64059BA43A0D502EE0747`
  - `AZUREAPPSERVICE_TENANTID_0604DD53F1B649DE89F575FAB6F38E6E`
  - `AZUREAPPSERVICE_SUBSCRIPTIONID_4C881B4B2AC942F8BD7AE94FED479E41`

### Flujo del pipeline

1. Compila en `his-develop/` con `./mvnw clean package`.
2. Publica artifact `his-develop/target/*.jar`.
3. Despliega el JAR a Azure Web App (slot `Production`).

### Checklist rápida de éxito

- `main_hospitalinformationsystem.yml` en rama `main`.
- Secrets válidos y no expirados.
- App Settings de Azure alineadas con backend (DB/JWT/CORS).
- Build verde en GitHub Actions y deploy completado sin errores.

