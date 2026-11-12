package pl.sylwestergladki.split_payment_api.groups.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUser;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUserRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

@Service
public class FindUserIdByKeycloakSubjectUseCase {

    private final AppUserRepository userRepository;

    public FindUserIdByKeycloakSubjectUseCase(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserId execute(String keycloakSubject) {
        return userRepository.findByKeycloakSubject(keycloakSubject)
                .map(AppUser::id)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user profile does not exist"
                ));
    }
}