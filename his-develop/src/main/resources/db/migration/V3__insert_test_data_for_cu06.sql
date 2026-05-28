-- =============================================================================
-- Migración V3: Datos de prueba para CU06 (Atención Médica - Simple)
--
-- Este script crea citas de prueba directamente sin requerir usuarios previos.
-- Los usuarios deben ser creados a través de la API o manualmente.
-- =============================================================================

-- NOTA: Este script asume que ya existen datos setup from CU03/CU04.
-- Si no hay pacientes o doctors, éste será un no-op.

-- Insertar cita de prueba si no existe paciente con doctor
INSERT INTO cita_medica (
  paciente_id,
  personal_id,
  especialidad_id,
  fecha_cita,
  hora_cita,
  motivo_consulta,
  estado_cita,
  estado_administrativo,
  metodo_pago,
  costo_consulta,
  pago_validado,
  created_at,
  updated_at
)
SELECT
  p.paciente_id,
  COALESCE(ph.personal_id, (SELECT personal_id FROM personal_hospitalario LIMIT 1)),
  1 AS especialidad_id,
  (CURRENT_DATE + INTERVAL '2 days')::date AS fecha_cita,
  '09:00:00'::time AS hora_cita,
  'Control general - prueba CU06',
  'PROGRAMADA'::estado_cita,
  'PAGO_VALIDADO'::estado_administrativo,
  'TARJETA'::metodo_pago,
  175.00 AS costo_consulta,
  true AS pago_validado,
  NOW() AS created_at,
  NOW() AS updated_at
FROM paciente p
LEFT JOIN personal_hospitalario ph ON ph.personal_id = 1
WHERE p.paciente_id = (SELECT paciente_id FROM paciente LIMIT 1)
  AND NOT EXISTS (
    SELECT 1 FROM cita_medica cm
    WHERE cm.paciente_id = p.paciente_id
      AND cm.estado_cita = 'PROGRAMADA'
      AND cm.estado_administrativo = 'PAGO_VALIDADO'
  );






