package pl.sylwestergladki.split_payment_api.groups.application;

import org.junit.jupiter.api.Test;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetUserGroupsUseCaseTest {

    private final GroupRepository groupRepository =
            mock(GroupRepository.class);

    private final GetUserGroupsUseCase useCase =
            new GetUserGroupsUseCase(groupRepository);

    @Test
    void shouldReturnGroupsForUser() {
        // given
        UserId userId = UserId.generate();

        Group group1 = Group.create("Trip to Spain", userId);
        Group group2 = Group.create("Weekend in Berlin", userId);

        when(groupRepository.findAllByMemberId(userId))
                .thenReturn(List.of(group1, group2));

        // when
        List<Group> result = useCase.execute(userId);

        // then
        assertThat(result)
                .containsExactly(group1, group2);

        verify(groupRepository).findAllByMemberId(userId);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoGroups() {
        // given
        UserId userId = UserId.generate();

        when(groupRepository.findAllByMemberId(userId))
                .thenReturn(List.of());

        // when
        List<Group> result = useCase.execute(userId);

        // then
        assertThat(result).isEmpty();

        verify(groupRepository).findAllByMemberId(userId);
    }
}