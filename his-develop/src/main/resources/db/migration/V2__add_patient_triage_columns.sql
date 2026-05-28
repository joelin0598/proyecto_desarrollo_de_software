-- =============================================================================
-- Migración V2: Ajustes en tabla paciente para soporte de triaje walk-in (CU 2.0)
--
-- EJECUTAR EN POSTGRESQL antes de usar el endpoint POST /api/triage
-- con pacientes nuevos (walk-in sin cuenta web).
-- =============================================================================

-- 1. usuario_id ahora es nullable → pacientes de triaje presencial sin cuenta web
ALTER TABLE paciente ALTER COLUMN usuario_id DROP NOT NULL;

-- 2. aseguradora_id ahora es nullable → seguro es opcional
ALTER TABLE paciente ALTER COLUMN aseguradora_id DROP NOT NULL;

-- 3. Columna correo de contacto (puede ya existir si Hibernate la agregó)
ALTER TABLE paciente
    ADD COLUMN IF NOT EXISTS email_contacto VARCHAR(150);

-- 4. Columna número de póliza individual (puede ya existir si Hibernate la agregó)
ALTER TABLE paciente
    ADD COLUMN IF NOT EXISTS poliza_seguro VARCHAR(80);
