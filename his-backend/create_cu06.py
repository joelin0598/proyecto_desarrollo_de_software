#!/usr/bin/env python3
"""Script to create CU06 Java files with proper UTF-8 encoding (no BOM)."""
import os

BASE = r"C:\GitHub\proyecto_desarrollo_de_software_v2\his-backend\src\main\java\his"

FILES = {}

FILES[os.path.join(BASE, "domain", "ports", "MedicalAppointmentRepository.java")] = """\
package his.domain.ports;

import his.domain.models.MedicalAppointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface MedicalAppointmentRepository {
    MedicalAppointment save(MedicalAppointment appointment);

    boolean existsByPersonalIdAndDateTime(Long personalId, LocalDate fechaCita, LocalTime horaCita);

    List<MedicalAppointment> findAllOrderByDateTimeDesc();

    Optional<MedicalAppointment> findById(Long citaMedicaId);

    /**
     * CU06 / RN09: cola de espera — citas PROGRAMADAS con PAGO_VALIDADO,
     * ordenadas por fecha+hora asc.
     */
    List<MedicalAppointment> findPendingQueueByDoctor(Long personalId);
}
"""

FILES[os.path.join(BASE, "infrastructure", "persistence", "entities", "ClinicalConsultationJpaEntity.java")] = """\
package his.infrastructure.persistence.entities;

import his.domain.models.ClinicalConsultationStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "consulta_clinica")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClinicalConsultationJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consulta_clinica_id")
    private Long consultaClinicaId;

    @Column(name = "cita_medica_id")
    private Long citaMedicaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private PatientJpaEntity paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    private HospitalStaffJpaEntity personal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private ClinicalConsultationStatus estado;

    @Column(name = "evaluacion_fisica", length = 1000)
    private String evaluacionFisica;

    @Column(name = "diagnostico", length = 1000)
    private String diagnostico;

    @Column(name = "orden_laboratorio", length = 1000)
    private String ordenLaboratorio;

    @Column(name = "receta_medica", length = 1000)
    private String recetaMedica;

    @Column(name = "medicacion_prescrita", length = 1000)
    private String medicacionPrescrita;

    @Column(name = "requiere_seguimiento")
    private Boolean requiereSeguimiento;

    @Column(name = "cita_seguimiento_id")
    private Long citaSeguimientoId;
}
"""

FILES[os.path.join(BASE, "infrastructure", "persistence", "repositories", "ClinicalConsultationJpaRepository.java")] = """\
package his.infrastructure.persistence.repositories;

import his.domain.models.ClinicalConsultationStatus;
import his.infrastructure.persistence.entities.ClinicalConsultationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicalConsultationJpaRepository extends JpaRepository<ClinicalConsultationJpaEntity, Long> {

    Optional<ClinicalConsultationJpaEntity> findTopByPersonalPersonalIdAndEstadoOrderByCreatedAtDesc(
            Long personalId, ClinicalConsultationStatus estado);

    List<ClinicalConsultationJpaEntity> findByPacientePacienteIdOrderByCreatedAtDesc(Long pacienteId);
}
"""

