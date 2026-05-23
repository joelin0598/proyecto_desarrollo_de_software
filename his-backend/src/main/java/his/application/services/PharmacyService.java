package his.application.services;

import his.application.dto.CreatePrescriptionRequest;
import his.application.dto.DispenseMedicineRequest;
import his.application.dto.MedicationReminderResponse;
import his.application.dto.MedicineResponse;
import his.application.dto.PrescriptionResponse;
import his.application.usecases.PharmacyUseCase;
import his.domain.models.HospitalStaff;
import his.domain.models.MedicalPrescription;
import his.domain.models.MedicalPrescriptionDetails;
import his.domain.models.MedicationReminder;
import his.domain.models.Medicine;
import his.domain.models.MedicalAppointmentDetails;
import his.domain.models.Role;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.MedicalAppointmentDetailsRepository;
import his.domain.ports.MedicalAppointmentRepository;
import his.domain.ports.MedicalPrescriptionDetailsRepository;
import his.domain.ports.MedicalPrescriptionRepository;
import his.domain.ports.MedicationReminderRepository;
import his.domain.ports.MedicineRepository;
import his.domain.ports.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * CU08 — Servicios de farmacia: creación de recetas, despacho y recordatorios.
 * Aplica RN03 (solvencia), RN09 (stock disponible), RN10 (ciclo clínico).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyService implements PharmacyUseCase {

    private final MedicalPrescriptionRepository prescriptionRepository;
    private final MedicalPrescriptionDetailsRepository prescriptionDetailsRepository;
    private final MedicineRepository medicineRepository;
    private final MedicationReminderRepository reminderRepository;
    private final MedicalAppointmentDetailsRepository appointmentDetailsRepository;
    private final MedicalAppointmentRepository appointmentRepository;
    private final HospitalStaffRepository staffRepository;
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
        List<MedicalPrescriptionDetails> items =
                prescriptionDetailsRepository.findByRecetaId(prescription.getRecetaMedicaId());
        return toResponse(prescription, items);
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

        // RN09 — FA01: validar que no esté ya despachado
        if (detalle.isDespachado()) {
            throw new IllegalStateException("Este medicamento ya fue despachado.");
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
        detalle.setPagoValidado(true);
        prescriptionDetailsRepository.save(detalle);

        // Crear recordatorio (CU08 FA postcondición)
        if (detalle.getFrecuenciaHoras() != null && detalle.getDuracionDias() != null) {
            // Obtener paciente desde la cita a través de la receta
            MedicalPrescription receta = prescriptionRepository.findById(detalle.getRecetaMedicaId())
                    .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada"));
            MedicalAppointmentDetails apptDetail = appointmentDetailsRepository
                    .findById(receta.getCitaMedicaDetalleId())
                    .orElseThrow(() -> new IllegalArgumentException("Detalle de cita no encontrado"));

            // Obtener paciente_id de la cita médica — necesitamos acceso mínimo
            // Lo guardamos contra la receta; el pacienteId viene de la cita_medica
            // Para evitar un join extra usamos un helper en el detalle de la cita
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

        log.info("CU08: Despachado detalleId={} medicamento={} cantidad={}",
                detalle.getRecetaMedicaDetalleId(), med.getNombre(), detalle.getCantidad());

        MedicalPrescription receta = prescriptionRepository.findById(detalle.getRecetaMedicaId()).orElseThrow();
        List<MedicalPrescriptionDetails> allItems =
                prescriptionDetailsRepository.findByRecetaId(receta.getRecetaMedicaId());
        return toResponse(receta, allItems);
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

    private PrescriptionResponse toResponse(MedicalPrescription p, List<MedicalPrescriptionDetails> items) {
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
                        .despachado(i.isDespachado())
                        .pagoValidado(i.isPagoValidado())
                        .build())
                .toList();

        return PrescriptionResponse.builder()
                .recetaMedicaId(p.getRecetaMedicaId())
                .citaMedicaDetalleId(p.getCitaMedicaDetalleId())
                .instruccionesGenerales(p.getInstruccionesGenerales())
                .fechaEmision(p.getFechaEmision())
                .createdAt(p.getCreatedAt())
                .items(itemResponses)
                .build();
    }
}




