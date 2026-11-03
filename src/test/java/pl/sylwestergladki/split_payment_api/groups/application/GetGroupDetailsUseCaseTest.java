package pl.sylwestergladki.split_payment_api.groups.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.exception.GroupNotFoundException;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetGroupDetailsUseCaseTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GetGroupDetailsUseCase useCase;

    @Test
    void shouldReturnGroupWhenUserIsMember() {
        // given
        UserId userId = UserId.generate();
        Group group = Group.create("Trip to Spain", userId);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        // when
        Group result = useCase.execute(group.id(), userId);

        // then
        assertThat(result).isSameAs(group);

        verify(groupRepository).findById(group.id());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotMember() {
        // given
        UserId creatorId = UserId.generate();
        UserId otherUserId = UserId.generate();

        Group group = Group.create("Trip to Spain", creatorId);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        // when / then
        assertThatThrownBy(() ->
                useCase.execute(group.id(), otherUserId)
        ).isInstanceOf(GroupNotFoundException.class);

        verify(groupRepository).findById(group.id());
    }

    @Test
    void shouldThrowExceptionWhenGroupDoesNotExist() {
        // given
        GroupId groupId = GroupId.generate();
        UserId userId = UserId.generate();

        when(groupRepository.findById(groupId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                useCase.execute(groupId, userId)
        ).isInstanceOf(GroupNotFoundException.class);

        verify(groupRepository).findById(groupId);
    }
}