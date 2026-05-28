package his.domain.ports;

import his.domain.models.MedicalAppointmentDetails;

import java.util.Optional;

public interface MedicalAppointmentDetailsRepository {
    MedicalAppointmentDetails save(MedicalAppointmentDetails details);

    Optional<MedicalAppointmentDetails> findById(Long medicalAppointmentDetailsId);

    Optional<MedicalAppointmentDetails> findByCitaMedicaId(Long citaMedicaId);

    void deleteById(Long medicalAppointmentDetailsId);
}

