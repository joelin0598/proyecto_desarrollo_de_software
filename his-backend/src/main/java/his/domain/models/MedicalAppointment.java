package his.domain.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicalAppointment {
    private Long citaMedicaId;
    private Long pacienteId;
    private Long personalId;
    private Long especialidadId;
    private LocalDate fechaCita;
    private LocalTime horaCita;
    private String motivoConsulta;
    private PaymentOption metodoPago;
    private Double costoConsulta;
    private StatusAppointment estadoCita;
    private AdministrativeAppointmentStatus estadoAdministrativo;
    private String observacionAdministrativa;

    // Consolidación CU02/CU04/CU06
    private Boolean solvenciaPago;
    private Boolean citaProgramada;
    private String codigoCita;
    private String qrContenido;

    // Signos vitales + clasificación (antes en signos_vitales)
    private Integer presionSistolica;
    private Integer presionDiastolica;
    private Integer frecuenciaCardiaca;
    private Double temperatura;
    private Integer saturacionOxigeno;
    private Double tallaCm;
    private Double pesoKg;
    private Priority prioridad;
    private Boolean alertaEmergencia;
    private LocalDateTime fechaHoraTriaje;
}