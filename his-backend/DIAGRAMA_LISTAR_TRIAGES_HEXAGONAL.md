# Diagrama de Procesos - Listar Triages (Arquitectura Hexagonal)

Este documento describe el flujo de implementacion de la funcionalidad **Listar Triages Recientes** orientado a capas y puertos/adaptadores en arquitectura hexagonal.

## 1) Vista por capas (Hexagonal)

```mermaid
flowchart LR
    subgraph Cliente[Cliente]
        UI[Frontend Admin\nBoton: Listar Triajes]
    end

    subgraph AdaptadoresEntrada[Adaptadores de Entrada]
        CTRL[TriageController\nGET /api/triage]
    end

    subgraph Aplicacion[Capa de Aplicacion]
        UC[TriageUseCase\nlistarTriajesRecientes()]
        SVC[TriageService\nOrquesta y mapea DTO]
        DTO[TriageListItemsResponse]
    end

    subgraph Dominio[Capa de Dominio]
        PORT_VS[Puerto: VitalSignsRepository]
        PORT_PT[Puerto: PatientRepository]
        MODEL_VS[Modelo: VitalSigns]
        MODEL_PT[Modelo: Patient]
    end

    subgraph AdaptadoresSalida[Adaptadores de Salida]
        SQL_VS[SqlVitalSignsRepository]
        SQL_PT[SqlPatientRepository]
        MAP[Mapper: VitalSignsMapper]
    end

    subgraph Infra[Infraestructura]
        JPA_VS[VitalSignsJpaRepository\nfindAllByOrderByCreatedAtDesc()]
        JPA_PT[PatientJpaRepository\nfindById()]
        DB[(PostgreSQL)]
    end

    UI --> CTRL
    CTRL --> UC
    UC --> SVC
    SVC --> PORT_VS
    SVC --> PORT_PT

    PORT_VS --> SQL_VS
    PORT_PT --> SQL_PT

    SQL_VS --> JPA_VS --> DB
    SQL_PT --> JPA_PT --> DB

    SQL_VS --> MAP --> MODEL_VS
    SQL_PT --> MODEL_PT
    SVC --> DTO --> CTRL --> UI
```

## 2) Flujo de ejecucion end-to-end

```mermaid
sequenceDiagram
    autonumber
    participant FE as Frontend (/admin)
    participant C as TriageController
    participant U as TriageUseCase
    participant S as TriageService
    participant VSP as VitalSignsRepository (Puerto)
    participant VS as SqlVitalSignsRepository
    participant VJ as VitalSignsJpaRepository
    participant PP as PatientRepository (Puerto)
    participant PS as SqlPatientRepository
    participant PJ as PatientJpaRepository
    participant DB as PostgreSQL

    FE->>C: GET /api/triage
    C->>U: listarTriajesRecientes()
    U->>S: listarTriajesRecientes()

    S->>VSP: findAllRecent()
    VSP->>VS: implementacion concreta
    VS->>VJ: findAllByOrderByCreatedAtDesc()
    VJ->>DB: SELECT * FROM signos_vitales ORDER BY created_at DESC
    DB-->>VJ: filas
    VJ-->>VS: List<VitalSignsJpaEntity>
    VS-->>S: List<VitalSigns>

    loop Por cada triage
        S->>PP: findById(pacienteId)
        PP->>PS: implementacion concreta
        PS->>PJ: findById()
        PJ->>DB: SELECT * FROM paciente WHERE id=?
        DB-->>PJ: fila paciente
        PJ-->>PS: Optional<PatientJpaEntity>
        PS-->>S: Optional<Patient>
    end

    S-->>C: List<TriageListItemsResponse>
    C-->>FE: 200 OK + JSON
```

## 3) Proceso de implementacion (orden recomendado)

```mermaid
flowchart TD
    A[1. Infra salida\nVitalSignsJpaRepository: findAllByOrderByCreatedAtDesc] -->
    B[2. Adapter salida\nSqlVitalSignsRepository.findAllRecent]
    B --> C[3. Dominio\nPuerto VitalSignsRepository con findAllRecent]
    C --> D[4. Dominio/Mapper\nIncluir createdAt en VitalSigns si se usa en caso de uso]
    D --> E[5. Dominio paciente\nPatientRepository.findById + SqlPatientRepository]
    E --> F[6. Aplicacion\nDTO TriageListItemsResponse]
    F --> G[7. Aplicacion\nTriageUseCase + TriageService.listarTriajesRecientes]
    G --> H[8. Entrada REST\nTriageController GET /api/triage]
    H --> I[9. Tests\nService + Controller + Integracion]
    I --> J[10. Frontend\nBoton y vista Listar Triajes]
```

## 4) Contrato de salida sugerido (JSON)

```json
[
  {
    "signosVitalesId": 25,
    "pacienteId": 11,
    "fechaHoraRegistro": "2026-05-13T14:35:20",
    "nombreCompleto": "Juan Perez",
    "dpi": "1234567890123",
    "prioridad": "ROJO",
    "alertaEmergencia": true,
    "presionSistolica": 80,
    "presionDiastolica": 50,
    "frecuenciaCardiaca": 130,
    "temperatura": 39.2,
    "saturacionOxigeno": 87,
    "pesoKg": 72.5,
    "tallaCm": 168
  }
]
```

## 5) Criterios de aceptacion tecnicos

- El endpoint `GET /api/triage` responde `200 OK` con lista ordenada por `fechaHoraRegistro` descendente.
- La respuesta incluye prioridad y alerta coherentes con signos vitales.
- No rompe el flujo existente de `POST /api/triage`.
- Existen pruebas unitarias de servicio y controlador, mas una prueba de integracion de orden y contenido.
- El frontend puede consumir el contrato sin transformaciones complejas.