FILES[os.path.join(BASE, "infrastructure", "persistence", "adapter", "SqlClinicalConsultationRepository.java")] = """\
package his.infrastructure.persistence.adapter;

import his.domain.models.ClinicalConsultation;
import his.domain.models.ClinicalConsultationStatus;
import his.domain.ports.ClinicalConsultationRepository;
import his.infrastructure.persistence.entities.ClinicalConsultationJpaEntity;
import his.infrastructure.persistence.repositories.ClinicalConsultationJpaRepository;
import his.infrastructure.persistence.repositories.HospitalStaffJpaRepository;
import his.infrastructure.persistence.repositories.PatientJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlClinicalConsultationRepository implements ClinicalConsultationRepository {

    private final ClinicalConsultationJpaRepository jpaRepository;
    private final PatientJpaRepository patientJpaRepository;
    private final HospitalStaffJpaRepository hospitalStaffJpaRepository;

    @Override
    public ClinicalConsultation save(ClinicalConsultation c) {
        var paciente = patientJpaRepository.getReferenceById(c.getPacienteId());
        var personal = hospitalStaffJpaRepository.getReferenceById(c.getPersonalId());

        var entity = ClinicalConsultationJpaEntity.builder()
                .consultaClinicaId(c.getConsultaClinicaId())
                .citaMedicaId(c.getCitaMedicaId())
                .paciente(paciente)
                .personal(personal)
                .estado(c.getEstado())
                .evaluacionFisica(c.getEvaluacionFisica())
                .diagnostico(c.getDiagnostico())
                .ordenLaboratorio(c.getOrdenLaboratorio())
                .recetaMedica(c.getRecetaMedica())
                .medicacionPrescrita(c.getMedicacionPrescrita())
                .requiereSeguimiento(c.getRequiereSeguimiento())
                .citaSeguimientoId(c.getCitaSeguimientoId())
                .build();

        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ClinicalConsultation> findOpenByDoctorId(Long doctorId) {
        return jpaRepository.findTopByPersonalPersonalIdAndEstadoOrderByCreatedAtDesc(
                        doctorId, ClinicalConsultationStatus.EN_CURSO)
                .map(this::toDomain);
    }

    @Override
    public Optional<ClinicalConsultation> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<ClinicalConsultation> findLatestByDoctorAndStatus(Long doctorId, ClinicalConsultationStatus status) {
        return jpaRepository.findTopByPersonalPersonalIdAndEstadoOrderByCreatedAtDesc(doctorId, status)
                .map(this::toDomain);
    }

    private ClinicalConsultation toDomain(ClinicalConsultationJpaEntity e) {
        return ClinicalConsultation.builder()
                .consultaClinicaId(e.getConsultaClinicaId())
                .citaMedicaId(e.getCitaMedicaId())
                .pacienteId(e.getPaciente().getPacienteId())
                .personalId(e.getPersonal().getPersonalId())
                .estado(e.getEstado())
                .evaluacionFisica(e.getEvaluacionFisica())
                .diagnostico(e.getDiagnostico())
                .ordenLaboratorio(e.getOrdenLaboratorio())
                .recetaMedica(e.getRecetaMedica())
                .medicacionPrescrita(e.getMedicacionPrescrita())
                .requiereSeguimiento(e.getRequiereSeguimiento())
                .citaSeguimientoId(e.getCitaSeguimientoId())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
"""

FILES[os.path.join(BASE, "infrastructure", "persistence", "adapter", "SqlMedicalAppointmentRepository.java")] = """\
package his.infrastructure.persistence.adapter;

import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.MedicalAppointment;
import his.domain.models.StatusAppointment;
import his.domain.ports.MedicalAppointmentRepository;
import his.infrastructure.persistence.mapper.MedicalAppointmentMapper;
import his.infrastructure.persistence.repositories.HospitalStaffJpaRepository;
import his.infrastructure.persistence.repositories.MedicalAppointmentJpaRepository;
import his.infrastructure.persistence.repositories.PatientJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlMedicalAppointmentRepository implements MedicalAppointmentRepository {

    private final MedicalAppointmentJpaRepository medicalAppointmentJpaRepository;
    private final PatientJpaRepository patientJpaRepository;
    private final HospitalStaffJpaRepository hospitalStaffJpaRepository;

    @Override
    public MedicalAppointment save(MedicalAppointment appointment) {
        var paciente = patientJpaRepository.getReferenceById(appointment.getPacienteId());
        var personal = hospitalStaffJpaRepository.getReferenceById(appointment.getPersonalId());
        var saved = medicalAppointmentJpaRepository.save(
                MedicalAppointmentMapper.toEntity(appointment, paciente, personal));
        return MedicalAppointmentMapper.toDomain(saved);
    }

    @Override
    public boolean existsByPersonalIdAndDateTime(Long personalId, LocalDate fechaCita, LocalTime horaCita) {
        return medicalAppointmentJpaRepository
                .existsByPersonalPersonalIdAndFechaCitaAndHoraCitaAndIsActiveTrue(personalId, fechaCita, horaCita);
    }

    @Override
    public List<MedicalAppointment> findAllOrderByDateTimeDesc() {
        return medicalAppointmentJpaRepository.findAllByOrderByFechaCitaDescHoraCitaDesc().stream()
                .map(MedicalAppointmentMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<MedicalAppointment> findById(Long citaMedicaId) {
        return medicalAppointmentJpaRepository.findById(citaMedicaId)
                .map(MedicalAppointmentMapper::toDomain);
    }

    @Override
    public List<MedicalAppointment> findPendingQueueByDoctor(Long personalId) {
        return medicalAppointmentJpaRepository
                .findByPersonalPersonalIdAndEstadoCitaAndEstadoAdministrativoAndIsActiveTrueOrderByFechaCitaAscHoraCitaAsc(
                        personalId,
                        StatusAppointment.PROGRAMADA,
                        AdministrativeAppointmentStatus.PAGO_VALIDADO)
                .stream()
                .map(MedicalAppointmentMapper::toDomain)
                .toList();
    }
}
"""

