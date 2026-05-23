-- Script de creación de datos de prueba para CU06
-- Ejecutar con: psql -h localhost -U postgres -d his -f create_test_data.sql

-- 1. Crear cita de prueba si no existe
-- Esta cita estará disponible para que el doctor la atienda en CU06

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
  created_at,
  updated_at
)
VALUES (
  (SELECT paciente_id FROM paciente LIMIT 1),  -- Usa el primer paciente disponible
  (SELECT personal_id FROM personal_hospitalario WHERE rol = 'DOCTOR' LIMIT 1),  -- Usa el primer doctor
  1,  -- Especialidad general
  CURRENT_DATE + INTERVAL '2 days',
  '09:00:00',
  'Control general de salud',
  'PROGRAMADA',
  'PAGO_VALIDADO',
  'TARJETA',
  175.00,
  NOW(),
  NOW()
)
ON CONFLICT DO NOTHING;

-- 2. Verificar que la cita fue creada
SELECT 'Citas disponibles para CU06:' as Resultado;
SELECT
  cm.cita_medica_id,
  p.nombre_completo as Paciente,
  ph.nombre_completo as Doctor,
  cm.fecha_cita,
  cm.hora_cita,
  cm.estado_cita,
  cm.estado_administrativo
FROM cita_medica cm
JOIN paciente p ON cm.paciente_id = p.paciente_id
JOIN personal_hospitalario ph ON cm.personal_id = ph.personal_id
WHERE cm.estado_cita = 'PROGRAMADA'
  AND cm.estado_administrativo = 'PAGO_VALIDADO'
ORDER BY cm.fecha_cita ASC;




