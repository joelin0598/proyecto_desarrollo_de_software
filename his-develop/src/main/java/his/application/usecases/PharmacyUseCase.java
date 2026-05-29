package his.application.usecases;
import his.application.dto.CreatePrescriptionRequest;
import his.application.dto.DispenseMedicineRequest;
import his.application.dto.MedicationReminderResponse;
import his.application.dto.MedicineResponse;
import his.application.dto.PrescriptionResponse;
import java.util.List;
public interface PharmacyUseCase {
    PrescriptionResponse createPrescription(CreatePrescriptionRequest req, String emailDoctor);
    PrescriptionResponse getPrescription(Long citaMedicaDetalleId);
    PrescriptionResponse dispense(DispenseMedicineRequest req, String emailFarmaceutico);
    List<MedicationReminderResponse> getReminders(Long pacienteId);
    List<MedicationReminderResponse> getRemindersByEmail(String emailPaciente);
    List<MedicineResponse> listMedicines();
}