package his.integration;

import his.application.dto.AddLaboratoryResultRequest;
import his.application.dto.CloseMedicalAppointmentAttentionRequest;
import his.application.dto.CreateLaboratoryOrderRequest;
import his.application.dto.CreatePrescriptionRequest;
import his.application.dto.DispenseMedicineRequest;
import his.application.dto.PharmacyPaymentRequest;
import his.application.dto.RegisterRequest;
import his.application.dto.RegisterRequestAdmin;
import his.application.dto.ScheduleAppointmentRequest;
import his.application.services.AppointmentService;
import his.application.services.AuthService;
import his.application.services.LaboratoryService;
import his.application.services.MedicalAppointmentAttentionService;
import his.application.services.PharmacyService;
import his.domain.models.PatientGender;
import his.domain.models.PaymentOption;
import his.domain.models.Role;
import his.domain.models.StatusAppointment;
import his.infrastructure.persistence.entities.MedicalSpecialityCatalogJpaEntity;
import his.infrastructure.persistence.entities.MedicineJpaEntity;
import his.infrastructure.persistence.repositories.HospitalStaffJpaRepository;
import his.infrastructure.persistence.repositories.MedicalSpecialityJpaRepository;
import his.infrastructure.persistence.repositories.MedicineJpaRepository;
import his.infrastructure.persistence.repositories.PatientJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClinicalLabPharmacyFlowIntegrationTest {

    @Autowired private AuthService authService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private MedicalAppointmentAttentionService attentionService;
    @Autowired private LaboratoryService laboratoryService;
    @Autowired private PharmacyService pharmacyService;
    @Autowired private PatientJpaRepository patientJpaRepository;
    @Autowired private HospitalStaffJpaRepository hospitalStaffJpaRepository;
    @Autowired private MedicalSpecialityJpaRepository specialityJpaRepository;
    @Autowired private MedicineJpaRepository medicineJpaRepository;

    @Test
    void cu06_cu07_cu08_fullIntegrationFlow_worksEndToEnd() {
        Long especialidadId = specialityJpaRepository.save(MedicalSpecialityCatalogJpaEntity.builder()
                .nombre("Medicina General CU06-07-08")
                .descripcion("Integracion completa")
                .build()).getEspecialidadId();

        MedicineJpaEntity medicine = medicineJpaRepository.save(MedicineJpaEntity.builder()
                .nombre("Paracetamol 500mg")
                .presentacion("Tabletas")
                .descripcion("Analgésico")
                .stockActual(100)
                .precioUnitario(1.5)
                .build());

        authService.register(RegisterRequest.builder()
                .nombreCompleto("Paciente Integración")
                .email("paciente.cu060708@example.com")
                .password("Segura1!")
                .dpi("7777777777777")
                .genero(PatientGender.FEMENINO)
                .build());

        Long pacienteId = patientJpaRepository.findByDpi("7777777777777").orElseThrow().getPacienteId();

        var doctorAuth = authService.registerPersonal(RegisterRequestAdmin.builder()
                .nombreCompleto("Dr. Integración")
                .email("doctor.cu060708@example.com")
                .password("Segura1!")
                .direccion("Zona 10")
                .telefonoCorporativo("50212345684")
                .rol(Role.DOCTOR)
                .especialidadId(especialidadId)
                .numeroColegiado("COL-CU060708-001")
                .build());

        Long doctorPersonalId = hospitalStaffJpaRepository
                .findByUsuarioSistemaUsuarioId(doctorAuth.getUser().getId())
                .orElseThrow()
                .getPersonalId();

        authService.registerPersonal(RegisterRequestAdmin.builder()
                .nombreCompleto("Recepción Integración")
                .email("recepcion.cu060708@example.com")
                .password("Segura1!")
                .direccion("Zona 1")
                .telefonoCorporativo("50287654327")
                .rol(Role.RECEPCION)
                .numeroColegiado("COL-CU060708-002")
                .build());

        authService.registerPersonal(RegisterRequestAdmin.builder()
                .nombreCompleto("Laboratorista Integración")
                .email("lab.cu060708@example.com")
                .password("Segura1!")
                .direccion("Zona 2")
                .telefonoCorporativo("50287654328")
                .rol(Role.LABORATORISTA)
                .numeroColegiado("COL-CU060708-003")
                .build());

        authService.registerPersonal(RegisterRequestAdmin.builder()
                .nombreCompleto("Farmacia Integración")
                .email("farmacia.cu060708@example.com")
                .password("Segura1!")
                .direccion("Zona 3")
                .telefonoCorporativo("50287654329")
                .rol(Role.FARMACEUTICO)
                .numeroColegiado("COL-CU060708-004")
                .build());

        var cita = appointmentService.scheduleAppointment(
                ScheduleAppointmentRequest.builder()
                        .pacienteId(pacienteId)
                        .medicoPersonalId(doctorPersonalId)
                        .especialidadId(especialidadId)
                        .fechaCita(LocalDate.now().plusDays(2))
                        .horaCita(LocalTime.of(9, 30))
                        .motivoConsulta("Flujo integración CU06-CU07-CU08")
                        .metodoPago(PaymentOption.TARJETA)
                        .bancoTarjeta("Banco Demo")
                        .numeroTarjeta("4111111111111111")
                        .fechaVencimientoTarjeta("12/30")
                        .nombreTitularTarjeta("PACIENTE INTEGRACION")
                        .cvc("123")
                        .build(),
                "recepcion.cu060708@example.com"
        );

        var apertura = attentionService.openAttention(cita.getCitaMedicaId(), "doctor.cu060708@example.com");
        Long detalleId = apertura.getCitaMedicaDetalleId();
        assertNotNull(detalleId);

        CreateLaboratoryOrderRequest orderRequest = new CreateLaboratoryOrderRequest();
        orderRequest.setCitaMedicaDetalleId(detalleId);
        orderRequest.setNombreExamen("Glucosa");
        orderRequest.setTipoMuestra("Sangre");

        var orden = laboratoryService.createOrder(orderRequest, "doctor.cu060708@example.com");

        assertNotNull(orden.getOrdenLaboratorioId());
        assertEquals("PENDIENTE_MUESTRA", orden.getEstado().name());

        var recibida = laboratoryService.receiveSample(orden.getOrdenLaboratorioId(), "lab.cu060708@example.com");
        assertEquals("EN_PROCESO", recibida.getEstado().name());
        assertNotNull(recibida.getEtiquetaId());

        AddLaboratoryResultRequest resultadoReq = new AddLaboratoryResultRequest();
        resultadoReq.setOrdenLaboratorioId(orden.getOrdenLaboratorioId());
        resultadoReq.setNombreExamen("Glucosa");
        resultadoReq.setValorResultado(new BigDecimal("250"));
        resultadoReq.setUnidadResultado("mg/dL");
        resultadoReq.setReferenciaMinima(new BigDecimal("70"));
        resultadoReq.setReferenciaMaxima(new BigDecimal("110"));
        resultadoReq.setConclusion("Hiperglucemia crítica");
        resultadoReq.setObservaciones("Validar manejo clínico inmediato");

        var ordenFinalizada = laboratoryService.addResult(resultadoReq, "lab.cu060708@example.com");
        assertEquals("FINALIZADO", ordenFinalizada.getEstado().name());
        assertTrue(ordenFinalizada.isAlertaCritica());
        assertTrue(ordenFinalizada.getResultado().isCritico());

        CreatePrescriptionRequest prescriptionRequest = new CreatePrescriptionRequest();
        prescriptionRequest.setCitaMedicaDetalleId(detalleId);
        prescriptionRequest.setInstruccionesGenerales("Tomar con alimentos");
        CreatePrescriptionRequest.PrescriptionItemRequest item = new CreatePrescriptionRequest.PrescriptionItemRequest();
        item.setMedicamentoId(medicine.getMedicamentoId());
        item.setCantidad(2);
        item.setDosis("500mg");
        item.setViaAdministracion("Oral");
        item.setFrecuenciaHoras(8);
        item.setDuracionDias(5);
        prescriptionRequest.setItems(List.of(item));

        var receta = pharmacyService.createPrescription(prescriptionRequest, "doctor.cu060708@example.com");
        assertNotNull(receta.getRecetaMedicaId());
        assertEquals(1, receta.getItems().size());
        assertFalse(receta.getItems().get(0).isDespachado());

        var recetaCargada = pharmacyService.getPrescription(detalleId);
        Long recetaDetalleId = recetaCargada.getItems().get(0).getRecetaMedicaDetalleId();
        Long recetaMedicaId = recetaCargada.getRecetaMedicaId();

        // CU08: Validar pago en farmacia antes de despachar (regla de negocio RN09)
        PharmacyPaymentRequest paymentRequest = new PharmacyPaymentRequest();
        paymentRequest.setMetodoPago(PaymentOption.TARJETA);
        paymentRequest.setBancoTarjeta("Banco Demo");
        paymentRequest.setNumeroTarjeta("4111111111111111");
        paymentRequest.setFechaVencimientoTarjeta("12/30");
        paymentRequest.setNombreTitularTarjeta("PACIENTE INTEGRACION");
        paymentRequest.setCvc("123");
        var recetaPagada = pharmacyService.validatePrescriptionPayment(recetaMedicaId, paymentRequest, "farmacia.cu060708@example.com");
        assertTrue(recetaPagada.isPagoFarmaciaValidado(), "CU08: El pago de farmacia debe quedar validado antes del despacho");

        DispenseMedicineRequest dispenseRequest = new DispenseMedicineRequest();
        dispenseRequest.setRecetaMedicaDetalleId(recetaDetalleId);
        var recetaDespachada = pharmacyService.dispense(dispenseRequest, "farmacia.cu060708@example.com");
        assertTrue(recetaDespachada.getItems().get(0).isDespachado());

        var reminders = pharmacyService.getReminders(pacienteId);
        assertFalse(reminders.isEmpty(), "CU08: Debe crear recordatorio al despachar");

        CloseMedicalAppointmentAttentionRequest closeRequest = CloseMedicalAppointmentAttentionRequest.builder()
                .citaMedicaDetalleId(detalleId)
                .evaluacionFisica("Paciente estable, signos vitales sin deterioro hemodinámico.")
                .diagnostico("Síndrome metabólico en estudio")
                .ordenLaboratorio("Orden #" + orden.getOrdenLaboratorioId() + " - Glucosa")
                .recetaMedica("Receta #" + receta.getRecetaMedicaId())
                .medicacionPrescrita("Paracetamol 500mg c/8h por 5 días")
                .requiereSeguimiento(true)
                .build();

        var cierre = attentionService.closeAttention(closeRequest, "doctor.cu060708@example.com");
        assertEquals(StatusAppointment.ATENDIDA, cierre.getEstado());
        assertNotNull(cierre.getCitaSeguimientoId());
    }
}


