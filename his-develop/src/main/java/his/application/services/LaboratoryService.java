package his.application.services;

import his.application.dto.AddLaboratoryResultRequest;
import his.application.dto.CreateLaboratoryOrderRequest;
import his.application.dto.LaboratoryOrderResponse;
import his.application.dto.LaboratoryResultResponse;
import his.application.usecases.LaboratoryUseCase;
import his.domain.models.HospitalStaff;
import his.domain.models.LaboratoryOrder;
import his.domain.models.LaboratoryOrderStatus;
import his.domain.models.LaboratoryResult;
import his.domain.models.MedicalAppointmentDetails;
import his.domain.models.Role;
import his.domain.models.AdministrativeAppointmentStatus;
import his.domain.ports.HospitalStaffRepository;
import his.domain.ports.LaboratoryOrderRepository;
import his.domain.ports.LaboratoryResultRepository;
import his.domain.ports.MedicalAppointmentRepository;
import his.domain.ports.MedicalAppointmentDetailsRepository;
import his.domain.ports.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * CU07 — Servicio de laboratorio: creación de órdenes, recepción de muestras
 * y registro de resultados con detección de valores críticos (FA03 / RN07).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LaboratoryService implements LaboratoryUseCase {

    private final LaboratoryOrderRepository orderRepository;
    private final LaboratoryResultRepository resultRepository;
    private final MedicalAppointmentDetailsRepository detailsRepository;
    private final MedicalAppointmentRepository appointmentRepository;
    private final HospitalStaffRepository staffRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Crear orden (RN07: requiere que la cita tenga pago validado)
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public LaboratoryOrderResponse createOrder(CreateLaboratoryOrderRequest req, String emailLaboratorista) {
        HospitalStaff staff = resolveLabOrderCreator(emailLaboratorista);

        detailsRepository.findById(req.getCitaMedicaDetalleId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Detalle de cita no encontrado: " + req.getCitaMedicaDetalleId()));

        validateAdministrativeSolvency(req.getCitaMedicaDetalleId());

        LaboratoryOrder order = LaboratoryOrder.builder()
                .citaMedicaDetalleId(req.getCitaMedicaDetalleId())
                .personalId(staff.getPersonalId())
                .nombreExamen(req.getNombreExamen())
                .tipoMuestra(req.getTipoMuestra())
                .estado(LaboratoryOrderStatus.PENDIENTE_MUESTRA)
                .pagoValidado(true)
                .alertaCritica(false)
                .build();

        LaboratoryOrder saved = orderRepository.save(order);
        log.info("CU07: Orden laboratorio creada ordenId={} detalle={} examen={}",
                saved.getOrdenLaboratorioId(), req.getCitaMedicaDetalleId(), req.getNombreExamen());

        return toResponse(saved, null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Recepción de muestra → EN_PROCESO + etiqueta única
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public LaboratoryOrderResponse receiveSample(Long ordenLaboratorioId, String emailLaboratorista) {
        resolveStaff(emailLaboratorista);

        LaboratoryOrder order = findOrder(ordenLaboratorioId);
        validateAdministrativeSolvency(order.getCitaMedicaDetalleId());
        if (order.getEstado() != LaboratoryOrderStatus.PENDIENTE_MUESTRA) {
            throw new IllegalStateException("La orden no está en estado PENDIENTE_MUESTRA — estado actual: " + order.getEstado());
        }

        String etiqueta = "LAB-" + ordenLaboratorioId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        order.setEstado(LaboratoryOrderStatus.EN_PROCESO);
        order.setEtiquetaId(etiqueta);

        LaboratoryOrder saved = orderRepository.save(order);
        log.info("CU07: Muestra recibida ordenId={} etiqueta={}", ordenLaboratorioId, etiqueta);

        return toResponse(saved, resultRepository.findByOrdenId(ordenLaboratorioId).orElse(null));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FA02 — Rechazar muestra
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public LaboratoryOrderResponse rejectSample(Long ordenLaboratorioId, String motivo, String emailLaboratorista) {
        resolveStaff(emailLaboratorista);

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Debes indicar el motivo de rechazo de muestra.");
        }

        LaboratoryOrder order = findOrder(ordenLaboratorioId);
        if (order.getEstado() == LaboratoryOrderStatus.FINALIZADO) {
            throw new IllegalStateException("No se puede rechazar una orden ya finalizada.");
        }
        if (order.getEstado() == LaboratoryOrderStatus.MUESTRA_RECHAZADA) {
            throw new IllegalStateException("La muestra ya fue rechazada previamente.");
        }
        if (order.getEstado() != LaboratoryOrderStatus.PENDIENTE_MUESTRA
                && order.getEstado() != LaboratoryOrderStatus.EN_PROCESO) {
            throw new IllegalStateException("Solo se puede rechazar una muestra pendiente o en proceso.");
        }

        order.setEstado(LaboratoryOrderStatus.MUESTRA_RECHAZADA);
        order.setObservacionesTecnico(motivo);

        LaboratoryOrder saved = orderRepository.save(order);
        log.warn("CU07: Muestra rechazada ordenId={} motivo={}", ordenLaboratorioId, motivo);

        return toResponse(saved, null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Registrar resultado + FA03 detección valores críticos
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public LaboratoryOrderResponse addResult(AddLaboratoryResultRequest req, String emailLaboratorista) {
        resolveStaff(emailLaboratorista);

        LaboratoryOrder order = findOrder(req.getOrdenLaboratorioId());
        validateAdministrativeSolvency(order.getCitaMedicaDetalleId());
        if (order.getEstado() != LaboratoryOrderStatus.EN_PROCESO) {
            throw new IllegalStateException("La orden debe estar EN_PROCESO para registrar resultados.");
        }
        if (resultRepository.findByOrdenId(req.getOrdenLaboratorioId()).isPresent()) {
            throw new IllegalStateException("La orden ya tiene un resultado registrado.");
        }
        if (req.getNombreExamen() == null
                || !order.getNombreExamen().trim().equalsIgnoreCase(req.getNombreExamen().trim())) {
            throw new IllegalArgumentException("El nombre del examen no coincide con la orden seleccionada.");
        }

        validateResultRanges(req);

        // FA03 — detectar valores críticos fuera de rango
        boolean critico = false;
        if (req.getValorResultado() != null && req.getReferenciaMinima() != null && req.getReferenciaMaxima() != null) {
            critico = req.getValorResultado().compareTo(req.getReferenciaMinima()) < 0
                    || req.getValorResultado().compareTo(req.getReferenciaMaxima()) > 0;
        }

        LaboratoryResult result = LaboratoryResult.builder()
                .ordenLaboratorioId(req.getOrdenLaboratorioId())
                .nombreExamen(req.getNombreExamen())
                .valorResultado(req.getValorResultado())
                .unidadResultado(req.getUnidadResultado())
                .referenciaMinima(req.getReferenciaMinima())
                .referenciaMaxima(req.getReferenciaMaxima())
                .observaciones(req.getObservaciones())
                .resumen(req.getResumen())
                .conclusion(req.getConclusion())
                .critico(critico)
                .build();

        LaboratoryResult savedResult = resultRepository.save(result);

        // Marcar orden como FINALIZADA y alerta crítica si aplica
        order.setEstado(LaboratoryOrderStatus.FINALIZADO);
        order.setAlertaCritica(critico);
        LaboratoryOrder savedOrder = orderRepository.save(order);

        if (critico) {
            log.warn("CU07-FA03: VALORES CRÍTICOS detectados ordenId={} examen={} valor={}",
                    req.getOrdenLaboratorioId(), req.getNombreExamen(), req.getValorResultado());
        } else {
            log.info("CU07: Resultado registrado ordenId={} examen={}", req.getOrdenLaboratorioId(), req.getNombreExamen());
        }

        return toResponse(savedOrder, savedResult);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaboratoryOrderResponse> getOrdersByDetalle(Long citaMedicaDetalleId) {
        return orderRepository.findByCitaMedicaDetalleId(citaMedicaDetalleId).stream()
                .map(o -> toResponse(o, resultRepository.findByOrdenId(o.getOrdenLaboratorioId()).orElse(null)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LaboratoryOrderResponse getOrder(Long ordenLaboratorioId) {
        LaboratoryOrder order = findOrder(ordenLaboratorioId);
        return toResponse(order, resultRepository.findByOrdenId(ordenLaboratorioId).orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaboratoryOrderResponse> getResultsByPatient(Long patientId) {
        // Obtener citas del paciente y todas las órdenes asociadas; devolver solo órdenes FINALIZADO
        return appointmentRepository.findByPacienteIdOrderByDateTimeDesc(patientId).stream()
                .map(cita -> detailsRepository.findByCitaMedicaId(cita.getCitaMedicaId()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .flatMap(det -> orderRepository.findByCitaMedicaDetalleId(det.getMedicalAppointmentDetailsId()).stream())
                .filter(o -> o.getEstado() == LaboratoryOrderStatus.FINALIZADO)
                .map(o -> toResponse(o, resultRepository.findByOrdenId(o.getOrdenLaboratorioId()).orElse(null)))
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private LaboratoryOrder findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orden de laboratorio no encontrada: " + id));
    }

    private void validateAdministrativeSolvency(Long citaMedicaDetalleId) {
        MedicalAppointmentDetails detalle = detailsRepository.findById(citaMedicaDetalleId)
                .orElseThrow(() -> new IllegalArgumentException("Detalle de cita no encontrado: " + citaMedicaDetalleId));
        var cita = appointmentRepository.findById(detalle.getCitaMedicaId())
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada para el detalle indicado."));
        if (cita.getEstadoAdministrativo() != AdministrativeAppointmentStatus.PAGO_VALIDADO) {
            throw new IllegalStateException("Examen no solvente. Debe validar pago antes de recibir muestra.");
        }
    }

    private void resolveStaff(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
        var staff = staffRepository.findByUsuarioId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Perfil de personal no encontrado: " + email));
        if (staff.getRol() != Role.LABORATORISTA && staff.getRol() != Role.ADMIN) {
            throw new IllegalArgumentException("Rol requerido: LABORATORISTA — actual: " + staff.getRol());
        }
    }

    private HospitalStaff resolveLabOrderCreator(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + email));
        var staff = staffRepository.findByUsuarioId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Perfil de personal no encontrado: " + email));

        Role role = staff.getRol();
        if (role != Role.LABORATORISTA && role != Role.DOCTOR && role != Role.ADMIN) {
            throw new IllegalArgumentException("Rol requerido: LABORATORISTA, DOCTOR o ADMIN — actual: " + role);
        }
        return staff;
    }

    private void validateResultRanges(AddLaboratoryResultRequest req) {
        boolean hasAnyRangeValue = req.getValorResultado() != null
                || req.getReferenciaMinima() != null
                || req.getReferenciaMaxima() != null;

        if (!hasAnyRangeValue) {
            return;
        }

        if (req.getValorResultado() == null || req.getReferenciaMinima() == null || req.getReferenciaMaxima() == null) {
            throw new IllegalArgumentException("Debes completar valorResultado, referenciaMinima y referenciaMaxima cuando reportas valores numéricos.");
        }

        if (req.getReferenciaMinima().compareTo(req.getReferenciaMaxima()) > 0) {
            throw new IllegalArgumentException("referenciaMinima no puede ser mayor a referenciaMaxima.");
        }
    }

    private LaboratoryOrderResponse toResponse(LaboratoryOrder o, LaboratoryResult r) {
        LaboratoryResultResponse resultResp = null;
        if (r != null) {
            resultResp = LaboratoryResultResponse.builder()
                    .resultadoLaboratorioId(r.getResultadoLaboratorioId())
                    .ordenLaboratorioId(r.getOrdenLaboratorioId())
                    .nombreExamen(r.getNombreExamen())
                    .valorResultado(r.getValorResultado())
                    .unidadResultado(r.getUnidadResultado())
                    .referenciaMinima(r.getReferenciaMinima())
                    .referenciaMaxima(r.getReferenciaMaxima())
                    .observaciones(r.getObservaciones())
                    .resumen(r.getResumen())
                    .conclusion(r.getConclusion())
                    .critico(r.isCritico())
                    .createdAt(r.getCreatedAt())
                    .build();
        }
        return LaboratoryOrderResponse.builder()
                .ordenLaboratorioId(o.getOrdenLaboratorioId())
                .citaMedicaDetalleId(o.getCitaMedicaDetalleId())
                .nombreExamen(o.getNombreExamen())
                .tipoMuestra(o.getTipoMuestra())
                .estado(o.getEstado())
                .pagoValidado(o.isPagoValidado())
                .etiquetaId(o.getEtiquetaId())
                .alertaCritica(o.isAlertaCritica())
                .observacionesTecnico(o.getObservacionesTecnico())
                .createdAt(o.getCreatedAt())
                .resultado(resultResp)
                .build();
    }
}

