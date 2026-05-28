package his.application.services;

import his.application.dto.ScheduleAppointmentRequest;
import his.application.dto.TriageRequest;
import his.domain.models.PaymentOption;
import his.domain.ports.InsuranceCatalogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
@RequiredArgsConstructor
public class PaymentValidationService {

    private final InsuranceCatalogRepository insuranceCatalogRepository;

    public PaymentValidationResult validateForAppointment(ScheduleAppointmentRequest request) {
        return validateByPaymentMethod(
                request.getMetodoPago(),
                request.getBancoTarjeta(),
                request.getNumeroTarjeta(),
                request.getFechaVencimientoTarjeta(),
                request.getNombreTitularTarjeta(),
                request.getCvc(),
                request.getAseguradoraId(),
                request.getNumeroPoliza());
    }

    public PaymentValidationResult validateForTriage(TriageRequest request) {
        return validateByPaymentMethod(
                request.getMetodoPago(),
                request.getBancoTarjeta(),
                request.getNumeroTarjeta(),
                request.getFechaVencimientoTarjeta(),
                request.getNombreTitularTarjeta(),
                request.getCvc(),
                request.getAseguradoraId(),
                request.getPolizaSeguro());
    }

    private PaymentValidationResult validateByPaymentMethod(
            PaymentOption paymentOption,
            String bancoTarjeta,
            String numeroTarjeta,
            String fechaVencimientoTarjeta,
            String nombreTitularTarjeta,
            String cvc,
            Long aseguradoraId,
            String numeroPoliza
    ) {
        if (paymentOption == null) {
            throw new IllegalArgumentException("Debe seleccionar metodo de pago");
        }

        if (paymentOption == PaymentOption.TARJETA) {
            return simulateCardAuthorization(bancoTarjeta, numeroTarjeta, fechaVencimientoTarjeta, nombreTitularTarjeta, cvc);
        }
        if (paymentOption == PaymentOption.SEGURO) {
            return simulateInsuranceCoverage(aseguradoraId, numeroPoliza);
        }
        throw new IllegalArgumentException("Metodo de pago no soportado");
    }

    private PaymentValidationResult simulateCardAuthorization(
            String bancoTarjeta,
            String numeroTarjeta,
            String fechaVencimientoTarjeta,
            String nombreTitularTarjeta,
            String cvc
    ) {
        if (isBlank(bancoTarjeta)
                || isBlank(numeroTarjeta)
                || isBlank(fechaVencimientoTarjeta)
                || isBlank(nombreTitularTarjeta)
                || isBlank(cvc)) {
            throw new IllegalArgumentException("Para pago con tarjeta debe completar banco, numero, vencimiento, titular y CVC");
        }

        YearMonth expiry = parseExpiry(fechaVencimientoTarjeta.trim());
        YearMonth now = YearMonth.now();
        if (expiry.isBefore(now)) {
            throw new IllegalArgumentException("La fecha de vencimiento de la tarjeta no puede estar expirada");
        }

        boolean approved = !numeroTarjeta.trim().endsWith("0000");
        if (approved) {
            return new PaymentValidationResult(true, "Pago con tarjeta validado correctamente");
        }
        return new PaymentValidationResult(false, "Pago con tarjeta pendiente: simulacion de saldo insuficiente");
    }

    private PaymentValidationResult simulateInsuranceCoverage(Long aseguradoraId, String numeroPoliza) {
        if (aseguradoraId == null || isBlank(numeroPoliza)) {
            throw new IllegalArgumentException("Para cobertura de seguro debe enviar aseguradoraId y numeroPoliza");
        }

        insuranceCatalogRepository.findById(aseguradoraId)
                .orElseThrow(() -> new IllegalArgumentException("La aseguradora enviada no existe o no esta activa"));

        String policy = numeroPoliza.trim().toUpperCase();
        boolean approved = !(policy.startsWith("X") || policy.contains("RECHAZADA"));

        if (approved) {
            return new PaymentValidationResult(true, "Cobertura de seguro validada correctamente");
        }
        return new PaymentValidationResult(false, "Cobertura pendiente: simulacion de poliza no vigente/no cubierta");
    }

    private YearMonth parseExpiry(String expiry) {
        try {
            String[] parts = expiry.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = 2000 + Integer.parseInt(parts[1]);
            return YearMonth.of(year, month);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("La fecha de vencimiento debe tener formato MM/yy");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record PaymentValidationResult(boolean approved, String message) {
    }
}

