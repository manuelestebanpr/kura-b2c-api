package co.com.kura.b2c.domain.repository;

import co.com.kura.b2c.domain.entity.MasterService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MasterServiceRepository extends JpaRepository<MasterService, UUID> {

    Optional<MasterService> findByCodeAndDeletedAtIsNull(String code);
}
