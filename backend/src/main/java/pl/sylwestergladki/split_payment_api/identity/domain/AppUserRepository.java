package pl.sylwestergladki.split_payment_api.identity.domain;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository {

    Optional<AppUser> findByKeycloakSubject(
            String keycloakSubject
    );

    AppUser save(AppUser user);

    List<AppUser> findAllByIds(List<UserId> userIds);

    boolean existsById(UserId userId);
}