package his.application.services;

import his.application.dto.PatientLookupResponse;
import his.application.dto.PatientRegisterRequest;
import his.application.dto.PatientRegisterResponse;
import his.application.dto.PatientAvailabilityResponse;
import his.application.dto.PatientTriageRequest;
import his.application.dto.TriageResponse;
import his.application.dto.MedicalRecordResponse;
import his.application.dto.UpdatePatientProfileRequest;
import his.application.usecases.LaboratoryUseCase;
import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.HospitalStaff;
import his.domain.models.MedicalAppointmentDetails;
import his.domain.models.MedicalPrescription;
import his.domain.models.MedicalPrescriptionDetails;
import his.domain.models.MedicalAppointment;
import his.domain.models.Patient;
import his.domain.models.Priority;
import his.domain.models.Role;
import his.domain.models.StatusAppointment;
import his.domain.ports.MedicalAppointmentDetailsRepository;
import his.domain.ports.MedicalPrescriptionDetailsRepository;
import his.domain.ports.MedicalPrescriptionRepository;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientFlowService {

    private final PatientRepository patientRepository;
    private final MedicalAppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final HospitalStaffRepository staffRepository;
    private final PaymentValidationService paymentValidationService;
    private final MedicalAppointmentDetailsRepository medicalAppointmentDetailsRepository;
    private final MedicalPrescriptionRepository medicalPrescriptionRepository;
    private final MedicalPrescriptionDetailsRepository medicalPrescriptionDetailsRepository;
    private final LaboratoryUseCase laboratoryUseCase;

    @Transactional(readOnly = true)
    public PatientAvailabilityResponse checkAvailability(String dpi, String emailContacto) {
        String normalizedDpi = dpi == null ? "" : dpi.trim();
        String normalizedEmail = emailContacto == null ? "" : emailContacto.trim();

        boolean dpiInUse = !normalizedDpi.isEmpty() && patientRepository.existsByDpi(normalizedDpi);
        boolean emailInUse = !normalizedEmail.isEmpty() && patientRepository.existsByEmailContacto(normalizedEmail);

        String message;
        if (dpiInUse && emailInUse) {
            message = "El DPI y el correo ya fueron registrados previamente.";
        } else if (dpiInUse) {
            message = "El DPI ya fue registrado previamente.";
        } else if (emailInUse) {
            message = "El correo ya fue registrado previamente.";
        } else {
            message = "Disponibilidad valida para continuar con el registro.";
        }

        return PatientAvailabilityResponse.builder()
                .dpiInUse(dpiInUse)
                .emailInUse(emailInUse)
                .available(!dpiInUse && !emailInUse)
                .message(message)
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<PatientLookupResponse> findPatientByDpi(String dpi) {
        String normalizedDpi = dpi == null ? "" : dpi.trim();
        if (normalizedDpi.isEmpty()) {
            return Optional.empty();
        }

        return patientRepository.findByDpi(normalizedDpi)
                .map(patient -> PatientLookupResponse.builder()
                        .pacienteId(patient.getPacienteId())
                        .nombreCompleto(patient.getNombreCompleto())
                        .dpi(patient.getDpi())
                        .fechaNacimiento(patient.getFechaNacimiento())
                        .genero(patient.getGenero() != null ? patient.getGenero().name() : null)
                        .telefono(patient.getTelefono())
                        .emailContacto(patient.getEmailContacto())
                        .direccion(patient.getDireccion())
                        .contactoEmergencia(patient.getContactoEmergencia())
                        .telefonoEmergencia(patient.getTelefonoEmergencia())
                        .build());
    }

    @Transactional
    public PatientRegisterResponse register(PatientRegisterRequest req, String emailPersonal) {
        HospitalStaff staff = resolveStaff(emailPersonal);

        String normalizedDpi = req.getDpi() == null ? "" : req.getDpi().trim();
        String normalizedEmail = req.getEmailContacto() == null ? null : req.getEmailContacto().trim();

        Patient existingByDpi = patientRepository.findByDpi(normalizedDpi).orElse(null);
        if (existingByDpi == null) {
            PatientAvailabilityResponse availability = checkAvailability(normalizedDpi, normalizedEmail);
            if (!availability.isAvailable()) {
                throw new IllegalArgumentException(availability.getMessage());
            }
        }

        if (req.getMetodoPago() == null) {
            throw new IllegalArgumentException("FA05: Debe registrar un metodo de pago para continuar.");
        }

        var paymentResult = paymentValidationService.validateForTriage(toPaymentRequest(req));
        if (!paymentResult.approved()) {
            throw new IllegalArgumentException("FA04: " + paymentResult.message());
        }

        boolean nuevo = existingByDpi == null;
        Patient patient = existingByDpi != null ? existingByDpi : Patient.builder().build();

        // FA06: cuando el paciente ya existe, preserva y reutiliza su expediente para fases 1 y 2.
        patient.setNombreCompleto(resolveString(req.getNombreCompleto(), patient.getNombreCompleto()));
        patient.setDpi(resolveString(normalizedDpi, patient.getDpi()));
        patient.setFechaNacimiento(req.getFechaNacimiento() != null ? req.getFechaNacimiento() : patient.getFechaNacimiento());
        patient.setGenero(req.getGenero() != null ? req.getGenero() : patient.getGenero());
        patient.setTelefono(resolveString(req.getTelefono(), patient.getTelefono()));
        patient.setEmailContacto(resolveString(normalizedEmail, patient.getEmailContacto()));
        patient.setDireccion(resolveString(req.getDireccion(), patient.getDireccion()));
        patient.setAseguradoraId(req.getAseguradoraId() != null ? req.getAseguradoraId() : patient.getAseguradoraId());
        patient.setPolizaSeguro(resolveString(req.getPolizaSeguro(), patient.getPolizaSeguro()));
        patient.setContactoEmergencia(resolveString(req.getContactoEmergencia(), patient.getContactoEmergencia()));
        patient.setTelefonoEmergencia(resolveString(req.getTelefonoEmergencia(), patient.getTelefonoEmergencia()));
        patient = patientRepository.save(patient);

        if (!nuevo) {
            log.info("FA06: Reutilizando expediente existente para DPI={} pacienteId={}", patient.getDpi(), patient.getPacienteId());
        }


        MedicalAppointment cita = appointmentRepository.save(MedicalAppointment.builder()
                .pacienteId(patient.getPacienteId())
                .personalId(staff.getPersonalId())
                .especialidadId(null)
                .fechaCita(LocalDate.now())
                .horaCita(LocalTime.now().withSecond(0).withNano(0))
                .motivoConsulta("Registro inicial y triaje")
                .metodoPago(req.getMetodoPago())
                .costoConsulta(175.0)
                .estadoCita(StatusAppointment.PROGRAMADA)
                .estadoAdministrativo(AdministrativeAppointmentStatus.PAGO_VALIDADO)
                .observacionAdministrativa(paymentResult.message())
                .solvenciaPago(true)
                .citaProgramada(false)
                .codigoCita("REG-" + patient.getPacienteId() + "-" + System.currentTimeMillis())
                .qrContenido("REG|PACIENTE=" + patient.getPacienteId() + "|PERSONAL=" + staff.getPersonalId())
                .build());

        return PatientRegisterResponse.builder()
                .pacienteId(patient.getPacienteId())
                .citaMedicaId(cita.getCitaMedicaId())
                .pacienteNuevo(nuevo)
                .pagoValidado(true)
                .mensaje("Registro completado y pago validado")
                .build();
    }

    private String resolveString(String requestedValue, String fallbackValue) {
        if (requestedValue == null || requestedValue.isBlank()) {
            return fallbackValue;
        }
        return requestedValue;
    }

    @Transactional
    public TriageResponse triage(PatientTriageRequest req, String emailPersonal) {
        resolveStaff(emailPersonal);

        MedicalAppointment cita = resolveAppointment(req);
        if (cita.getEstadoAdministrativo() != AdministrativeAppointmentStatus.PAGO_VALIDADO || !Boolean.TRUE.equals(cita.getSolvenciaPago())) {
            throw new IllegalArgumentException("RN03: No se puede realizar triaje sin solvencia administrativa.");
        }

        Priority prioridad = calculatePriority(req);
        boolean alerta = prioridad == Priority.ROJO;

        cita.setPresionSistolica(req.getPresionSistolica());
        cita.setPresionDiastolica(req.getPresionDiastolica());
        cita.setFrecuenciaCardiaca(req.getFrecuenciaCardiaca());
        cita.setTemperatura(req.getTemperatura());
        cita.setSaturacionOxigeno(req.getSaturacionOxigeno());
        cita.setPesoKg(req.getPesoKg());
        cita.setTallaCm(req.getTallaCm());
        cita.setPrioridad(prioridad);
        cita.setAlertaEmergencia(alerta);
        cita.setFechaHoraTriaje(LocalDateTime.now());

        MedicalAppointment updated = appointmentRepository.save(cita);
        Patient patient = patientRepository.findById(updated.getPacienteId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        if (alerta) {
            log.warn("FA03: CODIGO ROJO pacienteId={} citaId={} -> priorizar atencion inmediata", patient.getPacienteId(), updated.getCitaMedicaId());
        }

        return TriageResponse.builder()
                .pacienteId(patient.getPacienteId())
                .nombreCompleto(patient.getNombreCompleto())
                .dpi(patient.getDpi())
                .pacienteNuevo(false)
                .signosVitalesId(updated.getCitaMedicaId())
                .citaMedicaId(updated.getCitaMedicaId())
                .prioridad(prioridad)
                .alertaEmergencia(alerta)
                .pagoValidado(true)
                .mensajePago("Triaje registrado")
                .presionSistolica(updated.getPresionSistolica())
                .presionDiastolica(updated.getPresionDiastolica())
                .frecuenciaCardiaca(updated.getFrecuenciaCardiaca())
                .temperatura(updated.getTemperatura())
                .saturacionOxigeno(updated.getSaturacionOxigeno())
                .pesoKg(updated.getPesoKg())
                .tallaCm(updated.getTallaCm())
                .build();
    }

    private MedicalAppointment resolveAppointment(PatientTriageRequest req) {
        if (req.getCitaMedicaId() != null) {
            return appointmentRepository.findById(req.getCitaMedicaId())
                    .orElseThrow(() -> new IllegalArgumentException("No existe cita medica: " + req.getCitaMedicaId()));
        }
        if (req.getDpi() == null || req.getDpi().isBlank()) {
            throw new IllegalArgumentException("Debe enviar citaMedicaId o dpi para triaje.");
        }

        Patient patient = patientRepository.findByDpi(req.getDpi())
                .orElseThrow(() -> new IllegalArgumentException("No existe paciente con DPI=" + req.getDpi()));

        return appointmentRepository.findNextPaidScheduledByPatient(patient.getPacienteId())
                .orElseThrow(() -> new IllegalArgumentException("FA01: No hay cita programada y pagada pendiente para el paciente."));
    }

    private HospitalStaff resolveStaff(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
        var staff = staffRepository.findByUsuarioId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("No tiene perfil de personal: " + email));
        if (staff.getRol() != Role.RECEPCION && staff.getRol() != Role.ENFERMERA && staff.getRol() != Role.ADMINISTRATIVO && staff.getRol() != Role.ADMIN) {
            throw new IllegalArgumentException("Rol no autorizado para CU02: " + staff.getRol());
        }
        return staff;
    }

    private his.application.dto.TriageRequest toPaymentRequest(PatientRegisterRequest req) {
        return his.application.dto.TriageRequest.builder()
                .metodoPago(req.getMetodoPago())
                .bancoTarjeta(req.getBancoTarjeta())
                .numeroTarjeta(req.getNumeroTarjeta())
                .fechaVencimientoTarjeta(req.getFechaVencimientoTarjeta())
                .nombreTitularTarjeta(req.getNombreTitularTarjeta())
                .cvc(req.getCvc())
                .aseguradoraId(req.getAseguradoraId())
                .polizaSeguro(req.getPolizaSeguro())
                .build();
    }

    private Priority calculatePriority(PatientTriageRequest req) {
        if (req.getSaturacionOxigeno() < 85
                || req.getTemperatura() >= 40
                || req.getFrecuenciaCardiaca() > 160
                || req.getPresionSistolica() < 80) {
            return Priority.ROJO;
        }
        if (req.getSaturacionOxigeno() < 92
                || req.getTemperatura() >= 38.5
                || req.getFrecuenciaCardiaca() > 130
                || req.getPresionSistolica() < 90) {
            return Priority.NARANJA;
        }
        if (req.getSaturacionOxigeno() < 95
                || req.getTemperatura() >= 37.5
                || req.getFrecuenciaCardiaca() > 110
                || req.getPresionSistolica() < 100) {
            return Priority.AMARILLO;
        }
        return Priority.VERDE;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentPatientData(String emailContacto) {
        userRepository.findByEmail(emailContacto)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + emailContacto));

        Patient patient = patientRepository.findByEmailContacto(emailContacto)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado para el usuario"));

        Map<String, Object> data = new HashMap<>();
        data.put("id", patient.getPacienteId());
        data.put("nombre", patient.getNombreCompleto());
        data.put("dpi", patient.getDpi());
        data.put("email", patient.getEmailContacto());
        data.put("fechaNacimiento", patient.getFechaNacimiento());
        data.put("genero", patient.getGenero() != null ? patient.getGenero().name() : null);
        data.put("telefono", patient.getTelefono());
        data.put("direccion", patient.getDireccion());
        data.put("contactoEmergencia", patient.getContactoEmergencia());
        data.put("telefonoEmergencia", patient.getTelefonoEmergencia());
        return data;
    }

    @Transactional
    public Map<String, Object> updateCurrentPatientData(String emailContacto, Map<String, String> updates) {
        userRepository.findByEmail(emailContacto)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + emailContacto));

        Patient patient = patientRepository.findByEmailContacto(emailContacto)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado para el usuario"));

        // Permite actualizar solo teléfono y dirección
        if (updates.containsKey("telefono") && updates.get("telefono") != null) {
            patient.setTelefono(updates.get("telefono"));
        }
        if (updates.containsKey("direccion") && updates.get("direccion") != null) {
            patient.setDireccion(updates.get("direccion"));
        }

        Patient updated = patientRepository.save(patient);
        log.info("Datos del paciente actualizados: email={}, pacienteId={}", emailContacto, patient.getPacienteId());

        Map<String, Object> data = new HashMap<>();
        data.put("id", updated.getPacienteId());
        data.put("nombre", updated.getNombreCompleto());
        data.put("dpi", updated.getDpi());
        data.put("email", updated.getEmailContacto());
        data.put("telefono", updated.getTelefono());
        data.put("direccion", updated.getDireccion());
        data.put("contactoEmergencia", updated.getContactoEmergencia());
        data.put("telefonoEmergencia", updated.getTelefonoEmergencia());
        return data;
    }

    @Transactional
    public Map<String, Object> updatePatientProfile(String emailContacto, UpdatePatientProfileRequest request) {
        userRepository.findByEmail(emailContacto)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + emailContacto));

        Patient patient = patientRepository.findByEmailContacto(emailContacto)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado para el usuario"));

        if (request.getTelefono() != null) {
            patient.setTelefono(request.getTelefono().trim());
        }
        if (request.getDireccion() != null) {
            patient.setDireccion(request.getDireccion().trim());
        }
        if (request.getGenero() != null) {
            patient.setGenero(request.getGenero());
        }

        Patient updated = patientRepository.save(patient);

        Map<String, Object> data = new HashMap<>();
        data.put("id", updated.getPacienteId());
        data.put("nombre", updated.getNombreCompleto());
        data.put("dpi", updated.getDpi());
        data.put("email", updated.getEmailContacto());
        data.put("fechaNacimiento", updated.getFechaNacimiento());
        data.put("genero", updated.getGenero() != null ? updated.getGenero().name() : null);
        data.put("telefono", updated.getTelefono());
        data.put("direccion", updated.getDireccion());
        return data;
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse getMedicalRecord(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado: " + patientId));

        List<MedicalAppointment> appointments = appointmentRepository.findByPacienteIdOrderByDateTimeDesc(patientId);

        List<MedicalRecordResponse.AppointmentHistoryItem> appointmentItems = appointments.stream()
                .map(appointment -> MedicalRecordResponse.AppointmentHistoryItem.builder()
                        .citaMedicaId(appointment.getCitaMedicaId())
                        .fechaCita(appointment.getFechaCita())
                        .horaCita(appointment.getHoraCita())
                        .motivoConsulta(appointment.getMotivoConsulta())
                        .estadoCita(appointment.getEstadoCita())
                        .estadoAdministrativo(appointment.getEstadoAdministrativo())
                        .solvenciaPago(appointment.getSolvenciaPago())
                        .build())
                .toList();

        List<MedicalRecordResponse.TriageHistoryItem> triageItems = appointments.stream()
                .filter(appointment -> appointment.getFechaHoraTriaje() != null)
                .map(appointment -> MedicalRecordResponse.TriageHistoryItem.builder()
                        .citaMedicaId(appointment.getCitaMedicaId())
                        .prioridad(appointment.getPrioridad())
                        .alertaEmergencia(appointment.getAlertaEmergencia())
                        .presionSistolica(appointment.getPresionSistolica())
                        .presionDiastolica(appointment.getPresionDiastolica())
                        .frecuenciaCardiaca(appointment.getFrecuenciaCardiaca())
                        .temperatura(appointment.getTemperatura())
                        .saturacionOxigeno(appointment.getSaturacionOxigeno())
                        .pesoKg(appointment.getPesoKg())
                        .tallaCm(appointment.getTallaCm())
                        .fechaHoraTriaje(appointment.getFechaHoraTriaje())
                        .build())
                .toList();

        List<MedicalPrescription> prescriptions = medicalPrescriptionRepository.findByPacienteDpi(patient.getDpi());
        List<MedicalRecordResponse.PrescriptionHistoryItem> prescriptionItems = prescriptions.stream()
                .map(prescription -> {
                    List<MedicalPrescriptionDetails> details = medicalPrescriptionDetailsRepository.findByRecetaId(prescription.getRecetaMedicaId());
                    List<MedicalRecordResponse.PrescriptionDetailItem> detailItems = details.stream()
                            .map(detail -> MedicalRecordResponse.PrescriptionDetailItem.builder()
                                    .recetaMedicaDetalleId(detail.getRecetaMedicaDetalleId())
                                    .medicamentoId(detail.getMedicamentoId())
                                    .medicamentoNombre(detail.getMedicamentoNombre())
                                    .cantidad(detail.getCantidad())
                                    .dosis(detail.getDosis())
                                    .viaAdministracion(detail.getViaAdministracion())
                                    .frecuenciaHoras(detail.getFrecuenciaHoras())
                                    .duracionDias(detail.getDuracionDias())
                                    .despachado(detail.isDespachado())
                                    .pagoValidado(detail.isPagoValidado())
                                    .build())
                            .toList();

                    return MedicalRecordResponse.PrescriptionHistoryItem.builder()
                            .recetaMedicaId(prescription.getRecetaMedicaId())
                            .citaMedicaDetalleId(prescription.getCitaMedicaDetalleId())
                            .fechaEmision(prescription.getFechaEmision())
                            .instruccionesGenerales(prescription.getInstruccionesGenerales())
                            .createdAt(prescription.getCreatedAt())
                            .items(detailItems)
                            .build();
                })
                .toList();

        List<MedicalRecordResponse.LaboratoryHistoryItem> laboratoryItems = laboratoryUseCase.getResultsByPatient(patientId).stream()
                .map(order -> MedicalRecordResponse.LaboratoryHistoryItem.builder()
                        .ordenLaboratorioId(order.getOrdenLaboratorioId())
                        .citaMedicaDetalleId(order.getCitaMedicaDetalleId())
                        .nombreExamen(order.getNombreExamen())
                        .estado(order.getEstado())
                        .pagoValidado(order.isPagoValidado())
                        .etiquetaId(order.getEtiquetaId())
                        .alertaCritica(order.isAlertaCritica())
                        .observacionesTecnico(order.getObservacionesTecnico())
                        .createdAt(order.getCreatedAt())
                        .resultado(order.getResultado())
                        .build())
                .toList();

        return MedicalRecordResponse.builder()
                .patientId(patient.getPacienteId())
                .nombreCompleto(patient.getNombreCompleto())
                .dpi(patient.getDpi())
                .genero(patient.getGenero())
                .fechaNacimiento(patient.getFechaNacimiento())
                .telefono(patient.getTelefono())
                .direccion(patient.getDireccion())
                .appointments(appointmentItems)
                .triages(triageItems)
                .prescriptions(prescriptionItems)
                .laboratoryResults(laboratoryItems)
                .build();
    }
}

