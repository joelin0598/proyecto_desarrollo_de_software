-- =============================================================================
-- Migracion V7: sincroniza contacto de emergencia legado hacia paciente_contacto
-- =============================================================================

INSERT INTO paciente_contacto (paciente_id, nombre_contacto, telefono_contacto, created_at, updated_at, is_active)
SELECT
    p.paciente_id,
    p.contacto_emergencia,
    p.telefono_emergencia,
    NOW(),
    NOW(),
    TRUE
FROM paciente p
WHERE p.contacto_emergencia IS NOT NULL
  AND p.telefono_emergencia IS NOT NULL
  AND TRIM(p.contacto_emergencia) <> ''
  AND TRIM(p.telefono_emergencia) <> ''
ON CONFLICT (paciente_id)
DO UPDATE SET
    nombre_contacto = EXCLUDED.nombre_contacto,
    telefono_contacto = EXCLUDED.telefono_contacto,
    updated_at = NOW();

