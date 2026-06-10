package his.application.dto;

import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.LaboratoryOrderStatus;
import his.domain.models.PatientGender;
import his.domain.models.Priority;
import his.domain.models.StatusAppointment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponse {

	private Long patientId;
	private String nombreCompleto;
	private String dpi;
	private PatientGender genero;
	private LocalDate fechaNacimiento;
	private String telefono;
	private String direccion;

	private List<AppointmentHistoryItem> appointments;
	private List<TriageHistoryItem> triages;
	private List<PrescriptionHistoryItem> prescriptions;
	private List<LaboratoryHistoryItem> laboratoryResults;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AppointmentHistoryItem {
		private Long citaMedicaId;
		private LocalDate fechaCita;
		private LocalTime horaCita;
		private String motivoConsulta;
		private StatusAppointment estadoCita;
		private AdministrativeAppointmentStatus estadoAdministrativo;
		private Boolean solvenciaPago;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TriageHistoryItem {
		private Long citaMedicaId;
		private Priority prioridad;
		private Boolean alertaEmergencia;
		private Integer presionSistolica;
		private Integer presionDiastolica;
		private Integer frecuenciaCardiaca;
		private Double temperatura;
		private Integer saturacionOxigeno;
		private Double pesoKg;
		private Double tallaCm;
		private LocalDateTime fechaHoraTriaje;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PrescriptionHistoryItem {
		private Long recetaMedicaId;
		private Long citaMedicaDetalleId;
		private LocalDate fechaEmision;
		private String instruccionesGenerales;
		private LocalDateTime createdAt;
		private List<PrescriptionDetailItem> items;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PrescriptionDetailItem {
		private Long recetaMedicaDetalleId;
		private Long medicamentoId;
		private String medicamentoNombre;
		private Integer cantidad;
		private String dosis;
		private String viaAdministracion;
		private Integer frecuenciaHoras;
		private Integer duracionDias;
		private Boolean despachado;
		private Boolean pagoValidado;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class LaboratoryHistoryItem {
		private Long ordenLaboratorioId;
		private Long citaMedicaDetalleId;
		private String nombreExamen;
		private LaboratoryOrderStatus estado;
		private Boolean pagoValidado;
		private String etiquetaId;
		private Boolean alertaCritica;
		private String observacionesTecnico;
		private LocalDateTime createdAt;
		private LaboratoryResultResponse resultado;
	}
}


