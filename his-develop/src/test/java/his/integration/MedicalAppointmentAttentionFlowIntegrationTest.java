package his.integration;

import his.application.dto.CloseMedicalAppointmentAttentionRequest;
import his.application.dto.MedicalAppointmentAttentionResponse;
import his.application.dto.MedicalAppointmentQueueItemResponse;
import his.application.dto.PatientRegisterRequest;
import his.application.dto.PatientRegisterResponse;
import his.application.dto.PatientTriageRequest;
import his.application.dto.RegisterRequest;
import his.application.dto.RegisterRequestAdmin;
import his.application.dto.ScheduleAppointmentRequest;
import his.application.dto.ScheduleAppointmentResponse;
import his.application.services.AppointmentService;
import his.application.services.AuthService;
import his.application.services.MedicalAppointmentAttentionService;
import his.application.services.PatientFlowService;
import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.PatientGender;
import his.domain.models.PaymentOption;
import his.domain.models.Role;
import his.domain.models.StatusAppointment;
import his.infrastructure.persistence.entities.MedicalSpecialityCatalogJpaEntity;
import his.infrastructure.persistence.repositories.HospitalStaffJpaRepository;
import his.infrastructure.persistence.repositories.MedicalAppointmentDetailsJpaRepository;
import his.infrastructure.persistence.repositories.MedicalAppointmentJpaRepository;
import his.infrastructure.persistence.repositories.MedicalSpecialityJpaRepository;
import his.infrastructure.persistence.repositories.PatientJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CU06 — Test de integración del flujo completo de atención médica.
 * Flujo: Registro → Agendar cita (pago validado) → Cola de espera →
 *        Abrir atención (EN_CURSO) → Cerrar atención (ATENDIDA).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MedicalAppointmentAttentionFlowIntegrationTest {

    @Autowired private AuthService authService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private MedicalAppointmentAttentionService attentionService;
    @Autowired private PatientFlowService patientFlowService;
    @Autowired private PatientJpaRepository patientJpaRepository;
    @Autowired private HospitalStaffJpaRepository hospitalStaffJpaRepository;
    @Autowired private MedicalAppointmentJpaRepository appointmentJpaRepository;
    @Autowired private MedicalAppointmentDetailsJpaRepository appointmentDetailsJpaRepository;
    @Autowired private MedicalSpecialityJpaRepository specialityJpaRepository;

    // ──────────────────────────────────────────────────────────────────────────
    // Flujo completo: CU05 → CU06
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void fullAttentionFlow_scheduleThenOpenThenClose_persistsCorrectStates() {
        // ── PASO 1: Crear especialidad ──────────────────────────────────────
        Long especialidadId = specialityJpaRepository.save(
                MedicalSpecialityCatalogJpaEntity.builder()
                        .nombre("Medicina General CU06-IT")
                        .descripcion("Especialidad para prueba de integración CU06")
                        .build()
        ).getEspecialidadId();

        // ── PASO 2: Registrar paciente ──────────────────────────────────────
        authService.register(RegisterRequest.builder()
                .nombreCompleto("Pedro Ramirez")
                .email("pedro.cu06@example.com")
                .password("Segura1!")
                .dpi("8765432101234")
                .genero(PatientGender.MASCULINO)
                .build());

        Long pacienteId = patientJpaRepository.findByDpi("8765432101234")
                .orElseThrow()
                .getPacienteId();

        // ── PASO 3: Registrar doctor ────────────────────────────────────────
        var doctorAuth = authService.registerPersonal(RegisterRequestAdmin.builder()
                .nombreCompleto("Dr. López CU06")
                .email("dr.lopez.cu06@example.com")
                .password("Segura1!")
                .direccion("Zona 10")
                .telefonoCorporativo("50212345679")
                .rol(Role.DOCTOR)
                .especialidadId(especialidadId)
                .numeroColegiado("COL-CU06-001")
                .build());

        Long doctorPersonalId = hospitalStaffJpaRepository
                .findByUsuarioSistemaUsuarioId(doctorAuth.getUser().getId())
                .orElseThrow()
                .getPersonalId();

        String emailDoctor = "dr.lopez.cu06@example.com";

        // ── PASO 4: Registrar recepcionista ─────────────────────────────────
        authService.registerPersonal(RegisterRequestAdmin.builder()
                .nombreCompleto("Recepcion CU06")
                .email("recepcion.cu06@example.com")
                .password("Segura1!")
                .direccion("Zona 1")
                .telefonoCorporativo("50287654322")
                .rol(Role.RECEPCION)
                .numeroColegiado("COL-CU06-002")
                .build());

        // ── PASO 5: Agendar cita con pago aprobado (tarjeta válida) ─────────
        ScheduleAppointmentResponse citaProgramada = appointmentService.scheduleAppointment(
                ScheduleAppointmentRequest.builder()
                        .pacienteId(pacienteId)
                        .medicoPersonalId(doctorPersonalId)
                        .especialidadId(especialidadId)
                        .fechaCita(LocalDate.now().plusDays(2))
                        .horaCita(LocalTime.of(10, 0))
                        .motivoConsulta("Chequeo de rutina CU06")
                        .metodoPago(PaymentOption.TARJETA)
                        .bancoTarjeta("Banco Demo")
                        .numeroTarjeta("4111111111111111")
                        .fechaVencimientoTarjeta("12/30")
                        .nombreTitularTarjeta("PEDRO RAMIREZ")
                        .cvc("123")
                        .build(),
                "recepcion.cu06@example.com"
        );

        assertNotNull(citaProgramada.getCitaMedicaId(), "La cita debe crearse");
        assertEquals(StatusAppointment.PROGRAMADA, citaProgramada.getEstadoCita());
        assertEquals(AdministrativeAppointmentStatus.PAGO_VALIDADO, citaProgramada.getEstadoAdministrativo(),
                "RN09: solo citas con PAGO_VALIDADO entran en la cola");

        Long citaMedicaId = citaProgramada.getCitaMedicaId();

        // ── PASO 6: Verificar cola de espera del doctor (RN09) ───────────────
        List<MedicalAppointmentQueueItemResponse> queue = attentionService.getPatientQueue(emailDoctor);
        assertFalse(queue.isEmpty(), "RN09: La cola del doctor debe contener la cita recién agendada");
        assertEquals(citaMedicaId, queue.get(0).getCitaMedicaId());
        assertEquals("Pedro Ramirez", queue.get(0).getPacienteNombre());

        // ── PASO 7: Abrir atención (PROGRAMADA → EN_CURSO) ──────────────────
        MedicalAppointmentAttentionResponse apertura = attentionService.openAttention(citaMedicaId, emailDoctor);

        assertNotNull(apertura);
        assertEquals(StatusAppointment.EN_CURSO, apertura.getEstado(),
                "CU06: openAttention debe cambiar el estado a EN_CURSO");
        assertNotNull(apertura.getCitaMedicaDetalleId(),
                "CU06: openAttention debe crear el registro de detalle");
        assertEquals("Pedro Ramirez", apertura.getPacienteNombre());

        Long detalleId = apertura.getCitaMedicaDetalleId();

        // Verificar en BD que el estado cambió
        var citaEnBD = appointmentJpaRepository.findById(citaMedicaId).orElseThrow();
        assertEquals("EN_CURSO", citaEnBD.getEstadoCita().name());
        assertTrue(appointmentDetailsJpaRepository.count() >= 1,
                "Debe existir al menos un registro de detalle de cita");

        // ── PASO 8: Verificar que el doctor no puede abrir otra cita (FA01) ──
        // (En un flujo real, havría otra cita; en este test el cola ya está vacía,
        //  pero validamos que no puede abrir la misma cita nuevamente)
        MedicalAppointmentAttentionResponse current = attentionService.getCurrentAttention(emailDoctor);
        assertNotNull(current, "getCurrentAttention debe retornar la cita EN_CURSO");
        assertEquals(citaMedicaId, current.getCitaMedicaId());

        // ── PASO 9: Cerrar atención con registro clínico (EN_CURSO → ATENDIDA) ──
        CloseMedicalAppointmentAttentionRequest closeRequest = CloseMedicalAppointmentAttentionRequest.builder()
                .citaMedicaDetalleId(detalleId)
                .evaluacionFisica("Paciente consciente, orientado, signos vitales estables.")
                .diagnostico("Sin patologías agudas. Chequeo rutinario normal.")
                .recetaMedica("Vitaminas C 500mg - 1 por día por 30 días.")
                .ordenLaboratorio("Hemograma completo")
                .requiereSeguimiento(true)
                .build();

        MedicalAppointmentAttentionResponse cierre = attentionService.closeAttention(closeRequest, emailDoctor);

        assertNotNull(cierre);
        assertEquals(StatusAppointment.ATENDIDA, cierre.getEstado(),
                "RN13: closeAttention debe cambiar el estado a ATENDIDA");
        assertEquals("Sin patologías agudas. Chequeo rutinario normal.", cierre.getDiagnostico());
        assertEquals(Boolean.TRUE, cierre.getRequiereSeguimiento());
        assertNotNull(cierre.getCitaSeguimientoId(),
                "FA03: al marcar seguimiento debe generarse una cita tentativo enlazada");

        var detalleGuardado = appointmentDetailsJpaRepository.findById(detalleId).orElseThrow();
        assertEquals(Boolean.TRUE, detalleGuardado.getRequiereSeguimiento(),
                "El campo requiereSeguimiento debe persistirse en cita_medica_detalle");
        assertNotNull(detalleGuardado.getCitaSeguimiento(),
                "FA03: el detalle debe enlazar la cita de seguimiento tentativo");
        assertEquals("PROGRAMADA", detalleGuardado.getCitaSeguimiento().getEstadoCita().name());
        assertEquals("PAGO_PENDIENTE", detalleGuardado.getCitaSeguimiento().getEstadoAdministrativo().name());

        // Verificar en BD que el estado cambió a ATENDIDA
        var citaFinal = appointmentJpaRepository.findById(citaMedicaId).orElseThrow();
        assertEquals("ATENDIDA", citaFinal.getEstadoCita().name());

        // ── PASO 10: Cola vacía después de atender ───────────────────────────
        List<MedicalAppointmentQueueItemResponse> queueVacia = attentionService.getPatientQueue(emailDoctor);
        assertTrue(queueVacia.isEmpty(),
                "FA02: La cola debe estar vacía después de atender la cita");

        // ── PASO 11: getCurrentAttention debe retornar null ──────────────────
        MedicalAppointmentAttentionResponse currentDespues = attentionService.getCurrentAttention(emailDoctor);
        assertNull(currentDespues,
                "Después de cerrar la atención, getCurrentAttention debe retornar null");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FA01 — Intento de abrir segunda cita cuando ya hay una EN_CURSO
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void openAttention_throwsIllegalState_FA01_whenDoctorAlreadyHasOpenAttention() {
        // Arrange: crear infraestructura mínima
        Long especialidadId = specialityJpaRepository.save(
                MedicalSpecialityCatalogJpaEntity.builder()
                        .nombre("Cardiología CU06-FA01")
                        .descripcion("Test FA01")
                        .build()
        ).getEspecialidadId();

        authService.register(RegisterRequest.builder()
                .nombreCompleto("Lucia Mendez")
                .email("lucia.fa01@example.com")
                .password("Segura1!")
                .dpi("1111111111111")
                .genero(PatientGender.FEMENINO)
                .build());

        Long pacienteId = patientJpaRepository.findByDpi("1111111111111")
                .orElseThrow().getPacienteId();

        var doctorAuth = authService.registerPersonal(RegisterRequestAdmin.builder()
                .nombreCompleto("Dr. Ruiz FA01")
                .email("dr.ruiz.fa01@example.com")
                .password("Segura1!")
                .direccion("Zona 5")
                .telefonoCorporativo("50212345680")
                .rol(Role.DOCTOR)
                .especialidadId(especialidadId)
                .numeroColegiado("COL-FA01-001")
                .build());

        Long doctorPersonalId = hospitalStaffJpaRepository
                .findByUsuarioSistemaUsuarioId(doctorAuth.getUser().getId())
                .orElseThrow().getPersonalId();

        String emailDoctor = "dr.ruiz.fa01@example.com";

        authService.registerPersonal(RegisterRequestAdmin.builder()
                .nombreCompleto("Recepcion FA01")
                .email("recepcion.fa01@example.com")
                .password("Segura1!")
                .direccion("Zona 1")
                .telefonoCorporativo("50287654323")
                .rol(Role.RECEPCION)
                .numeroColegiado("COL-FA01-002")
                .build());

        // Crear primera cita
        ScheduleAppointmentResponse cita1 = appointmentService.scheduleAppointment(
                ScheduleAppointmentRequest.builder()
                        .pacienteId(pacienteId)
                        .medicoPersonalId(doctorPersonalId)
                        .especialidadId(especialidadId)
                        .fechaCita(LocalDate.now().plusDays(3))
                        .horaCita(LocalTime.of(9, 0))
                        .motivoConsulta("Consulta 1")
                        .metodoPago(PaymentOption.TARJETA)
                        .bancoTarjeta("Banco Demo")
                        .numeroTarjeta("4111111111111111")
                        .fechaVencimientoTarjeta("12/30")
                        .nombreTitularTarjeta("LUCIA MENDEZ")
                        .cvc("123")
                        .build(),
                "recepcion.fa01@example.com"
        );

        // Crear segunda cita (día distinto para no colisionar en horario)
        authService.register(RegisterRequest.builder()
                .nombreCompleto("Marco Diaz")
                .email("marco.fa01@example.com")
                .password("Segura1!")
                .dpi("2222222222222")
                .genero(PatientGender.MASCULINO)
                .build());
        Long paciente2Id = patientJpaRepository.findByDpi("2222222222222")
                .orElseThrow().getPacienteId();

        ScheduleAppointmentResponse cita2 = appointmentService.scheduleAppointment(
                ScheduleAppointmentRequest.builder()
                        .pacienteId(paciente2Id)
                        .medicoPersonalId(doctorPersonalId)
                        .especialidadId(especialidadId)
                        .fechaCita(LocalDate.now().plusDays(3))
                        .horaCita(LocalTime.of(10, 0))
                        .motivoConsulta("Consulta 2")
                        .metodoPago(PaymentOption.TARJETA)
                        .bancoTarjeta("Banco Demo")
                        .numeroTarjeta("4111111111111111")
                        .fechaVencimientoTarjeta("12/30")
                        .nombreTitularTarjeta("MARCO DIAZ")
                        .cvc("123")
                        .build(),
                "recepcion.fa01@example.com"
        );

        // Abrir la primera cita
        attentionService.openAttention(cita1.getCitaMedicaId(), emailDoctor);

        // Act + Assert: intentar abrir la segunda → FA01
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> attentionService.openAttention(cita2.getCitaMedicaId(), emailDoctor));
        assertTrue(ex.getMessage().contains("en curso"),
                "FA01: No se puede abrir una segunda cita si ya hay una EN_CURSO");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // RN09 — Cita con PAGO_PENDIENTE NO aparece en la cola
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void getPatientQueue_excludesCitaWithPagoPendiente_RN09() {
        // Arrange
        Long especialidadId = specialityJpaRepository.save(
                MedicalSpecialityCatalogJpaEntity.builder()
                        .nombre("Neurología CU06-RN09")
                        .descripcion("Test RN09")
                        .build()
        ).getEspecialidadId();

        authService.register(RegisterRequest.builder()
                .nombreCompleto("Sofia Rios")
                .email("sofia.rn09@example.com")
                .password("Segura1!")
                .dpi("3333333333333")
                .genero(PatientGender.FEMENINO)
                .build());
        Long pacienteId = patientJpaRepository.findByDpi("3333333333333")
                .orElseThrow().getPacienteId();

        var doctorAuth = authService.registerPersonal(RegisterRequestAdmin.builder()
                .nombreCompleto("Dr. Vega RN09")
                .email("dr.vega.rn09@example.com")
                .password("Segura1!")
                .direccion("Zona 7")
                .telefonoCorporativo("50212345681")
                .rol(Role.DOCTOR)
                .especialidadId(especialidadId)
                .numeroColegiado("COL-RN09-001")
                .build());

        Long doctorPersonalId = hospitalStaffJpaRepository
                .findByUsuarioSistemaUsuarioId(doctorAuth.getUser().getId())
                .orElseThrow().getPersonalId();

        String emailDoctor = "dr.vega.rn09@example.com";

        authService.registerPersonal(RegisterRequestAdmin.builder()
                .nombreCompleto("Recepcion RN09")
                .email("recepcion.rn09@example.com")
                .password("Segura1!")
                .direccion("Zona 1")
                .telefonoCorporativo("50287654324")
                .rol(Role.RECEPCION)
                .numeroColegiado("COL-RN09-002")
                .build());

        // Crear cita con tarjeta inválida → PAGO_PENDIENTE
        appointmentService.scheduleAppointment(
                ScheduleAppointmentRequest.builder()
                        .pacienteId(pacienteId)
                        .medicoPersonalId(doctorPersonalId)
                        .especialidadId(especialidadId)
                        .fechaCita(LocalDate.now().plusDays(4))
                        .horaCita(LocalTime.of(11, 0))
                        .motivoConsulta("Consulta pago pendiente")
                        .metodoPago(PaymentOption.TARJETA)
                        .bancoTarjeta("Banco Demo")
                        .numeroTarjeta("4111111111110000") // tarjeta que genera PAGO_PENDIENTE
                        .fechaVencimientoTarjeta("12/30")
                        .nombreTitularTarjeta("SOFIA RIOS")
                        .cvc("123")
                        .build(),
                "recepcion.rn09@example.com"
        );

        // Act
        List<MedicalAppointmentQueueItemResponse> queue = attentionService.getPatientQueue(emailDoctor);

        // Assert — RN09: citas con PAGO_PENDIENTE NO deben aparecer en la cola
        assertTrue(queue.isEmpty(),
                "RN09: Las citas con PAGO_PENDIENTE no deben aparecer en la cola de atención");
    }

    @Test
    void getPatientQueue_includesScheduledAndWalkInOrderedByPriority() {
        Long especialidadId = specialityJpaRepository.save(
                MedicalSpecialityCatalogJpaEntity.builder()
                        .nombre("Medicina Interna CU06-COLA")
                        .descripcion("Test cola combinada")
                        .build()
        ).getEspecialidadId();

        var doctorAuth = authService.registerPersonal(RegisterRequestAdmin.builder()
                .nombreCompleto("Dr. Prioridad CU06")
                .email("dr.prioridad.cu06@example.com")
                .password("Segura1!")
                .direccion("Zona 9")
                .telefonoCorporativo("50212345682")
                .rol(Role.DOCTOR)
                .especialidadId(especialidadId)
                .numeroColegiado("COL-CU06-003")
                .build());

        Long doctorPersonalId = hospitalStaffJpaRepository
                .findByUsuarioSistemaUsuarioId(doctorAuth.getUser().getId())
                .orElseThrow()
                .getPersonalId();

        authService.registerPersonal(RegisterRequestAdmin.builder()
                .nombreCompleto("Recepcion Cola CU06")
                .email("recepcion.cola.cu06@example.com")
                .password("Segura1!")
                .direccion("Zona 2")
                .telefonoCorporativo("50287654325")
                .rol(Role.RECEPCION)
                .numeroColegiado("COL-CU06-004")
                .build());

        authService.register(RegisterRequest.builder()
                .nombreCompleto("Paciente Programado CU06")
                .email("paciente.programado.cu06@example.com")
                .password("Segura1!")
                .dpi("4444444444444")
                .genero(PatientGender.MASCULINO)
                .build());

        Long pacienteProgramadoId = patientJpaRepository.findByDpi("4444444444444")
                .orElseThrow()
                .getPacienteId();

        ScheduleAppointmentResponse citaProgramada = appointmentService.scheduleAppointment(
                ScheduleAppointmentRequest.builder()
                        .pacienteId(pacienteProgramadoId)
                        .medicoPersonalId(doctorPersonalId)
                        .especialidadId(especialidadId)
                        .fechaCita(LocalDate.now().plusDays(5))
                        .horaCita(LocalTime.of(8, 0))
                        .motivoConsulta("Consulta programada CU06")
                        .metodoPago(PaymentOption.TARJETA)
                        .bancoTarjeta("Banco Demo")
                        .numeroTarjeta("4111111111111111")
                        .fechaVencimientoTarjeta("12/30")
                        .nombreTitularTarjeta("PACIENTE PROGRAMADO CU06")
                        .cvc("123")
                        .build(),
                "recepcion.cola.cu06@example.com"
        );

        PatientRegisterRequest walkInRequest = new PatientRegisterRequest();
        walkInRequest.setNombreCompleto("Paciente Walk-In CU06");
        walkInRequest.setDpi("5555555555555");
        walkInRequest.setGenero(PatientGender.FEMENINO);
        walkInRequest.setTelefono("55112233");
        walkInRequest.setEmailContacto("walkin.cu06@example.com");
        walkInRequest.setDireccion("Zona 3");
        walkInRequest.setContactoEmergencia("Contacto Walk-In");
        walkInRequest.setTelefonoEmergencia("55443322");
        walkInRequest.setMetodoPago(PaymentOption.TARJETA);
        walkInRequest.setBancoTarjeta("Banco Demo");
        walkInRequest.setNumeroTarjeta("4111111111111111");
        walkInRequest.setFechaVencimientoTarjeta("12/30");
        walkInRequest.setNombreTitularTarjeta("PACIENTE WALK-IN CU06");
        walkInRequest.setCvc("123");

        PatientRegisterResponse walkIn = patientFlowService.register(walkInRequest, "recepcion.cola.cu06@example.com");

        PatientTriageRequest walkInTriageRequest = new PatientTriageRequest();
        walkInTriageRequest.setCitaMedicaId(walkIn.getCitaMedicaId());
        walkInTriageRequest.setPresionSistolica(70);
        walkInTriageRequest.setPresionDiastolica(40);
        walkInTriageRequest.setFrecuenciaCardiaca(170);
        walkInTriageRequest.setTemperatura(40.0);
        walkInTriageRequest.setSaturacionOxigeno(80);
        walkInTriageRequest.setPesoKg(60);
        walkInTriageRequest.setTallaCm(165);

        patientFlowService.triage(walkInTriageRequest, "recepcion.cola.cu06@example.com");

        List<MedicalAppointmentQueueItemResponse> queue = attentionService.getPatientQueue("dr.prioridad.cu06@example.com");

        assertFalse(queue.isEmpty(), "La cola del médico debe mostrar pacientes pendientes");
        assertEquals(walkIn.getCitaMedicaId(), queue.get(0).getCitaMedicaId(),
                "El walk-in triado con mayor prioridad debe aparecer primero");
        assertEquals("ROJO", queue.get(0).getPrioridad());
        assertTrue(queue.stream().anyMatch(item -> citaProgramada.getCitaMedicaId().equals(item.getCitaMedicaId())),
                "La cola también debe incluir la cita programada del médico");
    }

    @Test
    void scheduledAppointmentTriagedById_entersDoctorQueue_evenIfArrivalTimeDiffers() {
        Long especialidadId = specialityJpaRepository.save(
                MedicalSpecialityCatalogJpaEntity.builder()
                        .nombre("Pediatría CU06-HORA")
                        .descripcion("Test llegada fuera de hora")
                        .build()
        ).getEspecialidadId();

        authService.register(RegisterRequest.builder()
                .nombreCompleto("Paciente Fuera de Hora")
                .email("paciente.fuera.hora@example.com")
                .password("Segura1!")
                .dpi("6666666666666")
                .genero(PatientGender.MASCULINO)
                .build());

        Long pacienteId = patientJpaRepository.findByDpi("6666666666666")
                .orElseThrow()
                .getPacienteId();

        var doctorAuth = authService.registerPersonal(RegisterRequestAdmin.builder()
                .nombreCompleto("Dr. Cola Hora")
                .email("dr.cola.hora@example.com")
                .password("Segura1!")
                .direccion("Zona 4")
                .telefonoCorporativo("50212345683")
                .rol(Role.DOCTOR)
                .especialidadId(especialidadId)
                .numeroColegiado("COL-CU06-005")
                .build());

        Long doctorPersonalId = hospitalStaffJpaRepository
                .findByUsuarioSistemaUsuarioId(doctorAuth.getUser().getId())
                .orElseThrow()
                .getPersonalId();

        authService.registerPersonal(RegisterRequestAdmin.builder()
                .nombreCompleto("Recepcion Hora")
                .email("recepcion.hora@example.com")
                .password("Segura1!")
                .direccion("Zona 6")
                .telefonoCorporativo("50287654326")
                .rol(Role.RECEPCION)
                .numeroColegiado("COL-CU06-006")
                .build());

        ScheduleAppointmentResponse citaProgramada = appointmentService.scheduleAppointment(
                ScheduleAppointmentRequest.builder()
                        .pacienteId(pacienteId)
                        .medicoPersonalId(doctorPersonalId)
                        .especialidadId(especialidadId)
                        .fechaCita(LocalDate.now().plusDays(10))
                        .horaCita(LocalTime.of(15, 30))
                        .motivoConsulta("Paciente llega antes del horario")
                        .metodoPago(PaymentOption.TARJETA)
                        .bancoTarjeta("Banco Demo")
                        .numeroTarjeta("4111111111111111")
                        .fechaVencimientoTarjeta("12/30")
                        .nombreTitularTarjeta("PACIENTE FUERA DE HORA")
                        .cvc("123")
                        .build(),
                "recepcion.hora@example.com"
        );

        PatientTriageRequest scheduledTriageRequest = new PatientTriageRequest();
        scheduledTriageRequest.setCitaMedicaId(citaProgramada.getCitaMedicaId());
        scheduledTriageRequest.setPresionSistolica(118);
        scheduledTriageRequest.setPresionDiastolica(78);
        scheduledTriageRequest.setFrecuenciaCardiaca(75);
        scheduledTriageRequest.setTemperatura(37.0);
        scheduledTriageRequest.setSaturacionOxigeno(97);
        scheduledTriageRequest.setPesoKg(68);
        scheduledTriageRequest.setTallaCm(172);

        patientFlowService.triage(scheduledTriageRequest, "recepcion.hora@example.com");

        List<MedicalAppointmentQueueItemResponse> queue = attentionService.getPatientQueue("dr.cola.hora@example.com");

        assertTrue(queue.stream().anyMatch(item -> citaProgramada.getCitaMedicaId().equals(item.getCitaMedicaId())),
                "Una cita programada triada por ID debe entrar a la cola del médico aunque el paciente llegue antes o después de la hora exacta");
    }
}

