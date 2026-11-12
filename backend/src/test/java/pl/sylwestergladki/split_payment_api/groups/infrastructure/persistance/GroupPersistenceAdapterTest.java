package pl.sylwestergladki.split_payment_api.groups.infrastructure.persistance;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupPersistenceAdapterTest {

    @Mock
    private SpringDataGroupRepository jpaRepository;

    private GroupPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GroupPersistenceAdapter(jpaRepository);
    }

    @Test
    void shouldSaveNewGroup() {
        // given
        UserId creator = new UserId(UUID.randomUUID());
        Group group = Group.create("Mountain trip", creator);

        when(jpaRepository.findById(group.id().value()))
                .thenReturn(Optional.empty());

        when(jpaRepository.save(any(GroupEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Group saved = adapter.save(group);

        // then
        assertThat(saved.id()).isEqualTo(group.id());
        assertThat(saved.name()).isEqualTo("Mountain trip");
        assertThat(saved.hasMember(creator)).isTrue();
        assertThat(saved.members()).hasSize(1);

        verify(jpaRepository).save(any(GroupEntity.class));
    }

    @Test
    void shouldAddMissingMemberToExistingGroup() {
        // given
        UserId creator = new UserId(UUID.randomUUID());
        UserId secondUser = new UserId(UUID.randomUUID());

        Group group = Group.create("Mountain trip", creator);

        GroupEntity existingEntity = new GroupEntity(
                group.id().value(),
                group.name(),
                group.createdBy().value(),
                group.createdAt()
        );

        existingEntity.addMember(
                new GroupMemberEntity(
                        creator.value(),
                        group.members().iterator().next().joinedAt()
                )
        );

        when(jpaRepository.findById(group.id().value()))
                .thenReturn(Optional.of(existingEntity));

        group.addMember(secondUser);

        // when
        Group saved = adapter.save(group);

        // then
        assertThat(saved.members()).hasSize(2);
        assertThat(saved.hasMember(creator)).isTrue();
        assertThat(saved.hasMember(secondUser)).isTrue();

        assertThat(existingEntity.getMembers())
                .extracting(GroupMemberEntity::getUserId)
                .containsExactlyInAnyOrder(
                        creator.value(),
                        secondUser.value()
                );

        verify(jpaRepository, never()).save(any());
    }

    @Test
    void shouldNotDuplicateExistingMembers() {
        // given
        UserId creator = new UserId(UUID.randomUUID());

        Group group = Group.create("Mountain trip", creator);

        GroupEntity existingEntity = new GroupEntity(
                group.id().value(),
                group.name(),
                group.createdBy().value(),
                group.createdAt()
        );

        existingEntity.addMember(
                new GroupMemberEntity(
                        creator.value(),
                        group.members().iterator().next().joinedAt()
                )
        );

        when(jpaRepository.findById(group.id().value()))
                .thenReturn(Optional.of(existingEntity));

        // when
        adapter.save(group);
        adapter.save(group);

        // then
        assertThat(existingEntity.getMembers()).hasSize(1);

        assertThat(existingEntity.getMembers())
                .extracting(GroupMemberEntity::getUserId)
                .containsExactly(creator.value());

        verify(jpaRepository, never()).save(any());
    }

    @Test
    void shouldFindGroupById() {
        // given
        UserId creator = new UserId(UUID.randomUUID());
        Group group = Group.create("Mountain trip", creator);

        GroupEntity entity = new GroupEntity(
                group.id().value(),
                group.name(),
                group.createdBy().value(),
                group.createdAt()
        );

        entity.addMember(
                new GroupMemberEntity(
                        creator.value(),
                        group.members().iterator().next().joinedAt()
                )
        );

        when(jpaRepository.findById(group.id().value()))
                .thenReturn(Optional.of(entity));

        // when
        Optional<Group> found = adapter.findById(group.id());

        // then
        assertThat(found).isPresent();
        assertThat(found.orElseThrow().id()).isEqualTo(group.id());
        assertThat(found.orElseThrow().hasMember(creator)).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenGroupDoesNotExist() {
        // given
        GroupId groupId = new GroupId(UUID.randomUUID());

        when(jpaRepository.findById(groupId.value()))
                .thenReturn(Optional.empty());

        // when
        Optional<Group> found = adapter.findById(groupId);

        // then
        assertThat(found).isEmpty();
    }
}