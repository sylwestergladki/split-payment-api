package pl.sylwestergladki.split_payment_api.identity.infrastructure.persistance;

import org.springframework.stereotype.Repository;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUser;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUserRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;


import java.util.List;
import java.util.Optional;

@Repository
public class AppUserPersistenceAdapter
        implements AppUserRepository {

    private final SpringDataAppUserRepository jpaRepository;

    public AppUserPersistenceAdapter(
            SpringDataAppUserRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<AppUser> findByKeycloakSubject(
            String keycloakSubject
    ) {
        return jpaRepository
                .findByKeycloakSubject(keycloakSubject)
                .map(this::toDomain);
    }

    @Override
    public AppUser save(AppUser user) {
        AppUserEntity entity = toEntity(user);

        AppUserEntity saved = jpaRepository.save(entity);

        return toDomain(saved);
    }

    @Override
    public List<AppUser> findAllByIds(List<UserId> userIds) {
        var ids = userIds.stream()
                .map(UserId::value)
                .toList();

        return jpaRepository.findAllById(ids).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(UserId userId) {
        return jpaRepository.existsById(userId.value());
    }

    private AppUser toDomain(AppUserEntity entity) {
        return AppUser.reconstitute(
                new UserId(entity.getId()),
                entity.getKeycloakSubject(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AppUserEntity toEntity(AppUser user) {
        return new AppUserEntity(
                user.id().value(),
                user.keycloakSubject(),
                user.email(),
                user.displayName(),
                user.createdAt(),
                user.updatedAt()
        );
    }
}