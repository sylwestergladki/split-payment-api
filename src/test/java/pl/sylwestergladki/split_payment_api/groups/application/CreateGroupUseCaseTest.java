package pl.sylwestergladki.split_payment_api.groups.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateGroupUseCaseTest {

    private GroupRepository groupRepository;
    private CreateGroupUseCase useCase;

    @BeforeEach
    void setUp() {
        groupRepository = mock(GroupRepository.class);
        useCase = new CreateGroupUseCase(groupRepository);

        when(groupRepository.save(any(Group.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldCreateGroupWithCreatorAsFirstMember() {
        UserId creator = new UserId(UUID.randomUUID());

        Group result = useCase.execute("Mountain trip", creator);

        assertThat(result.name()).isEqualTo("Mountain trip");
        assertThat(result.createdBy()).isEqualTo(creator);
        assertThat(result.members()).hasSize(1);
        assertThat(result.hasMember(creator)).isTrue();

        verify(groupRepository).save(any(Group.class));
    }
}