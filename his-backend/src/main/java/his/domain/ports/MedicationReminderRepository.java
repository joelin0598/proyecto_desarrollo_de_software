package his.domain.ports;

import his.domain.models.MedicationReminder;

import java.util.List;

public interface MedicationReminderRepository {
    MedicationReminder save(MedicationReminder reminder);
    List<MedicationReminder> findActivosByPacienteId(Long pacienteId);
}

