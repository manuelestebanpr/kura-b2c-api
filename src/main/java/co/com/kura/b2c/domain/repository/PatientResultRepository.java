package co.com.kura.b2c.domain.repository;

import co.com.kura.b2c.domain.entity.PatientResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientResultRepository extends JpaRepository<PatientResult, UUID> {

    List<PatientResult> findByOrderId(UUID orderId);

    List<PatientResult> findByPatientId(UUID patientId);
}
