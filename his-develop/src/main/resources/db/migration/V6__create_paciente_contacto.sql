-- =============================================================================
-- Migracion V6: Contacto de emergencia separado por paciente (CU02)
-- =============================================================================

CREATE TABLE IF NOT EXISTS paciente_contacto (
    paciente_contacto_id BIGSERIAL PRIMARY KEY,
    paciente_id BIGINT NOT NULL UNIQUE REFERENCES paciente(paciente_id),
    nombre_contacto VARCHAR(150) NOT NULL,
    telefono_contacto VARCHAR(20) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

