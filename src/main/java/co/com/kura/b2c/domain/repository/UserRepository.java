package co.com.kura.b2c.domain.repository;

import co.com.kura.b2c.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByCedulaAndDeletedAtIsNull(String cedula);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByCedulaAndDeletedAtIsNull(String cedula);
}