FILES[os.path.join(BASE, "application", "dto", "CloseConsultationRequest.java")] = """\
package his.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CU06 — Solicitud de cierre de consulta clinica (RN13).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CloseConsultationRequest {

    @NotNull(message = "El ID de la consulta es obligatorio")
    private Long consultaClinicaId;

    @NotBlank(message = "La evaluacion fisica es obligatoria")
    private String evaluacionFisica;

    @NotBlank(message = "El diagnostico es obligatorio")
    private String diagnostico;

    private String ordenLaboratorio;

    private String recetaMedica;

    private String medicacionPrescrita;

    private Boolean requiereSeguimiento;
}
"""

FILES[os.path.join(BASE, "application", "dto", "ConsultationResponse.java")] = """\
package his.application.dto;

import his.domain.models.ClinicalConsultationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * CU06 — Respuesta de consulta clinica.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConsultationResponse {
    private Long consultaClinicaId;
    private Long citaMedicaId;
    private Long pacienteId;
    private String pacienteNombre;
    private String pacienteDpi;
    private Long personalId;
    private String medicoNombre;
    private ClinicalConsultationStatus estado;
    private String evaluacionFisica;
    private String diagnostico;
    private String ordenLaboratorio;
    private String recetaMedica;
    private String medicacionPrescrita;
    private Boolean requiereSeguimiento;
    private LocalDateTime createdAt;
    private String fechaCita;
    private String horaCita;
    private String motivoConsulta;
    private String especialidadNombre;
    private String prioridad;
}
"""

FILES[os.path.join(BASE, "application", "dto", "PatientQueueItemResponse.java")] = """\
package his.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CU06 / RN09 — Item de la cola de espera del medico.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientQueueItemResponse {
    private Long citaMedicaId;
    private Long pacienteId;
    private String pacienteNombre;
    private String pacienteDpi;
    private String fechaCita;
    private String horaCita;
    private String motivoConsulta;
    private String especialidadNombre;
    private String prioridad;
    private String estadoAdministrativo;
}
"""

FILES[os.path.join(BASE, "application", "usecases", "ClinicalConsultationUseCase.java")] = """\
package his.application.usecases;

import his.application.dto.CloseConsultationRequest;
import his.application.dto.ConsultationResponse;
import his.application.dto.PatientQueueItemResponse;

import java.util.List;

/**
 * CU06 — Atencion Medica: cola de espera, apertura y cierre de consulta clinica.
 */
public interface ClinicalConsultationUseCase {

    /** RN09 / FA02 — Cola de espera del medico autenticado. */
    List<PatientQueueItemResponse> getPatientQueue(String emailDoctor);

    /** Flujo 1-3: el medico solicita siguiente paciente y abre la consulta. */
    ConsultationResponse openConsultation(Long citaMedicaId, String emailDoctor);

    /** Flujo 4-5 + RN13: el medico cierra la consulta con registro clinico completo. */
    ConsultationResponse closeConsultation(CloseConsultationRequest request, String emailDoctor);

    /** Retorna la consulta EN_CURSO del medico autenticado, si existe. */
    ConsultationResponse getCurrentConsultation(String emailDoctor);
}
"""

