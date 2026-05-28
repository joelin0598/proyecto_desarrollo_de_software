-- =============================================================================
-- Migracion V8: rollback de paciente_contacto
-- Reintegra contacto de emergencia en tabla paciente y elimina paciente_contacto
-- =============================================================================

UPDATE paciente p
SET
    contacto_emergencia = COALESCE(NULLIF(TRIM(p.contacto_emergencia), ''), pc.nombre_contacto),
    telefono_emergencia = COALESCE(NULLIF(TRIM(p.telefono_emergencia), ''), pc.telefono_contacto),
    updated_at = NOW()
FROM paciente_contacto pc
WHERE pc.paciente_id = p.paciente_id;

DROP TABLE IF EXISTS paciente_contacto;

