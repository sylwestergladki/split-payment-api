package pl.sylwestergladki.split_payment_api.identity.domain;

import java.util.Optional;

public interface AppUserRepository {

    Optional<AppUser> findByKeycloakSubject(
            String keycloakSubject
    );

    AppUser save(AppUser user);

    boolean existsById(UserId userId);
}