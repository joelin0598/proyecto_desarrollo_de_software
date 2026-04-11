package his.domain.ports;

import his.domain.PaymentEntity;
import his.domain.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByAppointment_Id(Long appointmentId);
    List<PaymentEntity> findByPaymentStatus(PaymentStatus status);
}
