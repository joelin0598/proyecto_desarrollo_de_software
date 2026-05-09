package his.domain.models;

public enum Role {
    PACIENTE,
    ADMIN,
    DOCTOR,
    ENFERMERA,
    LABORATORISTA,
    FARMACEUTICO,
    ADMINISTRATIVO,
    RECEPCION;

    public boolean isPersonalHospitalario() {
        return this != PACIENTE;
    }
}
