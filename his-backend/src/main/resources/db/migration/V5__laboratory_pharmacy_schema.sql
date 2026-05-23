-- =============================================================================
-- Migración V5: Módulos CU07 (Laboratorio) y CU08 (Farmacia)
-- =============================================================================

-- ─────────────────────────────────────────────────────────────────────────────
-- CU07: LABORATORIO
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS orden_laboratorio (
    orden_laboratorio_id   BIGSERIAL PRIMARY KEY,
    cita_medica_detalle_id BIGINT       NOT NULL REFERENCES cita_medica_detalle(cita_medica_detalle_id),
    personal_id            BIGINT       REFERENCES personal_hospital(personal_id),
    nombre_examen          VARCHAR(200) NOT NULL,
    tipo_muestra           VARCHAR(100),
    estado                 VARCHAR(40)  NOT NULL DEFAULT 'PENDIENTE_PAGO',
    pago_validado          BOOLEAN DEFAULT FALSE,
    etiqueta_id            VARCHAR(80),
    alerta_critica         BOOLEAN DEFAULT FALSE,
    observaciones_tecnico  VARCHAR(500),
    created_at             TIMESTAMP,
    updated_at             TIMESTAMP,
    is_active              BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS resultado_laboratorio (
    resultado_laboratorio_id BIGSERIAL PRIMARY KEY,
    orden_laboratorio_id     BIGINT        NOT NULL REFERENCES orden_laboratorio(orden_laboratorio_id),
    nombre_examen            VARCHAR(200),
    valor_resultado          NUMERIC(12,4),
    unidad_resultado         VARCHAR(50),
    referencia_minima        NUMERIC(12,4),
    referencia_maxima        NUMERIC(12,4),
    observaciones            VARCHAR(500),
    resumen                  VARCHAR(1000),
    conclusion               VARCHAR(1000),
    critico                  BOOLEAN DEFAULT FALSE,
    created_at               TIMESTAMP,
    updated_at               TIMESTAMP,
    is_active                BOOLEAN DEFAULT TRUE
);

-- ─────────────────────────────────────────────────────────────────────────────
-- CU08: FARMACIA
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS medicamento (
    medicamento_id  BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(200) NOT NULL,
    presentacion    VARCHAR(100),
    descripcion     VARCHAR(300),
    stock_actual    INTEGER      NOT NULL DEFAULT 0,
    precio_unitario DOUBLE PRECISION,
    created_at      TIMESTAMP,
    updated_at      TIMESTAMP,
    is_active       BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS receta_medica (
    receta_medica_id       BIGSERIAL PRIMARY KEY,
    cita_medica_detalle_id BIGINT        NOT NULL REFERENCES cita_medica_detalle(cita_medica_detalle_id),
    instrucciones_generales VARCHAR(1000),
    fecha_emision          DATE,
    created_at             TIMESTAMP,
    updated_at             TIMESTAMP,
    is_active              BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS receta_medica_detalle (
    receta_medica_detalle_id BIGSERIAL PRIMARY KEY,
    receta_medica_id         BIGINT      NOT NULL REFERENCES receta_medica(receta_medica_id),
    medicamento_id           BIGINT      NOT NULL REFERENCES medicamento(medicamento_id),
    cantidad                 INTEGER     NOT NULL,
    dosis                    VARCHAR(100),
    via_administracion       VARCHAR(80),
    frecuencia_horas         INTEGER,
    duracion_dias            INTEGER,
    despachado               BOOLEAN DEFAULT FALSE,
    pago_validado            BOOLEAN DEFAULT FALSE,
    created_at               TIMESTAMP,
    updated_at               TIMESTAMP,
    is_active                BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS recordatorio_medicamento (
    recordatorio_id          BIGSERIAL PRIMARY KEY,
    receta_medica_detalle_id BIGINT       NOT NULL REFERENCES receta_medica_detalle(receta_medica_detalle_id),
    paciente_id              BIGINT       NOT NULL REFERENCES paciente(paciente_id),
    medicamento_nombre       VARCHAR(200),
    dosis                    VARCHAR(100),
    frecuencia_horas         INTEGER,
    duracion_dias            INTEGER,
    via_administracion       VARCHAR(80),
    proximo_recordatorio     TIMESTAMP,
    activo                   BOOLEAN DEFAULT TRUE,
    created_at               TIMESTAMP,
    updated_at               TIMESTAMP,
    is_active                BOOLEAN DEFAULT TRUE
);

-- Datos de prueba para medicamento
INSERT INTO medicamento (nombre, presentacion, descripcion, stock_actual, precio_unitario, created_at, updated_at, is_active)
VALUES
  ('Amoxicilina 500mg','Cápsula','Antibiótico de amplio espectro', 200, 2.50, NOW(), NOW(), TRUE),
  ('Ibuprofeno 400mg','Tableta','Antiinflamatorio no esteroide', 150, 1.75, NOW(), NOW(), TRUE),
  ('Losartán 50mg','Tableta','Antihipertensivo', 100, 3.00, NOW(), NOW(), TRUE),
  ('Metformina 850mg','Tableta','Antidiabético oral', 80, 2.20, NOW(), NOW(), TRUE),
  ('Omeprazol 20mg','Cápsula','Inhibidor bomba de protones', 120, 1.50, NOW(), NOW(), TRUE)
ON CONFLICT DO NOTHING;

