package his.domain.ports;


import his.domain.models.VitalSigns;

public interface VitalSignsRepository {
    VitalSigns save(VitalSigns vitalSigns);
}
