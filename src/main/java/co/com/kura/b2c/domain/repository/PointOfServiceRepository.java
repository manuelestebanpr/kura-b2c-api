package co.com.kura.b2c.domain.repository;

import co.com.kura.b2c.domain.entity.PointOfService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PointOfServiceRepository extends JpaRepository<PointOfService, UUID> {

    Optional<PointOfService> findByIdAndDeletedAtIsNull(UUID id);
}
