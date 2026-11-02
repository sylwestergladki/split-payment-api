package pl.sylwestergladki.split_payment_api.identity.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUser;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUserRepository;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncUserProfileUseCaseTest {

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private SyncUserProfileUseCase useCase;

    @Test
    void shouldRegisterAndSaveNewUser() {
        // given
        String subject = "keycloak-user-123";
        String email = "john@example.com";
        String displayName = "John";

        when(userRepository.findByKeycloakSubject(subject))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        AppUser result = useCase.execute(
                subject,
                email,
                displayName
        );

        // then
        assertThat(result.keycloakSubject()).isEqualTo(subject);
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.displayName()).isEqualTo(displayName);
        assertThat(result.id()).isNotNull();
        assertThat(result.createdAt()).isNotNull();

        verify(userRepository).findByKeycloakSubject(subject);
        verify(userRepository).save(result);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldUpdateExistingUserProfile() {
        // given
        String subject = "keycloak-user-123";

        AppUser existingUser = AppUser.register(
                subject,
                "old@example.com",
                "Old name"
        );

        when(userRepository.findByKeycloakSubject(subject))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        AppUser result = useCase.execute(
                subject,
                "new@example.com",
                "New name"
        );

        // then
        assertThat(result).isSameAs(existingUser);
        assertThat(result.email()).isEqualTo("new@example.com");
        assertThat(result.displayName()).isEqualTo("New name");

        verify(userRepository).findByKeycloakSubject(subject);
        verify(userRepository).save(existingUser);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldRejectBlankEmailWhenUpdatingExistingUser() {
        // given
        String subject = "keycloak-user-123";

        AppUser existingUser = AppUser.register(
                subject,
                "old@example.com",
                "Old name"
        );

        when(userRepository.findByKeycloakSubject(subject))
                .thenReturn(Optional.of(existingUser));

        // when / then
        assertThatThrownBy(() ->
                useCase.execute(subject, " ", "New name")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("email cannot be blank");

        verify(userRepository).findByKeycloakSubject(subject);
        verify(userRepository, never()).save(any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldPropagateRepositoryFailure() {
        // given
        String subject = "keycloak-user-123";

        when(userRepository.findByKeycloakSubject(subject))
                .thenThrow(new IllegalStateException("Database error"));

        // when / then
        assertThatThrownBy(() ->
                useCase.execute(
                        subject,
                        "john@example.com",
                        "John"
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Database error");

        verify(userRepository).findByKeycloakSubject(subject);
        verifyNoMoreInteractions(userRepository);
    }
}
