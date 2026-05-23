package his.application.services;

import his.application.dto.CloseMedicalAppointmentAttentionRequest;
import his.application.dto.MedicalAppointmentAttentionResponse;
import his.application.dto.MedicalAppointmentQueueItemResponse;
import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.HospitalStaff;
import his.domain.models.MedicalAppointment;
import his.domain.models.MedicalAppointmentDetails;
import his.domain.models.MedicalSpecialityCatalog;
import his.domain.models.Patient;
import his.domain.models.Role;
import his.domain.models.StatusAppointment;
import his.domain.models.User;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.MedicalAppointmentDetailsRepository;
import his.domain.ports.MedicalAppointmentRepository;
import his.domain.ports.MedicalSpecialityRepository;
import his.domain.ports.PatientRepository;
import his.domain.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CU06 — Tests unitarios del servicio de atención médica sobre citas.
 * Cubre: cola de espera (RN09/FA02), apertura (FA01), cierre (RN13/FA03) y validaciones.
 */
@ExtendWith(MockitoExtension.class)
class MedicalAppointmentAttentionServiceTest {

    @Mock private MedicalAppointmentRepository appointmentRepository;
    @Mock private MedicalAppointmentDetailsRepository appointmentDetailsRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private HospitalStaffRepository staffRepository;
    @Mock private MedicalSpecialityRepository specialityRepository;
    @Mock private UserRepository userRepository;

    private MedicalAppointmentAttentionService service;

    private static final String EMAIL_DOCTOR = "doctor@hospital.com";
    private static final Long USER_ID = 1L;
    private static final Long PERSONAL_ID = 10L;
    private static final Long PACIENTE_ID = 20L;
    private static final Long CITA_ID = 100L;
    private static final Long DETALLE_ID = 200L;