FILES[os.path.join(BASE, "application", "services", "ClinicalConsultationService.java")] = """\
package his.application.services;

import his.application.dto.CloseConsultationRequest;
import his.application.dto.ConsultationResponse;
import his.application.dto.PatientQueueItemResponse;
import his.application.usecases.ClinicalConsultationUseCase;
import his.domain.models.ClinicalConsultation;
import his.domain.models.ClinicalConsultationStatus;
import his.domain.models.HospitalStaff;
import his.domain.models.MedicalAppointment;
import his.domain.models.MedicalSpecialityCatalog;
import his.domain.models.Patient;
import his.domain.models.Role;
import his.domain.models.StatusAppointment;
import his.domain.models.VitalSigns;
import his.domain.ports.ClinicalConsultationRepository;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.MedicalAppointmentRepository;
import his.domain.ports.MedicalSpecialityRepository;
import his.domain.ports.PatientRepository;
import his.domain.ports.UserRepository;
import his.domain.ports.VitalSignsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClinicalConsultationService implements ClinicalConsultationUseCase {

    private final ClinicalConsultationRepository consultationRepository;
    private final MedicalAppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final HospitalStaffRepository staffRepository;
    private final VitalSignsRepository vitalSignsRepository;
    private final MedicalSpecialityRepository specialityRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PatientQueueItemResponse> getPatientQueue(String emailDoctor) {
        HospitalStaff doctor = resolveDoctor(emailDoctor);
        List<MedicalAppointment> queue = appointmentRepository.findPendingQueueByDoctor(doctor.getPersonalId());
        if (queue.isEmpty()) {
            log.info("FA02: Cola vacia para doctor personalId={}", doctor.getPersonalId());
        }
        return queue.stream().map(apt -> toQueueItem(apt, doctor)).toList();
    }

    @Override
    @Transactional
    public ConsultationResponse openConsultation(Long citaMedicaId, String emailDoctor) {
        HospitalStaff doctor = resolveDoctor(emailDoctor);

        Optional<ClinicalConsultation> open = consultationRepository.findOpenByDoctorId(doctor.getPersonalId());
        if (open.isPresent()) {
            log.warn("FA01: Doctor personalId={} ya tiene consulta EN_CURSO consultaId={}",
                    doctor.getPersonalId(), open.get().getConsultaClinicaId());
            throw new IllegalStateException(
                    "El medico ya tiene una consulta en curso. Cierre la consulta actual antes de iniciar una nueva.");
        }

        MedicalAppointment cita = appointmentRepository.findById(citaMedicaId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la cita medica con id=" + citaMedicaId));

        if (cita.getEstadoCita() != StatusAppointment.PROGRAMADA) {
            throw new IllegalArgumentException("La cita ya fue atendida o cancelada.");
        }

        Patient patient = patientRepository.findById(cita.getPacienteId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        ClinicalConsultation consultation = ClinicalConsultation.builder()
                .citaMedicaId(citaMedicaId)
                .pacienteId(cita.getPacienteId())
                .personalId(doctor.getPersonalId())
                .estado(ClinicalConsultationStatus.EN_CURSO)
                .requiereSeguimiento(false)
                .build();

        ClinicalConsultation saved = consultationRepository.save(consultation);
        log.info("CU06: Consulta abierta consultaId={} pacienteId={} doctorId={}",
                saved.getConsultaClinicaId(), patient.getPacienteId(), doctor.getPersonalId());

        return toResponse(saved, patient, doctor, cita,
                resolveEspecialidadNombre(cita.getEspecialidadId()),
                resolveLastPriority(cita.getPacienteId()));
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationResponse getCurrentConsultation(String emailDoctor) {
        HospitalStaff doctor = resolveDoctor(emailDoctor);
        ClinicalConsultation open = consultationRepository.findOpenByDoctorId(doctor.getPersonalId()).orElse(null);
        if (open == null) return null;

        Patient patient = patientRepository.findById(open.getPacienteId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
        MedicalAppointment cita = open.getCitaMedicaId() != null
                ? appointmentRepository.findById(open.getCitaMedicaId()).orElse(null) : null;

        return toResponse(open, patient, doctor, cita,
                cita != null ? resolveEspecialidadNombre(cita.getEspecialidadId()) : null,
                resolveLastPriority(patient.getPacienteId()));
    }

    @Override
    @Transactional
    public ConsultationResponse closeConsultation(CloseConsultationRequest req, String emailDoctor) {
        HospitalStaff doctor = resolveDoctor(emailDoctor);

        ClinicalConsultation consulta = consultationRepository.findById(req.getConsultaClinicaId())
                .orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada: " + req.getConsultaClinicaId()));

        if (!consulta.getPersonalId().equals(doctor.getPersonalId())) {
            throw new IllegalArgumentException("Esta consulta no pertenece al medico autenticado.");
        }
        if (consulta.getEstado() == ClinicalConsultationStatus.FINALIZADA) {
            throw new IllegalStateException("La consulta ya fue finalizada.");
        }

        consulta.setEvaluacionFisica(req.getEvaluacionFisica());
        consulta.setDiagnostico(req.getDiagnostico());
        consulta.setOrdenLaboratorio(req.getOrdenLaboratorio());
        consulta.setRecetaMedica(req.getRecetaMedica());
        consulta.setMedicacionPrescrita(req.getMedicacionPrescrita());
        consulta.setRequiereSeguimiento(req.getRequiereSeguimiento() != null && req.getRequiereSeguimiento());
        consulta.setEstado(ClinicalConsultationStatus.FINALIZADA);

        if (consulta.getCitaMedicaId() != null) {
            appointmentRepository.findById(consulta.getCitaMedicaId()).ifPresent(cita -> {
                cita.setEstadoCita(StatusAppointment.ATENDIDA);
                appointmentRepository.save(cita);
            });
        }

        ClinicalConsultation closed = consultationRepository.save(consulta);
        log.info("CU06: Consulta cerrada consultaId={} doctorId={} seguimiento={}",
                closed.getConsultaClinicaId(), doctor.getPersonalId(), closed.getRequiereSeguimiento());

        Patient patient = patientRepository.findById(closed.getPacienteId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
        MedicalAppointment cita = closed.getCitaMedicaId() != null
                ? appointmentRepository.findById(closed.getCitaMedicaId()).orElse(null) : null;

        return toResponse(closed, patient, doctor, cita,
                cita != null ? resolveEspecialidadNombre(cita.getEspecialidadId()) : null,
                resolveLastPriority(patient.getPacienteId()));
    }

    private HospitalStaff resolveDoctor(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
        HospitalStaff staff = staffRepository.findByUsuarioId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("No tiene perfil de personal: " + email));
        if (staff.getRol() != Role.DOCTOR) {
            throw new IllegalArgumentException("El usuario autenticado no tiene rol DOCTOR.");
        }
        return staff;
    }

    private String resolveEspecialidadNombre(Long especialidadId) {
        if (especialidadId == null) return null;
        return specialityRepository.findAll().stream()
                .filter(s -> s.getEspecialidadId() != null && s.getEspecialidadId().equals(especialidadId))
                .map(MedicalSpecialityCatalog::getNombre)
                .findFirst()
                .orElse("Especialidad #" + especialidadId);
    }

    private String resolveLastPriority(Long pacienteId) {
        List<VitalSigns> signs = vitalSignsRepository.findAllRecent();
        return signs.stream()
                .filter(v -> v.getPacienteId() != null && v.getPacienteId().equals(pacienteId))
                .max(Comparator.comparing(VitalSigns::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(v -> v.getPriority() != null ? v.getPriority().name() : "SIN_TRIAJE")
                .orElse("SIN_TRIAJE");
    }

    private PatientQueueItemResponse toQueueItem(MedicalAppointment apt, HospitalStaff doctor) {
        Patient patient = patientRepository.findById(apt.getPacienteId()).orElse(null);
        return PatientQueueItemResponse.builder()
                .citaMedicaId(apt.getCitaMedicaId())
                .pacienteId(apt.getPacienteId())
                .pacienteNombre(patient != null ? patient.getNombreCompleto() : "Paciente #" + apt.getPacienteId())
                .pacienteDpi(patient != null ? patient.getDpi() : null)
                .fechaCita(apt.getFechaCita() != null ? apt.getFechaCita().toString() : null)
                .horaCita(apt.getHoraCita() != null ? apt.getHoraCita().toString() : null)
                .motivoConsulta(apt.getMotivoConsulta())
                .especialidadNombre(resolveEspecialidadNombre(apt.getEspecialidadId()))
                .prioridad(resolveLastPriority(apt.getPacienteId()))
                .estadoAdministrativo(apt.getEstadoAdministrativo() != null ? apt.getEstadoAdministrativo().name() : null)
                .build();
    }

    private ConsultationResponse toResponse(ClinicalConsultation c, Patient patient, HospitalStaff doctor,
                                             MedicalAppointment cita, String especialidadNombre, String prioridad) {
        return ConsultationResponse.builder()
                .consultaClinicaId(c.getConsultaClinicaId())
                .citaMedicaId(c.getCitaMedicaId())
                .pacienteId(patient.getPacienteId())
                .pacienteNombre(patient.getNombreCompleto())
                .pacienteDpi(patient.getDpi())
                .personalId(doctor.getPersonalId())
                .medicoNombre(doctor.getNombreCompleto())
                .estado(c.getEstado())
                .evaluacionFisica(c.getEvaluacionFisica())
                .diagnostico(c.getDiagnostico())
                .ordenLaboratorio(c.getOrdenLaboratorio())
                .recetaMedica(c.getRecetaMedica())
                .medicacionPrescrita(c.getMedicacionPrescrita())
                .requiereSeguimiento(c.getRequiereSeguimiento())
                .createdAt(c.getCreatedAt())
                .fechaCita(cita != null && cita.getFechaCita() != null ? cita.getFechaCita().toString() : null)
                .horaCita(cita != null && cita.getHoraCita() != null ? cita.getHoraCita().toString() : null)
                .motivoConsulta(cita != null ? cita.getMotivoConsulta() : null)
                .especialidadNombre(especialidadNombre)
                .prioridad(prioridad)
                .build();
    }
}
"""

