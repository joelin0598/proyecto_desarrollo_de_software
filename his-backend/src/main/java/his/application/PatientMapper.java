package his.application;

import his.application.dto.PatientResponse;
import his.domain.Patient;

/**
 * Utilidad de mapeo para la entidad Patient → PatientResponse.
 * Centraliza la lógica de conversión para evitar duplicación entre servicios.
 */
public final class PatientMapper {

    private PatientMapper() {
        // utility class
    }

    public static PatientResponse toResponse(Patient patient) {
        return PatientResponse.builder()
                .patientId(patient.getPatientId())
                .fullName(patient.getFullName())
                .dpi(patient.getDpi())
                .birthDate(patient.getBirthDate())
                .gender(patient.getGender())
                .phone(patient.getPhone())
                .email(patient.getEmail())
                .address(patient.getAddress())
                .emergencyContactName(patient.getEmergencyContactName())
                .emergencyContactPhone(patient.getEmergencyContactPhone())
                .insurancePolicyNumber(patient.getInsurancePolicyNumber())
                .insuranceProvider(patient.getInsuranceProvider())
                .createdAt(patient.getCreatedAt())
                .updatedAt(patient.getUpdatedAt())
                .build();
    }
}
