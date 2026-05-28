package his.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorOptionResponse {
    private Long personalId;
    private String nombreCompleto;
    private Long especialidadId;
    private String numeroColegiado;
}