FILES[os.path.join(BASE, "adapters", "rest", "ClinicalConsultationController.java")] = """\
package his.adapters.rest;

import his.application.dto.CloseConsultationRequest;
import his.application.dto.ConsultationResponse;
import his.application.dto.PatientQueueItemResponse;
import his.application.usecases.ClinicalConsultationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Consulta Clinica (CU06)", description = "Atencion medica: cola de espera, apertura y cierre de consulta")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/consultations")
@RequiredArgsConstructor
public class ClinicalConsultationController {

    private final ClinicalConsultationUseCase useCase;

    @Operation(summary = "Cola de espera del medico autenticado (RN09)")
    @GetMapping("/queue")
    public ResponseEntity<List<PatientQueueItemResponse>> getQueue() {
        return ResponseEntity.ok(useCase.getPatientQueue(getAuthenticatedEmail()));
    }

    @Operation(summary = "Consulta EN_CURSO del medico autenticado")
    @GetMapping("/current")
    public ResponseEntity<ConsultationResponse> getCurrent() {
        ConsultationResponse resp = useCase.getCurrentConsultation(getAuthenticatedEmail());
        if (resp == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(resp);
    }

    @Operation(summary = "Abrir consulta para una cita PROGRAMADA (FA01)")
    @PostMapping("/open/{citaMedicaId}")
    public ResponseEntity<ConsultationResponse> open(@PathVariable Long citaMedicaId) {
        return ResponseEntity.ok(useCase.openConsultation(citaMedicaId, getAuthenticatedEmail()));
    }

    @Operation(summary = "Cerrar consulta con registro clinico completo (RN13, FA03)")
    @PatchMapping("/close")
    public ResponseEntity<ConsultationResponse> close(@Valid @RequestBody CloseConsultationRequest request) {
        return ResponseEntity.ok(useCase.closeConsultation(request, getAuthenticatedEmail()));
    }

    private String getAuthenticatedEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
"""

for path, content in FILES.items():
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        f.write(content)
    print(f"OK: {os.path.basename(path)}")

print("All files created successfully.")

