package pl.sylwestergladki.split_payment_api.identity.application;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUser;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUserRepository;


@Service
public class SyncUserProfileUseCase {

    private final AppUserRepository userRepository;

    public SyncUserProfileUseCase(
            AppUserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AppUser execute(
            String keycloakSubject,
            String email,
            String displayName
    ) {
        return userRepository
                .findByKeycloakSubject(keycloakSubject)
                .map(user -> {
                    user.updateProfile(email, displayName);
                    return userRepository.save(user);
                })
                .orElseGet(() ->
                        userRepository.save(
                                AppUser.register(
                                        keycloakSubject,
                                        email,
                                        displayName
                                )
                        )
                );
    }
}