package pl.sylwestergladki.split_payment_api.groups.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.exception.GroupNotFoundException;
import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUserRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddGroupMemberUseCaseTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private AddGroupMemberUseCase useCase;

    @Test
    void shouldAddMemberToGroup() {
        UserId creator = new UserId(UUID.randomUUID());
        UserId newMember = new UserId(UUID.randomUUID());

        Group group = Group.create("Vacation", creator);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        when(userRepository.existsById(newMember))
                .thenReturn(true);

        useCase.execute(group.id(), newMember, creator);

        assertThat(group.hasMember(newMember)).isTrue();

        verify(groupRepository).save(group);
    }

    @Test
    void shouldRejectUserWhoIsNotGroupMember() {
        UserId creator = new UserId(UUID.randomUUID());
        UserId outsider = new UserId(UUID.randomUUID());
        UserId newMember = new UserId(UUID.randomUUID());

        Group group = Group.create("Vacation", creator);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        assertThatThrownBy(() ->
                useCase.execute(group.id(), newMember, outsider)
        ).isInstanceOf(GroupNotFoundException.class);

        verifyNoInteractions(userRepository);
        verify(groupRepository, never()).save(any());
    }

    @Test
    void shouldRejectNonexistentUser() {
        UserId creator = new UserId(UUID.randomUUID());
        UserId nonexistentUser = new UserId(UUID.randomUUID());

        Group group = Group.create("Vacation", creator);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        when(userRepository.existsById(nonexistentUser))
                .thenReturn(false);

        assertThatThrownBy(() ->
                useCase.execute(group.id(), nonexistentUser, creator)
        ).isInstanceOf(IllegalArgumentException.class);

        verify(groupRepository, never()).save(any());
    }
}