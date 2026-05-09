package his.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HospitalStaff {
    private Long personalId;
    private Long usuarioId;
    private Long especialidadId;
    private Long unidadAtencionId;
    private Role rol;
    private String numeroColegiado;
    private String telefonoCorporativo;
    private String nombreCompleto;
    private String direccion;

    public void validateNumeroColegiadoIfPresent() {
        if (numeroColegiado == null || numeroColegiado.isBlank()) {
            return;
        }
        if (numeroColegiado.length() > 20) {
            throw new IllegalArgumentException("El numero de colegiado no puede exceder 20 caracteres");
        }
    }
}
