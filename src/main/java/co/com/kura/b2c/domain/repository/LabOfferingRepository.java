package co.com.kura.b2c.domain.repository;

import co.com.kura.b2c.domain.entity.LabOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabOfferingRepository extends JpaRepository<LabOffering, UUID> {

    Optional<LabOffering> findByPosIdAndServiceIdAndDeletedAtIsNull(UUID posId, UUID serviceId);

    List<LabOffering> findByPosIdAndIsAvailableTrueAndDeletedAtIsNull(UUID posId);
}
