package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalPrescription {

    private Long recetaMedicaId;
    private Long citaMedicaDetalleId;
    private String instruccionesGenerales;
    private LocalDate fechaEmision;
    private LocalDateTime createdAt;
}