    @BeforeEach
    void setUp() {
        service = new MedicalAppointmentAttentionService(
                appointmentRepository,
                appointmentDetailsRepository,
                patientRepository,
                staffRepository,
                specialityRepository,
                userRepository
        );
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers reutilizables
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void cancelCurrentAttention_returnsFalse_whenNoOpenAttention() {
        // Arrange
        stubDoctorResolution();
        when(appointmentRepository.findOpenByDoctor(PERSONAL_ID)).thenReturn(Optional.empty());

        // Act
        boolean cancelled = service.cancelCurrentAttention(EMAIL_DOCTOR);

        // Assert
        assertFalse(cancelled);
        verify(appointmentRepository, never()).save(any());
        verify(appointmentDetailsRepository, never()).deleteById(any());
    }

    @Test
    void cancelCurrentAttention_revertsToProgramada_andKeepsDoctorAssignmentForWalkIn() {
        // Arrange
        stubDoctorResolution();
        MedicalAppointment open = buildProgramada();
        open.setEstadoCita(StatusAppointment.EN_CURSO);
        open.setCitaProgramada(false);
        open.setPersonalId(PERSONAL_ID);
        when(appointmentRepository.findOpenByDoctor(PERSONAL_ID)).thenReturn(Optional.of(open));

        MedicalAppointmentDetails detalle = MedicalAppointmentDetails.builder()
                .medicalAppointmentDetailsId(DETALLE_ID)
                .citaMedicaId(CITA_ID)
                .build();
        when(appointmentDetailsRepository.findByCitaMedicaId(CITA_ID)).thenReturn(Optional.of(detalle));
        when(appointmentRepository.save(any(MedicalAppointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        boolean cancelled = service.cancelCurrentAttention(EMAIL_DOCTOR);

        // Assert
        assertTrue(cancelled);
        verify(appointmentDetailsRepository).deleteById(DETALLE_ID);
        verify(appointmentRepository).save(argThat(cita ->
                cita.getEstadoCita() == StatusAppointment.PROGRAMADA
                        && PERSONAL_ID.equals(cita.getPersonalId())));
    }

    private void stubDoctorResolution() {
        when(userRepository.findByEmail(EMAIL_DOCTOR))
                .thenReturn(Optional.of(User.builder().userId(USER_ID).build()));
        when(staffRepository.findByUsuarioId(USER_ID))
                .thenReturn(Optional.of(HospitalStaff.builder()
                        .personalId(PERSONAL_ID)
                        .usuarioId(USER_ID)
                        .rol(Role.DOCTOR)
                        .nombreCompleto("Dr. García")
                        .build()));
    }

    private MedicalAppointment buildProgramada() {
        return MedicalAppointment.builder()
                .citaMedicaId(CITA_ID)
                .pacienteId(PACIENTE_ID)
                .personalId(PERSONAL_ID)
                .especialidadId(5L)
                .estadoCita(StatusAppointment.PROGRAMADA)
                .estadoAdministrativo(AdministrativeAppointmentStatus.PAGO_VALIDADO)
                .solvenciaPago(true)
                .fechaCita(LocalDate.now().plusDays(1))
                .horaCita(LocalTime.of(9, 0))
                .motivoConsulta("Control general")
                .build();
    }

    private Patient buildPatient() {
        return Patient.builder()
                .pacienteId(PACIENTE_ID)
                .nombreCompleto("Ana Torres")
                .dpi("1234567890123")
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // getPatientQueue — RN09 / FA02
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void getPatientQueue_returnsQueueItems_whenDoctorHasAppointments() {
        // Arrange
        stubDoctorResolution();
        MedicalAppointment cita = buildProgramada();
        when(appointmentRepository.findPendingQueueByDoctor(PERSONAL_ID)).thenReturn(List.of(cita));
        when(patientRepository.findById(PACIENTE_ID)).thenReturn(Optional.of(buildPatient()));
        when(specialityRepository.findAllActive()).thenReturn(List.of(
                MedicalSpecialityCatalog.builder().especialidadMedicaId(5L).nombre("Medicina General").build()));

        // Act
        List<MedicalAppointmentQueueItemResponse> result = service.getPatientQueue(EMAIL_DOCTOR);

        // Assert
        assertEquals(1, result.size());
        assertEquals(CITA_ID, result.get(0).getCitaMedicaId());
        assertEquals("Ana Torres", result.get(0).getPacienteNombre());
        assertEquals("Medicina General", result.get(0).getEspecialidadNombre());
        assertEquals("SIN_TRIAJE", result.get(0).getPrioridad());
    }

    @Test
    void getPatientQueue_returnsPriorityFromAppointment_whenPriorityExists() {
        // Arrange
        stubDoctorResolution();
        MedicalAppointment cita = buildProgramada();
        cita.setPrioridad(his.domain.models.Priority.NARANJA);
        when(appointmentRepository.findPendingQueueByDoctor(PERSONAL_ID)).thenReturn(List.of(cita));
        when(patientRepository.findById(PACIENTE_ID)).thenReturn(Optional.of(buildPatient()));
        when(specialityRepository.findAllActive()).thenReturn(Collections.emptyList());

        // Act
        List<MedicalAppointmentQueueItemResponse> result = service.getPatientQueue(EMAIL_DOCTOR);

        // Assert
        assertEquals("NARANJA", result.get(0).getPrioridad());
    }

    @Test
    void getPatientQueue_returnsEmptyList_whenQueueIsEmpty_FA02() {
        // Arrange — FA02: cola vacía para el doctor
        stubDoctorResolution();
        when(appointmentRepository.findPendingQueueByDoctor(PERSONAL_ID)).thenReturn(Collections.emptyList());

        // Act
        List<MedicalAppointmentQueueItemResponse> result = service.getPatientQueue(EMAIL_DOCTOR);

        // Assert
        assertTrue(result.isEmpty(), "FA02: Cola vacía debe retornar lista vacía");
    }

    @Test
    void getPatientQueue_throwsException_whenUserEmailNotFound() {
        // Arrange
        when(userRepository.findByEmail(EMAIL_DOCTOR)).thenReturn(Optional.empty());

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getPatientQueue(EMAIL_DOCTOR));
        assertTrue(ex.getMessage().contains("no encontrado"));
    }

    @Test
    void getPatientQueue_throwsException_whenStaffHasNoDoctor_role() {
        // Arrange — el usuario autenticado no tiene rol DOCTOR
        when(userRepository.findByEmail(EMAIL_DOCTOR))
                .thenReturn(Optional.of(User.builder().userId(USER_ID).build()));
        when(staffRepository.findByUsuarioId(USER_ID))
                .thenReturn(Optional.of(HospitalStaff.builder()
                        .personalId(PERSONAL_ID)
                        .rol(Role.ENFERMERA)
                        .build()));

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.getPatientQueue(EMAIL_DOCTOR));
        assertTrue(ex.getMessage().contains("DOCTOR"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // openAttention — Flujo principal (RN09 → apertura)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void openAttention_success_changesStatusToEnCurso() {
        // Arrange
        stubDoctorResolution();
        when(appointmentRepository.findOpenByDoctor(PERSONAL_ID)).thenReturn(Optional.empty());

        MedicalAppointment cita = buildProgramada();
        when(appointmentRepository.findById(CITA_ID)).thenReturn(Optional.of(cita));
        when(patientRepository.findById(PACIENTE_ID)).thenReturn(Optional.of(buildPatient()));

        MedicalAppointment citaEnCurso = buildProgramada();
        citaEnCurso.setEstadoCita(StatusAppointment.EN_CURSO);
        when(appointmentRepository.save(any(MedicalAppointment.class))).thenReturn(citaEnCurso);

        MedicalAppointmentDetails detalle = MedicalAppointmentDetails.builder()
                .medicalAppointmentDetailsId(DETALLE_ID)
                .citaMedicaId(CITA_ID)
                .requiereSeguimiento(false)
                .build();
        when(appointmentDetailsRepository.save(any(MedicalAppointmentDetails.class))).thenReturn(detalle);
        when(specialityRepository.findAllActive()).thenReturn(Collections.emptyList());

        // Act
        MedicalAppointmentAttentionResponse response = service.openAttention(CITA_ID, EMAIL_DOCTOR);

        // Assert
        assertNotNull(response);
        assertEquals(StatusAppointment.EN_CURSO, response.getEstado());
        assertEquals(CITA_ID, response.getCitaMedicaId());
        assertEquals(DETALLE_ID, response.getCitaMedicaDetalleId());
        assertEquals("Ana Torres", response.getPacienteNombre());
        verify(appointmentRepository).save(any(MedicalAppointment.class));
        verify(appointmentDetailsRepository).save(any(MedicalAppointmentDetails.class));
    }

    @Test
    void openAttention_throwsIllegalState_whenDoctorAlreadyHasOpenAttention_FA01() {
        // Arrange — FA01: el médico ya tiene una cita en curso
        stubDoctorResolution();
        MedicalAppointment citaAbierta = buildProgramada();
        citaAbierta.setEstadoCita(StatusAppointment.EN_CURSO);
        when(appointmentRepository.findOpenByDoctor(PERSONAL_ID)).thenReturn(Optional.of(citaAbierta));

        // Act + Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.openAttention(CITA_ID, EMAIL_DOCTOR));
        assertTrue(ex.getMessage().contains("en curso"),
                "FA01: debe indicar que ya hay una cita en curso");
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void openAttention_throwsException_whenCitaMedicaNotFound() {
        // Arrange
        stubDoctorResolution();
        when(appointmentRepository.findOpenByDoctor(PERSONAL_ID)).thenReturn(Optional.empty());
        when(appointmentRepository.findById(CITA_ID)).thenReturn(Optional.empty());

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.openAttention(CITA_ID, EMAIL_DOCTOR));
        assertTrue(ex.getMessage().contains("No existe la cita"));
    }

    @Test
    void openAttention_throwsException_whenCitaNotBelongsToDoctor() {
        // Arrange
        stubDoctorResolution();
        when(appointmentRepository.findOpenByDoctor(PERSONAL_ID)).thenReturn(Optional.empty());

        MedicalAppointment citaDeOtroDoctor = buildProgramada();
        citaDeOtroDoctor.setPersonalId(999L); // diferente doctor
        when(appointmentRepository.findById(CITA_ID)).thenReturn(Optional.of(citaDeOtroDoctor));

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.openAttention(CITA_ID, EMAIL_DOCTOR));
        assertTrue(ex.getMessage().contains("no pertenece"));
    }

    @Test
    void openAttention_throwsException_whenCitaIsNotProgramada() {
        // Arrange — cita ya atendida o cancelada
        stubDoctorResolution();
        when(appointmentRepository.findOpenByDoctor(PERSONAL_ID)).thenReturn(Optional.empty());

        MedicalAppointment citaAtendida = buildProgramada();
        citaAtendida.setEstadoCita(StatusAppointment.ATENDIDA);
        when(appointmentRepository.findById(CITA_ID)).thenReturn(Optional.of(citaAtendida));

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.openAttention(CITA_ID, EMAIL_DOCTOR));
        assertTrue(ex.getMessage().contains("atendida o cancelada"));
    }

    @Test
    void openAttention_allowsWalkInFromOtherStaff_andReassignsDoctor() {
        // Arrange
        stubDoctorResolution();
        when(appointmentRepository.findOpenByDoctor(PERSONAL_ID)).thenReturn(Optional.empty());

        MedicalAppointment walkIn = buildProgramada();
        walkIn.setCitaProgramada(false);
        walkIn.setPersonalId(999L); // triaje lo registró otro personal
        when(appointmentRepository.findById(CITA_ID)).thenReturn(Optional.of(walkIn));
        when(patientRepository.findById(PACIENTE_ID)).thenReturn(Optional.of(buildPatient()));

        MedicalAppointment reassigned = buildProgramada();
        reassigned.setCitaProgramada(false);
        reassigned.setEstadoCita(StatusAppointment.EN_CURSO);
        reassigned.setPersonalId(PERSONAL_ID);
        when(appointmentRepository.save(any(MedicalAppointment.class))).thenReturn(reassigned);

        MedicalAppointmentDetails detalle = MedicalAppointmentDetails.builder()
                .medicalAppointmentDetailsId(DETALLE_ID)
                .citaMedicaId(CITA_ID)
                .requiereSeguimiento(false)
                .build();
        when(appointmentDetailsRepository.save(any(MedicalAppointmentDetails.class))).thenReturn(detalle);
        when(specialityRepository.findAllActive()).thenReturn(Collections.emptyList());

        // Act
        MedicalAppointmentAttentionResponse response = service.openAttention(CITA_ID, EMAIL_DOCTOR);

        // Assert
        assertNotNull(response);
        assertEquals(StatusAppointment.EN_CURSO, response.getEstado());
        verify(appointmentRepository).save(any(MedicalAppointment.class));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // getCurrentAttention
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void getCurrentAttention_returnsResponse_whenOpenAttentionExists() {
        // Arrange
        stubDoctorResolution();
        MedicalAppointment citaEnCurso = buildProgramada();
        citaEnCurso.setEstadoCita(StatusAppointment.EN_CURSO);
        when(appointmentRepository.findOpenByDoctor(PERSONAL_ID)).thenReturn(Optional.of(citaEnCurso));
        when(patientRepository.findById(PACIENTE_ID)).thenReturn(Optional.of(buildPatient()));

        MedicalAppointmentDetails detalle = MedicalAppointmentDetails.builder()
                .medicalAppointmentDetailsId(DETALLE_ID)
                .citaMedicaId(CITA_ID)
                .build();
        when(appointmentDetailsRepository.findByCitaMedicaId(CITA_ID)).thenReturn(Optional.of(detalle));
        when(specialityRepository.findAllActive()).thenReturn(Collections.emptyList());

        // Act
        MedicalAppointmentAttentionResponse response = service.getCurrentAttention(EMAIL_DOCTOR);

        // Assert
        assertNotNull(response);
        assertEquals(StatusAppointment.EN_CURSO, response.getEstado());
        assertEquals(CITA_ID, response.getCitaMedicaId());
    }

    @Test
    void getCurrentAttention_returnsNull_whenNoOpenAttention() {
        // Arrange
        stubDoctorResolution();
        when(appointmentRepository.findOpenByDoctor(PERSONAL_ID)).thenReturn(Optional.empty());

        // Act
        MedicalAppointmentAttentionResponse response = service.getCurrentAttention(EMAIL_DOCTOR);

        // Assert
        assertNull(response, "Debe retornar null cuando el médico no tiene cita en curso");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // closeAttention — RN13 / FA03
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void closeAttention_success_changesStatusToAtendida_RN13() {
        // Arrange
        stubDoctorResolution();

        MedicalAppointmentDetails detalle = MedicalAppointmentDetails.builder()
                .medicalAppointmentDetailsId(DETALLE_ID)
                .citaMedicaId(CITA_ID)
                .requiereSeguimiento(false)
                .build();
        when(appointmentDetailsRepository.findById(DETALLE_ID)).thenReturn(Optional.of(detalle));

        MedicalAppointment citaEnCurso = buildProgramada();
        citaEnCurso.setEstadoCita(StatusAppointment.EN_CURSO);
        when(appointmentRepository.findById(CITA_ID)).thenReturn(Optional.of(citaEnCurso));

        MedicalAppointmentDetails detalleCerrado = MedicalAppointmentDetails.builder()
                .medicalAppointmentDetailsId(DETALLE_ID)
                .citaMedicaId(CITA_ID)
                .evaluacionFisica("Paciente estable")
                .diagnostico("Hipertensión leve")
                .requiereSeguimiento(true)
                .build();
        when(appointmentDetailsRepository.save(any(MedicalAppointmentDetails.class))).thenReturn(detalleCerrado);

        MedicalAppointment citaAtendida = buildProgramada();
        citaAtendida.setEstadoCita(StatusAppointment.ATENDIDA);
        when(appointmentRepository.save(any(MedicalAppointment.class))).thenReturn(citaAtendida);

        when(patientRepository.findById(PACIENTE_ID)).thenReturn(Optional.of(buildPatient()));
        when(specialityRepository.findAllActive()).thenReturn(Collections.emptyList());

        CloseMedicalAppointmentAttentionRequest request = CloseMedicalAppointmentAttentionRequest.builder()
                .citaMedicaDetalleId(DETALLE_ID)
                .evaluacionFisica("Paciente estable")
                .diagnostico("Hipertensión leve")
                .recetaMedica("Losartán 50mg")
                .requiereSeguimiento(true)
                .build();

        // Act
        MedicalAppointmentAttentionResponse response = service.closeAttention(request, EMAIL_DOCTOR);

        // Assert
        assertNotNull(response);
        assertEquals(StatusAppointment.ATENDIDA, response.getEstado());
        assertEquals("Hipertensión leve", response.getDiagnostico());
        assertTrue(response.getRequiereSeguimiento());
        verify(appointmentRepository).save(any(MedicalAppointment.class));
        verify(appointmentDetailsRepository).save(any(MedicalAppointmentDetails.class));
    }

    @Test
    void closeAttention_throwsException_whenDetalleNotFound() {
        // Arrange
        stubDoctorResolution();
        when(appointmentDetailsRepository.findById(DETALLE_ID)).thenReturn(Optional.empty());

        CloseMedicalAppointmentAttentionRequest request = CloseMedicalAppointmentAttentionRequest.builder()
                .citaMedicaDetalleId(DETALLE_ID)
                .evaluacionFisica("Examen físico")
                .diagnostico("Diagnóstico")
                .build();

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.closeAttention(request, EMAIL_DOCTOR));
        assertTrue(ex.getMessage().contains("Detalle de cita no encontrado"));
    }

    @Test
    void closeAttention_throwsException_whenCitaNotBelongsToDoctor() {
        // Arrange
        stubDoctorResolution();

        MedicalAppointmentDetails detalle = MedicalAppointmentDetails.builder()
                .medicalAppointmentDetailsId(DETALLE_ID)
                .citaMedicaId(CITA_ID)
                .build();
        when(appointmentDetailsRepository.findById(DETALLE_ID)).thenReturn(Optional.of(detalle));

        MedicalAppointment citaDeOtroDoctor = buildProgramada();
        citaDeOtroDoctor.setPersonalId(888L); // diferente doctor
        citaDeOtroDoctor.setEstadoCita(StatusAppointment.EN_CURSO);
        when(appointmentRepository.findById(CITA_ID)).thenReturn(Optional.of(citaDeOtroDoctor));

        CloseMedicalAppointmentAttentionRequest request = CloseMedicalAppointmentAttentionRequest.builder()
                .citaMedicaDetalleId(DETALLE_ID)
                .evaluacionFisica("Examen físico")
                .diagnostico("Diagnóstico")
                .build();

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.closeAttention(request, EMAIL_DOCTOR));
        assertTrue(ex.getMessage().contains("no pertenece"));
    }

    @Test
    void closeAttention_throwsIllegalState_whenCitaIsNotEnCurso_FA03() {
        // Arrange — FA03: la cita ya fue finalizada previamente
        stubDoctorResolution();

        MedicalAppointmentDetails detalle = MedicalAppointmentDetails.builder()
                .medicalAppointmentDetailsId(DETALLE_ID)
                .citaMedicaId(CITA_ID)
                .build();
        when(appointmentDetailsRepository.findById(DETALLE_ID)).thenReturn(Optional.of(detalle));

        MedicalAppointment citaAtendida = buildProgramada();
        citaAtendida.setEstadoCita(StatusAppointment.ATENDIDA); // ya finalizada
        when(appointmentRepository.findById(CITA_ID)).thenReturn(Optional.of(citaAtendida));

        CloseMedicalAppointmentAttentionRequest request = CloseMedicalAppointmentAttentionRequest.builder()
                .citaMedicaDetalleId(DETALLE_ID)
                .evaluacionFisica("Examen físico")
                .diagnostico("Diagnóstico")
                .build();

        // Act + Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.closeAttention(request, EMAIL_DOCTOR));
        assertTrue(ex.getMessage().contains("no esta en curso"),
                "FA03: debe indicar que la cita no está en curso");
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void closeAttention_setsRequiereSeguimientoFalse_whenNull() {
        // Arrange — requiereSeguimiento null debe interpretarse como false
        stubDoctorResolution();

        MedicalAppointmentDetails detalle = MedicalAppointmentDetails.builder()
                .medicalAppointmentDetailsId(DETALLE_ID)
                .citaMedicaId(CITA_ID)
                .build();
        when(appointmentDetailsRepository.findById(DETALLE_ID)).thenReturn(Optional.of(detalle));

        MedicalAppointment citaEnCurso = buildProgramada();
        citaEnCurso.setEstadoCita(StatusAppointment.EN_CURSO);
        when(appointmentRepository.findById(CITA_ID)).thenReturn(Optional.of(citaEnCurso));

        MedicalAppointmentDetails detalleSaved = MedicalAppointmentDetails.builder()
                .medicalAppointmentDetailsId(DETALLE_ID)
                .citaMedicaId(CITA_ID)
                .evaluacionFisica("OK")
                .diagnostico("Normal")
                .requiereSeguimiento(false)
                .build();
        when(appointmentDetailsRepository.save(any())).thenReturn(detalleSaved);

        MedicalAppointment citaAtendida = buildProgramada();
        citaAtendida.setEstadoCita(StatusAppointment.ATENDIDA);
        when(appointmentRepository.save(any())).thenReturn(citaAtendida);
        when(patientRepository.findById(PACIENTE_ID)).thenReturn(Optional.of(buildPatient()));
        when(specialityRepository.findAllActive()).thenReturn(Collections.emptyList());

        CloseMedicalAppointmentAttentionRequest request = CloseMedicalAppointmentAttentionRequest.builder()
                .citaMedicaDetalleId(DETALLE_ID)
                .evaluacionFisica("OK")
                .diagnostico("Normal")
                .requiereSeguimiento(null) // null → debe quedar false
                .build();

        // Act
        MedicalAppointmentAttentionResponse response = service.closeAttention(request, EMAIL_DOCTOR);

        // Assert
        assertFalse(response.getRequiereSeguimiento(),
                "requiereSeguimiento null debe interpretarse como false");
    }
}

