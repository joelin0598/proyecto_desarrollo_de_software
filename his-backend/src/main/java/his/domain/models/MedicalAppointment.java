package his.domain.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalAppointment {
    private Long citaMedicaId;
    private Long pacienteId;
    private Long personalId;
    //private Long signosVitalesId; //Se invirtió la relación
    private LocalDate fechaCita;
    private LocalTime horaCita;
    private String estadoCita;
    private StatusAppointment pagoValidado;
    private Boolean recordatorioCita;
}
