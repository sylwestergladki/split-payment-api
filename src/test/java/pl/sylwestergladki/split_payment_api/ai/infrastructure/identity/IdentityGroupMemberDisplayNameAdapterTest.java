package pl.sylwestergladki.split_payment_api.ai.infrastructure.identity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsingGroupMember;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUser;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUserRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityGroupMemberDisplayNameAdapterTest {

    @Mock
    private AppUserRepository appUserRepository;

    private IdentityGroupMemberDisplayNameAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new IdentityGroupMemberDisplayNameAdapter(
                appUserRepository
        );
    }

    @Test
    void shouldReturnGroupMembersWithDisplayNames() {
        // given
        var john = AppUser.register(
                "keycloak-john",
                "john@example.com",
                "John"
        );

        var anna = AppUser.register(
                "keycloak-anna",
                "anna@example.com",
                "Anna"
        );

        var userIds = List.of(john.id(), anna.id());

        when(appUserRepository.findAllByIds(userIds))
                .thenReturn(List.of(john, anna));

        // when
        var result = adapter.findMembers(userIds);

        // then
        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(ParsingGroupMember::displayName)
                .containsExactly("John", "Anna");

        assertThat(result)
                .extracting(ParsingGroupMember::userId)
                .containsExactly(john.id(), anna.id());

        verify(appUserRepository).findAllByIds(userIds);
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersFound() {
        // given
        List<UserId> userIds = List.of(UserId.generate());

        when(appUserRepository.findAllByIds(userIds))
                .thenReturn(List.of());

        // when
        var result = adapter.findMembers(userIds);

        // then
        assertThat(result).isEmpty();
    }
}
