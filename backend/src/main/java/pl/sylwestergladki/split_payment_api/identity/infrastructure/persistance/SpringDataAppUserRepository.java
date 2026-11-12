package pl.sylwestergladki.split_payment_api.identity.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataAppUserRepository
        extends JpaRepository<AppUserEntity, UUID> {

    Optional<AppUserEntity> findByKeycloakSubject(
            String keycloakSubject
    );
}
