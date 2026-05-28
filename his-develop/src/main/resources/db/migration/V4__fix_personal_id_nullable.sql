-- =============================================================================
-- Migración V4: Permitir personal_id NULL en cita_medica (CU06 - Cancelar atención walk-in)
--
-- Propósito: Permitir que las citas walk-in (citaProgramada = false) vuelvan
-- a la cola general sin quedar vinculadas a un médico específico.
-- =============================================================================

-- Permitir que personal_id sea NULL en la tabla cita_medica
ALTER TABLE cita_medica ALTER COLUMN personal_id DROP NOT NULL;

