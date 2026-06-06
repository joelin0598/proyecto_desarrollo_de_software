package his.application.services;

import his.application.dto.CreatePrescriptionRequest;
import his.application.dto.DispenseMedicineRequest;
import his.application.dto.MedicationReminderResponse;
import his.application.dto.MedicineResponse;
import his.application.dto.PharmacyPaymentRequest;
import his.application.dto.PharmacyPrescriptionLookupResponse;
import his.application.dto.PrescriptionResponse;
import his.application.usecases.PharmacyUseCase;
import his.domain.models.HospitalStaff;
import his.domain.models.MedicalPrescription;
import his.domain.models.MedicalPrescriptionDetails;
import his.domain.models.MedicationReminder;
import his.domain.models.Medicine;
import his.domain.models.MedicalAppointmentDetails;
import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.models.Patient;
import his.domain.models.PaymentOption;
import his.domain.models.Role;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.MedicalAppointmentDetailsRepository;
import his.domain.ports.MedicalAppointmentRepository;
import his.domain.ports.MedicalPrescriptionDetailsRepository;
import his.domain.ports.MedicalPrescriptionRepository;
import his.domain.ports.MedicationReminderRepository;
import his.domain.ports.MedicineRepository;
import his.domain.ports.PatientRepository;
import his.domain.ports.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * CU08 — Servicios de farmacia: creación de recetas, despacho y recordatorios.
 * Aplica RN03 (solvencia), RN09 (stock disponible), RN10 (ciclo clínico).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyService implements PharmacyUseCase {

    private static final int PRESCRIPTION_VALID_DAYS = 30;

    private final MedicalPrescriptionRepository prescriptionRepository;
    private final MedicalPrescriptionDetailsRepository prescriptionDetailsRepository;
    private final MedicineRepository medicineRepository;
    private final MedicationReminderRepository reminderRepository;
    private final MedicalAppointmentDetailsRepository appointmentDetailsRepository;
    private final MedicalAppointmentRepository appointmentRepository;
    private final HospitalStaffRepository staffRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Crear receta médica (solo médico durante o tras consulta)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public PrescriptionResponse createPrescription(CreatePrescriptionRequest req, String emailDoctor) {
        HospitalStaff doctor = resolveRole(emailDoctor, Role.DOCTOR);

        // RN10 — el detalle de cita debe existir (flujo: Registro→Triaje→Consulta→Farmacia)
        MedicalAppointmentDetails detalle = appointmentDetailsRepository.findById(req.getCitaMedicaDetalleId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Detalle de cita no encontrado: " + req.getCitaMedicaDetalleId()));

        MedicalPrescription prescription = prescriptionRepository.save(MedicalPrescription.builder()
                .citaMedicaDetalleId(req.getCitaMedicaDetalleId())
                .instruccionesGenerales(req.getInstruccionesGenerales())
                .fechaEmision(LocalDate.now())
                .build());

        List<MedicalPrescriptionDetails> items = req.getItems().stream().map(item -> {
            Medicine med = medicineRepository.findById(item.getMedicamentoId())
                    .orElseThrow(() -> new IllegalArgumentException("Medicamento no encontrado: " + item.getMedicamentoId()));
            return prescriptionDetailsRepository.save(MedicalPrescriptionDetails.builder()
                    .recetaMedicaId(prescription.getRecetaMedicaId())
                    .medicamentoId(med.getMedicamentoId())
                    .cantidad(item.getCantidad())
                    .dosis(item.getDosis())
                    .viaAdministracion(item.getViaAdministracion())
                    .frecuenciaHoras(item.getFrecuenciaHoras())
                    .duracionDias(item.getDuracionDias())
                    .despachado(false)
                    .pagoValidado(false)
                    .build());
        }).toList();

        log.info("CU08: Receta creada recetaId={} detalleId={} medicamentos={}",
                prescription.getRecetaMedicaId(), req.getCitaMedicaDetalleId(), items.size());

        return toResponse(prescription, items);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Obtener receta de una cita detalle
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescription(Long citaMedicaDetalleId) {
        MedicalPrescription prescription = prescriptionRepository.findByCitaMedicaDetalleId(citaMedicaDetalleId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Receta no encontrada para detalleId=" + citaMedicaDetalleId));
        validatePrescriptionIsActive(prescription);
        List<MedicalPrescriptionDetails> items =
                prescriptionDetailsRepository.findByRecetaId(prescription.getRecetaMedicaId());
        return toResponse(prescription, items);
    }

    @Override
    @Transactional(readOnly = true)
    public PharmacyPrescriptionLookupResponse findPrescriptionsByDpi(String dpi) {
        if (dpi == null || !dpi.matches("^[0-9]{13}$")) {
            throw new IllegalArgumentException("El DPI debe tener exactamente 13 digitos.");
        }

        Patient patient = patientRepository.findByDpi(dpi)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado para el DPI indicado."));

        List<PrescriptionResponse> prescriptions = prescriptionRepository.findByPacienteDpi(dpi).stream()
                .filter(this::isPrescriptionActive)
                .map(prescription -> toResponse(
                        prescription,
                        prescriptionDetailsRepository.findByRecetaId(prescription.getRecetaMedicaId())))
                .toList();

        return PharmacyPrescriptionLookupResponse.builder()
                .pacienteId(patient.getPacienteId())
                .pacienteNombre(patient.getNombreCompleto())
                .pacienteDpi(patient.getDpi())
                .recetas(prescriptions)
                .build();
    }

    @Override
    @Transactional
    public PrescriptionResponse validatePrescriptionPayment(Long recetaMedicaId, PharmacyPaymentRequest req, String emailFarmaceutico) {
        resolveRole(emailFarmaceutico, Role.FARMACEUTICO);
        validatePaymentRequest(req);

        MedicalPrescription receta = prescriptionRepository.findById(recetaMedicaId)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada: " + recetaMedicaId));

        validatePrescriptionIsActive(receta);
        validateAdministrativeSolvency(receta.getCitaMedicaDetalleId());
        validatePrescriptionBelongsToDpi(receta, req.getDpiPaciente());

        List<MedicalPrescriptionDetails> pending = prescriptionDetailsRepository.findByRecetaId(receta.getRecetaMedicaId())
                .stream()
                .filter(item -> !item.isDespachado())
                .toList();

        if (pending.isEmpty()) {
            throw new IllegalStateException("La receta ya fue despachada.");
        }

        validateStockForItems(pending);

        pending.forEach(item -> {
            item.setPagoValidado(true);
            prescriptionDetailsRepository.save(item);
        });

        log.info("CU08: Pago farmacia validado recetaId={} metodo={} itemsPendientes={}",
                recetaMedicaId, req.getMetodoPago(), pending.size());

        return toResponse(receta, prescriptionDetailsRepository.findByRecetaId(receta.getRecetaMedicaId()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Despacho (RN09 — solvencia + stock)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public PrescriptionResponse dispense(DispenseMedicineRequest req, String emailFarmaceutico) {
        resolveRole(emailFarmaceutico, Role.FARMACEUTICO);

        MedicalPrescriptionDetails detalle = prescriptionDetailsRepository.findById(req.getRecetaMedicaDetalleId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Detalle de receta no encontrado: " + req.getRecetaMedicaDetalleId()));

        MedicalPrescription receta = prescriptionRepository.findById(detalle.getRecetaMedicaId())
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada"));

        validatePrescriptionIsActive(receta);
        validateAdministrativeSolvency(receta.getCitaMedicaDetalleId());

        // RN09 — FA01: validar que no esté ya despachado
        if (detalle.isDespachado()) {
            throw new IllegalStateException("Este medicamento ya fue despachado.");
        }
        if (!detalle.isPagoValidado()) {
            throw new IllegalStateException("Debe validar el pago en farmacia antes de despachar el medicamento.");
        }

        // RN09 — FA02: validar stock
        Medicine med = medicineRepository.findById(detalle.getMedicamentoId())
                .orElseThrow(() -> new IllegalArgumentException("Medicamento no encontrado: " + detalle.getMedicamentoId()));

        if (med.getStockActual() < detalle.getCantidad()) {
            throw new IllegalStateException(
                    "Stock insuficiente para " + med.getNombre() +
                    ". Disponible: " + med.getStockActual() + ", solicitado: " + detalle.getCantidad());
        }

        // Descontar inventario
        med.setStockActual(med.getStockActual() - detalle.getCantidad());
        medicineRepository.save(med);

        // Marcar despachado
        detalle.setDespachado(true);
        prescriptionDetailsRepository.save(detalle);

        // Crear recordatorio (CU08 FA postcondición)
        createReminderIfApplies(receta, detalle, med);

        log.info("CU08: Despachado detalleId={} medicamento={} cantidad={}",
                detalle.getRecetaMedicaDetalleId(), med.getNombre(), detalle.getCantidad());

        List<MedicalPrescriptionDetails> allItems =
                prescriptionDetailsRepository.findByRecetaId(receta.getRecetaMedicaId());
        return toResponse(receta, allItems);
    }

    @Override
    @Transactional
    public PrescriptionResponse dispensePrescription(Long recetaMedicaId, String emailFarmaceutico) {
        resolveRole(emailFarmaceutico, Role.FARMACEUTICO);

        MedicalPrescription receta = prescriptionRepository.findById(recetaMedicaId)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada: " + recetaMedicaId));

        validatePrescriptionIsActive(receta);
        validateAdministrativeSolvency(receta.getCitaMedicaDetalleId());

        List<MedicalPrescriptionDetails> pending = prescriptionDetailsRepository.findByRecetaId(receta.getRecetaMedicaId())
                .stream()
                .filter(item -> !item.isDespachado())
                .toList();

        if (pending.isEmpty()) {
            throw new IllegalStateException("La receta ya fue despachada.");
        }
        if (pending.stream().anyMatch(item -> !item.isPagoValidado())) {
            throw new IllegalStateException("Debe validar el pago en farmacia antes de despachar la receta.");
        }

        validateStockForItems(pending);

        for (MedicalPrescriptionDetails detalle : pending) {
            Medicine med = medicineRepository.findById(detalle.getMedicamentoId())
                    .orElseThrow(() -> new IllegalArgumentException("Medicamento no encontrado: " + detalle.getMedicamentoId()));
            med.setStockActual(med.getStockActual() - detalle.getCantidad());
            medicineRepository.save(med);

            detalle.setDespachado(true);
            prescriptionDetailsRepository.save(detalle);
            createReminderIfApplies(receta, detalle, med);
        }

        log.info("CU08: Receta despachada recetaId={} items={}", recetaMedicaId, pending.size());

        return toResponse(receta, prescriptionDetailsRepository.findByRecetaId(receta.getRecetaMedicaId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationReminderResponse> getReminders(Long pacienteId) {
        return reminderRepository.findActivosByPacienteId(pacienteId).stream()
                .map(r -> MedicationReminderResponse.builder()
                        .recordatorioId(r.getRecordatorioId())
                        .medicamentoNombre(r.getMedicamentoNombre())
                        .dosis(r.getDosis())
                        .frecuenciaHoras(r.getFrecuenciaHoras())
                        .duracionDias(r.getDuracionDias())
                        .viaAdministracion(r.getViaAdministracion())
                        .proximoRecordatorio(r.getProximoRecordatorio())
                        .activo(r.isActivo())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicationReminderResponse> getRemindersByEmail(String emailPaciente) {
        Long pacienteId = resolvePacienteIdByEmail(emailPaciente);
        return getReminders(pacienteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicineResponse> listMedicines() {
        return medicineRepository.findAllActive().stream()
                .map(m -> MedicineResponse.builder()
                        .medicamentoId(m.getMedicamentoId())
                        .nombre(m.getNombre())
                        .presentacion(m.getPresentacion())
                        .descripcion(m.getDescripcion())
                        .stockActual(m.getStockActual())
                        .precioUnitario(m.getPrecioUnitario())
                        .build())
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private HospitalStaff resolveRole(String email, Role expected) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
        var staff = staffRepository.findByUsuarioId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Perfil de personal no encontrado: " + email));
        if (staff.getRol() != expected && staff.getRol() != Role.ADMIN) {
            throw new IllegalArgumentException("Rol requerido: " + expected + " — actual: " + staff.getRol());
        }
        return staff;
    }

    /**
     * Recupera el pacienteId a partir del cita_medica_detalle_id accediendo a la cita médica.
     * Usamos findById en appointmentDetailsRepository que ya contiene citaMedicaId,
     * luego buscamos el pacienteId en cita_medica (a través del adapter existente).
     */
    private Long resolvePacienteIdFromDetalle(Long citaMedicaDetalleId) {
        MedicalAppointmentDetails d = appointmentDetailsRepository.findById(citaMedicaDetalleId)
                .orElseThrow(() -> new IllegalArgumentException("Detalle no encontrado: " + citaMedicaDetalleId));
        return appointmentRepository.findById(d.getCitaMedicaId())
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"))
                .getPacienteId();
    }

    private Long resolvePacienteIdByEmail(String emailPaciente) {
        var user = userRepository.findByEmail(emailPaciente)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + emailPaciente));
        if (user.getRole() != Role.PACIENTE) {
            throw new IllegalArgumentException("Solo pacientes pueden consultar sus recordatorios en este endpoint.");
        }
        return patientRepository.findByUsuarioId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado para usuario autenticado."))
                .getPacienteId();
    }

    private void validatePrescriptionIsActive(MedicalPrescription prescription) {
        LocalDate emissionDate = prescription.getFechaEmision();
        if (emissionDate == null || emissionDate.isBefore(LocalDate.now().minusDays(PRESCRIPTION_VALID_DAYS))) {
            throw new IllegalStateException("Receta inexistente o vencida.");
        }
    }

    private boolean isPrescriptionActive(MedicalPrescription prescription) {
        LocalDate emissionDate = prescription.getFechaEmision();
        return emissionDate != null && !emissionDate.isBefore(LocalDate.now().minusDays(PRESCRIPTION_VALID_DAYS));
    }

    private void validatePaymentRequest(PharmacyPaymentRequest req) {
        if (req == null || req.getMetodoPago() == null) {
            throw new IllegalArgumentException("Selecciona el metodo de pago de farmacia.");
        }
        if (req.getMetodoPago() == PaymentOption.TARJETA) {
            if (isBlank(req.getBancoTarjeta()) || isBlank(req.getNumeroTarjeta())
                    || isBlank(req.getFechaVencimientoTarjeta()) || isBlank(req.getNombreTitularTarjeta())
                    || isBlank(req.getCvc())) {
                throw new IllegalArgumentException("Completa los datos de tarjeta para validar el pago.");
            }
            if (!req.getNumeroTarjeta().matches("^[0-9]{13,19}$")) {
                throw new IllegalArgumentException("El numero de tarjeta debe tener entre 13 y 19 digitos.");
            }
            if (!req.getFechaVencimientoTarjeta().matches("^(0[1-9]|1[0-2])/[0-9]{2}$")) {
                throw new IllegalArgumentException("La fecha de vencimiento debe usar formato MM/YY.");
            }
            if (!req.getCvc().matches("^[0-9]{3,4}$")) {
                throw new IllegalArgumentException("El CVC debe tener 3 o 4 digitos.");
            }
        }
        if (req.getMetodoPago() == PaymentOption.SEGURO
                && (req.getAseguradoraId() == null || isBlank(req.getNumeroPoliza()))) {
            throw new IllegalArgumentException("Selecciona aseguradora e ingresa numero de poliza.");
        }
    }

    private void validatePrescriptionBelongsToDpi(MedicalPrescription receta, String dpi) {
        if (isBlank(dpi)) {
            return;
        }
        Long pacienteId = resolvePacienteIdFromDetalle(receta.getCitaMedicaDetalleId());
        Patient patient = patientRepository.findById(pacienteId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado para la receta."));
        if (!Objects.equals(patient.getDpi(), dpi)) {
            throw new IllegalArgumentException("La receta seleccionada no pertenece al DPI buscado.");
        }
    }

    private void validateStockForItems(List<MedicalPrescriptionDetails> items) {
        for (MedicalPrescriptionDetails detalle : items) {
            Medicine med = medicineRepository.findById(detalle.getMedicamentoId())
                    .orElseThrow(() -> new IllegalArgumentException("Medicamento no encontrado: " + detalle.getMedicamentoId()));
            if (med.getStockActual() < detalle.getCantidad()) {
                throw new IllegalStateException(
                        "Stock insuficiente para " + med.getNombre() +
                                ". Disponible: " + med.getStockActual() + ", solicitado: " + detalle.getCantidad());
            }
        }
    }

    private void createReminderIfApplies(MedicalPrescription receta, MedicalPrescriptionDetails detalle, Medicine med) {
        if (detalle.getFrecuenciaHoras() == null || detalle.getDuracionDias() == null) {
            return;
        }
        Long pacienteId = resolvePacienteIdFromDetalle(receta.getCitaMedicaDetalleId());
        reminderRepository.save(MedicationReminder.builder()
                .recetaMedicaDetalleId(detalle.getRecetaMedicaDetalleId())
                .pacienteId(pacienteId)
                .medicamentoNombre(med.getNombre())
                .dosis(detalle.getDosis())
                .frecuenciaHoras(detalle.getFrecuenciaHoras())
                .duracionDias(detalle.getDuracionDias())
                .viaAdministracion(detalle.getViaAdministracion())
                .proximoRecordatorio(LocalDateTime.now().plusHours(detalle.getFrecuenciaHoras()))
                .activo(true)
                .build());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void validateAdministrativeSolvency(Long citaMedicaDetalleId) {
        MedicalAppointmentDetails detail = appointmentDetailsRepository.findById(citaMedicaDetalleId)
                .orElseThrow(() -> new IllegalArgumentException("Detalle de cita no encontrado: " + citaMedicaDetalleId));
        var appointment = appointmentRepository.findById(detail.getCitaMedicaId())
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada para validación administrativa."));
        if (appointment.getEstadoAdministrativo() != AdministrativeAppointmentStatus.PAGO_VALIDADO) {
            throw new IllegalStateException("Receta no solvente administrativamente. Valida pago antes del despacho.");
        }
    }

    private PrescriptionResponse toResponse(MedicalPrescription p, List<MedicalPrescriptionDetails> items) {
        Patient patient = null;
        String medicoNombre = null;
        String estadoAdministrativo = null;
        try {
            MedicalAppointmentDetails detail = appointmentDetailsRepository.findById(p.getCitaMedicaDetalleId()).orElse(null);
            if (detail != null) {
                var appointment = appointmentRepository.findById(detail.getCitaMedicaId()).orElse(null);
                if (appointment != null) {
                    patient = patientRepository.findById(appointment.getPacienteId()).orElse(null);
                    estadoAdministrativo = appointment.getEstadoAdministrativo() != null
                            ? appointment.getEstadoAdministrativo().name()
                            : null;
                    if (appointment.getPersonalId() != null) {
                        medicoNombre = staffRepository.findById(appointment.getPersonalId())
                                .map(HospitalStaff::getNombreCompleto)
                                .orElse(null);
                    }
                }
            }
        } catch (RuntimeException ex) {
            log.debug("No se pudo enriquecer recetaId={} con datos de paciente/medico", p.getRecetaMedicaId(), ex);
        }

        var itemResponses = items.stream()
                .map(i -> PrescriptionResponse.PrescriptionDetailResponse.builder()
                        .recetaMedicaDetalleId(i.getRecetaMedicaDetalleId())
                        .medicamentoId(i.getMedicamentoId())
                        .medicamentoNombre(i.getMedicamentoNombre())
                        .cantidad(i.getCantidad())
                        .dosis(i.getDosis())
                        .viaAdministracion(i.getViaAdministracion())
                        .frecuenciaHoras(i.getFrecuenciaHoras())
                        .duracionDias(i.getDuracionDias())
                        .stockActual(i.getStockActual())
                        .precioUnitario(i.getPrecioUnitario())
                        .subtotal((i.getPrecioUnitario() != null ? i.getPrecioUnitario() : 0.0) * i.getCantidad())
                        .disponible(i.isDespachado() || (i.getStockActual() != null && i.getStockActual() >= i.getCantidad()))
                        .despachado(i.isDespachado())
                        .pagoValidado(i.isPagoValidado())
                        .build())
                .toList();

        double total = itemResponses.stream()
                .mapToDouble(item -> item.getSubtotal() != null ? item.getSubtotal() : 0.0)
                .sum();
        boolean pagoFarmaciaValidado = !items.isEmpty() && items.stream()
                .filter(item -> !item.isDespachado())
                .allMatch(MedicalPrescriptionDetails::isPagoValidado);
        boolean despachada = !items.isEmpty() && items.stream().allMatch(MedicalPrescriptionDetails::isDespachado);

        return PrescriptionResponse.builder()
                .recetaMedicaId(p.getRecetaMedicaId())
                .citaMedicaDetalleId(p.getCitaMedicaDetalleId())
                .pacienteId(patient != null ? patient.getPacienteId() : null)
                .pacienteNombre(patient != null ? patient.getNombreCompleto() : null)
                .pacienteDpi(patient != null ? patient.getDpi() : null)
                .medicoNombre(medicoNombre)
                .estadoAdministrativo(estadoAdministrativo)
                .instruccionesGenerales(p.getInstruccionesGenerales())
                .fechaEmision(p.getFechaEmision())
                .createdAt(p.getCreatedAt())
                .pagoFarmaciaValidado(pagoFarmaciaValidado)
                .despachada(despachada)
                .totalMedicamentos(total)
                .items(itemResponses)
                .build();
    }
}




