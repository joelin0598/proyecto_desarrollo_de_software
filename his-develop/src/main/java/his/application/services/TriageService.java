package his.application.services;

import his.application.dto.TriageListItemsResponse;
import his.application.dto.TriagePaidAppointmentLookupResponse;
import his.application.dto.TriageRequest;
import his.application.dto.TriageResponse;
import his.application.usecases.TriageUseCase;
import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.MedicalAppointment;
import his.domain.models.Patient;
import his.domain.models.Priority;
import his.domain.models.StatusAppointment;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.MedicalAppointmentRepository;
import his.domain.ports.PatientRepository;
import his.domain.ports.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * CU 2.0 — Orquestación de ingreso y triaje hospitalario.
 *
 * Flujo:
 *  1. Resolve personalId desde email JWT → User → HospitalStaff
 *  2. FA01: buscar paciente por DPI; si no existe, crear nuevo (sin cuenta web)
 *  3. Construir VitalSigns con los datos del request
 *  4. calculatePriority() en el dominio — lógica de RN04 encapsulada en VitalSigns
 *  5. Persistir y retornar TriageResponse con prioridad real
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TriageService implements TriageUseCase {

    private final PatientRepository patientRepository;
    private final HospitalStaffRepository hospitalStaffRepository;
    private final UserRepository userRepository;
    private final MedicalAppointmentRepository medicalAppointmentRepository;
    private final PaymentValidationService paymentValidationService;

    @Override
    @Transactional
    public TriageResponse execute(TriageRequest request, String emailPersonal) {

        // 1. Resolver personalId del personal autenticado que registra el triaje
        Long personalId = resolvePersonalId(emailPersonal);

        // 2. FA01 — Buscar paciente por DPI; crear si es primera visita
        boolean pacienteNuevo = false;
        Patient patient = patientRepository.findByDpi(request.getDpi()).orElse(null);

        if (patient == null) {
            log.info("FA01: Paciente con DPI={} no encontrado. Creando nuevo expediente.", request.getDpi());
            patient = createWalkInPatient(request);
            pacienteNuevo = true;
        } else {
            log.info("Paciente existente encontrado con DPI={}, pacienteId={}", request.getDpi(), patient.getPacienteId());
            patient = syncPatientDataFromTriage(patient, request);
        }

        PaymentContext paymentContext = resolvePaymentContext(request, patient, personalId);

        Priority prioridad = calculatePriority(request);
        log.info("Prioridad asignada a pacienteId={}: {}", patient.getPacienteId(), prioridad);

        boolean alertaEmergencia = prioridad == Priority.ROJO;
        if (alertaEmergencia) {
            log.warn("FA03: ALERTA ROJA para pacienteId={}. Signos vitales críticos.", patient.getPacienteId());
        }

        MedicalAppointment citaConTriaje = persistTriageInAppointment(
                paymentContext,
                request,
                patient,
                prioridad,
                alertaEmergencia,
                personalId);

        return TriageResponse.builder()
                .pacienteId(patient.getPacienteId())
                .nombreCompleto(patient.getNombreCompleto())
                .dpi(patient.getDpi())
                .pacienteNuevo(pacienteNuevo)
                .signosVitalesId(citaConTriaje.getCitaMedicaId())
                .citaMedicaId(citaConTriaje.getCitaMedicaId())
                .prioridad(prioridad)
                .alertaEmergencia(alertaEmergencia)
                .pagoValidado(paymentContext.pagoValidado())
                .mensajePago(paymentContext.mensajePago())
                .presionSistolica(citaConTriaje.getPresionSistolica())
                .presionDiastolica(citaConTriaje.getPresionDiastolica())
                .frecuenciaCardiaca(citaConTriaje.getFrecuenciaCardiaca())
                .temperatura(citaConTriaje.getTemperatura())
                .saturacionOxigeno(citaConTriaje.getSaturacionOxigeno())
                .pesoKg(citaConTriaje.getPesoKg())
                .tallaCm(citaConTriaje.getTallaCm())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TriageListItemsResponse> listarTriajesRecientes() {
        return medicalAppointmentRepository.findAllOrderByDateTimeDesc().stream()
                // Triajes se consideran citas originadas como walk-in (citaProgramada=false).
                .filter(c -> Boolean.FALSE.equals(c.getCitaProgramada()))
                .map(this::toListItemResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TriagePaidAppointmentLookupResponse> findPaidAppointmentByDpi(String dpi) {
        Patient patient = patientRepository.findByDpi(dpi).orElse(null);
        if (patient == null) {
            return Optional.empty();
        }

        return medicalAppointmentRepository.findNextPaidScheduledByPatient(patient.getPacienteId())
                .map(apt -> toPaidAppointmentLookup(apt, patient));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TriagePaidAppointmentLookupResponse> findPaidAppointmentById(Long citaMedicaId) {
        if (citaMedicaId == null || citaMedicaId <= 0) {
            return Optional.empty();
        }

        return medicalAppointmentRepository.findById(citaMedicaId)
                .filter(apt -> apt.getEstadoCita() == StatusAppointment.PROGRAMADA)
                .filter(apt -> apt.getEstadoAdministrativo() == AdministrativeAppointmentStatus.PAGO_VALIDADO)
                .flatMap(apt -> patientRepository.findById(apt.getPacienteId())
                        .map(patient -> toPaidAppointmentLookup(apt, patient)));
    }

    private TriagePaidAppointmentLookupResponse toPaidAppointmentLookup(MedicalAppointment apt, Patient patient) {
        return TriagePaidAppointmentLookupResponse.builder()
                .citaMedicaId(apt.getCitaMedicaId())
                .pacienteId(patient.getPacienteId())
                .pacienteNombre(patient.getNombreCompleto())
                .pacienteDpi(patient.getDpi())
                .fechaNacimiento(patient.getFechaNacimiento() != null ? patient.getFechaNacimiento().toString() : null)
                .genero(patient.getGenero() != null ? patient.getGenero().name() : null)
                .telefono(patient.getTelefono())
                .emailContacto(patient.getEmailContacto())
                .direccion(patient.getDireccion())
                .contactoEmergencia(patient.getContactoEmergencia())
                .telefonoEmergencia(patient.getTelefonoEmergencia())
                .medicoPersonalId(apt.getPersonalId())
                .especialidadId(apt.getEspecialidadId())
                .fechaCita(apt.getFechaCita() != null ? apt.getFechaCita().toString() : null)
                .horaCita(apt.getHoraCita() != null ? apt.getHoraCita().toString() : null)
                .motivoConsulta(apt.getMotivoConsulta())
                .estadoAdministrativo(apt.getEstadoAdministrativo() != null ? apt.getEstadoAdministrativo().name() : null)
                .build();
    }

    private TriageListItemsResponse toListItemResponse(MedicalAppointment appointment) {
        Patient patient = patientRepository.findById(appointment.getPacienteId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontro paciente para pacienteId=" + appointment.getPacienteId()));

        Priority prioridad = appointment.getPrioridad() != null ? appointment.getPrioridad() : Priority.VERDE;
        boolean alertaEmergencia = prioridad == Priority.ROJO;

        return TriageListItemsResponse.builder()
                .signosVitalesId(appointment.getCitaMedicaId())
                .pacienteId(patient.getPacienteId())
                .fechaHoraRegistro(appointment.getFechaHoraTriaje())
                .nombreCompleto(patient.getNombreCompleto())
                .dpi(patient.getDpi())
                .prioridad(prioridad)
                .alertaEmergencia(alertaEmergencia)
                .presionSistolica(appointment.getPresionSistolica() != null ? appointment.getPresionSistolica() : 0)
                .presionDiastolica(appointment.getPresionDiastolica() != null ? appointment.getPresionDiastolica() : 0)
                .frecuenciaCardiaca(appointment.getFrecuenciaCardiaca() != null ? appointment.getFrecuenciaCardiaca() : 0)
                .temperatura(appointment.getTemperatura() != null ? appointment.getTemperatura() : 0)
                .saturacionOxigeno(appointment.getSaturacionOxigeno() != null ? appointment.getSaturacionOxigeno() : 0)
                .pesoKg(appointment.getPesoKg() != null ? appointment.getPesoKg() : 0)
                .tallaCm(appointment.getTallaCm() != null ? appointment.getTallaCm() : 0)
                .build();
    }
    /**
     * Resuelve el personalId del empleado hospitalario autenticado.
     * Ruta: email → User → HospitalStaff.personalId
     */
    private Long resolvePersonalId(String emailPersonal) {
        var user = userRepository.findByEmail(emailPersonal)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el usuario autenticado con email: " + emailPersonal));

        return hospitalStaffRepository.findByUsuarioId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El usuario autenticado no tiene un perfil de personal hospitalario registrado"))
                .getPersonalId();
    }

    /**
     * FA01 — Crea un nuevo paciente sin cuenta web (walk-in / primera visita).
     */
    private Patient createWalkInPatient(TriageRequest request) {
        Patient newPatient = Patient.builder()
                .usuarioId(null)   // sin cuenta web; otro CU gestiona la vinculación
                .nombreCompleto(request.getNombreCompleto())
                .dpi(request.getDpi())
                .genero(request.getGenero())
                .emailContacto(request.getEmailContacto())
                .fechaNacimiento(request.getFechaNacimiento())
                .direccion(request.getDireccion())
                .telefono(request.getTelefono())
                .contactoEmergencia(request.getContactoEmergencia())
                .telefonoEmergencia(request.getTelefonoEmergencia())
                .aseguradoraId(request.getAseguradoraId())
                .polizaSeguro(request.getPolizaSeguro())
                .build();

        newPatient.validateDpiIfPresent();
        return patientRepository.save(newPatient);
    }

    private Patient syncPatientDataFromTriage(Patient patient, TriageRequest request) {
        patient.setNombreCompleto(request.getNombreCompleto());
        patient.setGenero(request.getGenero());
        patient.setFechaNacimiento(request.getFechaNacimiento());
        patient.setEmailContacto(request.getEmailContacto());
        patient.setDireccion(request.getDireccion());
        patient.setTelefono(request.getTelefono());
        patient.setContactoEmergencia(request.getContactoEmergencia());
        patient.setTelefonoEmergencia(request.getTelefonoEmergencia());
        patient.setAseguradoraId(request.getAseguradoraId());
        patient.setPolizaSeguro(request.getPolizaSeguro());
        return patientRepository.save(patient);
    }

    private PaymentContext resolvePaymentContext(TriageRequest request, Patient patient, Long personalId) {
        if (request.getCitaMedicaId() != null) {
            MedicalAppointment cita = medicalAppointmentRepository.findById(request.getCitaMedicaId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No existe la cita medica indicada para triaje: " + request.getCitaMedicaId()));

            if (!patient.getPacienteId().equals(cita.getPacienteId())) {
                throw new IllegalArgumentException("La cita medica enviada no pertenece al paciente del DPI indicado.");
            }

            return new PaymentContext(
                    cita,
                    true,
                    "Llegada con cita programada: pago exonerado en triaje por cita existente");
        }

        boolean presentoMetodoPago = request.getMetodoPago() != null;
        String mensajePago = presentoMetodoPago
                ? "Walk-in con metodo de pago informado en triaje"
                : "Walk-in sin metodo de pago informado en triaje";

        MedicalAppointment walkIn = MedicalAppointment.builder()
                .pacienteId(patient.getPacienteId())
                .personalId(personalId)
                .especialidadId(null)
                .fechaCita(LocalDate.now())
                .horaCita(LocalTime.now().withSecond(0).withNano(0))
                .motivoConsulta("Ingreso por triaje walk-in")
                .metodoPago(request.getMetodoPago())
                .costoConsulta(175.0)
                .estadoCita(his.domain.models.StatusAppointment.PROGRAMADA)
                .estadoAdministrativo(presentoMetodoPago
                        ? AdministrativeAppointmentStatus.PAGO_VALIDADO
                        : AdministrativeAppointmentStatus.PAGO_PENDIENTE)
                .observacionAdministrativa(mensajePago)
                .solvenciaPago(presentoMetodoPago)
                .citaProgramada(false)
                .codigoCita("TRIAGE-" + patient.getPacienteId() + "-" + System.currentTimeMillis())
                .qrContenido("TRIAGE|PACIENTE=" + patient.getPacienteId() + "|PERSONAL=" + personalId)
                .build();

        return new PaymentContext(
                medicalAppointmentRepository.save(walkIn),
                presentoMetodoPago,
                mensajePago);
    }

    private MedicalAppointment persistTriageInAppointment(
            PaymentContext paymentContext,
            TriageRequest request,
            Patient patient,
            Priority prioridad,
            boolean alertaEmergencia,
            Long personalId
    ) {
        MedicalAppointment appointment = paymentContext.appointment();
        appointment.setPacienteId(patient.getPacienteId());
        appointment.setPresionSistolica(request.getPresionSistolica());
        appointment.setPresionDiastolica(request.getPresionDiastolica());
        appointment.setFrecuenciaCardiaca(request.getFrecuenciaCardiaca());
        appointment.setTemperatura(request.getTemperatura());
        appointment.setSaturacionOxigeno(request.getSaturacionOxigeno());
        appointment.setPesoKg(request.getPesoKg());
        appointment.setTallaCm(request.getTallaCm());
        appointment.setPrioridad(prioridad);
        appointment.setAlertaEmergencia(alertaEmergencia);
        appointment.setFechaHoraTriaje(LocalDateTime.now());
        appointment.setSolvenciaPago(paymentContext.pagoValidado());

        if (appointment.getCitaProgramada() == null) {
            appointment.setCitaProgramada(request.getCitaMedicaId() != null);
        }

        if (appointment.getCitaProgramada() != null && !appointment.getCitaProgramada() && appointment.getPersonalId() == null) {
            // Walk-in queda sin doctor asignado hasta que uno abra la atención.
            appointment.setPersonalId(null);
        }

        return medicalAppointmentRepository.save(appointment);
    }

    private Priority calculatePriority(TriageRequest request) {
        if (request.getSaturacionOxigeno() < 85 || request.getTemperatura() >= 40) {
            return Priority.ROJO;
        }
        if (request.getSaturacionOxigeno() < 92 || request.getTemperatura() >= 38.5) {
            return Priority.NARANJA;
        }
        if (request.getSaturacionOxigeno() < 95 || request.getTemperatura() >= 37.5) {
            return Priority.AMARILLO;
        }
        return Priority.VERDE;
    }

    private record PaymentContext(MedicalAppointment appointment, boolean pagoValidado, String mensajePago) {
    }
}
