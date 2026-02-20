package co.com.kura.b2c.domain.repository;

import co.com.kura.b2c.domain.entity.BundleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BundleItemRepository extends JpaRepository<BundleItem, UUID> {

    List<BundleItem> findByBundleId(UUID bundleId);
}
