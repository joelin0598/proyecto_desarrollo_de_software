package his.domain.ports;

import his.domain.TriageRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TriageRecordRepository extends JpaRepository<TriageRecord, Long> {
    List<TriageRecord> findByPatient_PatientIdOrderByArrivalTimeDesc(Long patientId);
    List<TriageRecord> findAllByOrderByArrivalTimeDesc();
}
