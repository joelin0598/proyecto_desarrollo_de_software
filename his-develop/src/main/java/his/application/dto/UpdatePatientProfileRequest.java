package his.application.dto;

import his.domain.models.PatientGender;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePatientProfileRequest {

	@Pattern(regexp = "^$|^[0-9]{8,15}$", message = "El telefono debe contener solo numeros (8-15 digitos)")
	private String telefono;

	@Size(max = 255, message = "La direccion no puede exceder 255 caracteres")
	private String direccion;

	private PatientGender genero;
}


