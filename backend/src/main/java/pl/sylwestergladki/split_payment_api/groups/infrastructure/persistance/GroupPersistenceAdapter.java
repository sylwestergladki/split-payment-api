package pl.sylwestergladki.split_payment_api.groups.infrastructure.persistance;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.groups.domain.model.GroupMember;
import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@Transactional
public class GroupPersistenceAdapter implements GroupRepository {

    private final SpringDataGroupRepository jpaRepository;

    public GroupPersistenceAdapter(SpringDataGroupRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Group save(Group group) {
        return jpaRepository.findById(group.id().value())
                .map(existingEntity -> {
                    syncMembers(existingEntity, group);
                    return toDomain(existingEntity);
                })
                .orElseGet(() -> {
                    GroupEntity saved = jpaRepository.save(toEntity(group));
                    return toDomain(saved);
                });
    }

    private void syncMembers(GroupEntity entity, Group group) {

        Set<UUID> existingMemberIds = entity.getMembers()
                .stream()
                .map(GroupMemberEntity::getUserId)
                .collect(Collectors.toSet());

        for (GroupMember member : group.members()) {

            UUID userId = member.userId().value();

            if (!existingMemberIds.contains(userId)) {

                GroupMemberEntity newMember = new GroupMemberEntity(
                        userId,
                        member.joinedAt()
                );

                entity.addMember(newMember);

                existingMemberIds.add(userId);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Group> findById(GroupId id) {
        return jpaRepository.findById(id.value())
                .map(this::toDomain);
    }

    @Override
    public List<Group> findAllByMemberId(UserId userId) {
        return jpaRepository
                .findDistinctByMembersUserId(userId.value())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private GroupEntity toEntity(Group group) {
        GroupEntity entity = new GroupEntity(
                group.id().value(),
                group.name(),
                group.createdBy().value(),
                group.createdAt()
        );

        group.members().forEach(member -> {
            GroupMemberEntity memberEntity = new GroupMemberEntity(
                    member.userId().value(),
                    member.joinedAt()
            );

            entity.addMember(memberEntity);
        });

        return entity;
    }

    private Group toDomain(GroupEntity entity) {
        var members = entity.getMembers().stream()
                .map(member -> new GroupMember(
                        new UserId(member.getUserId()),
                        member.getJoinedAt()
                ))
                .toList();

        return Group.reconstitute(
                new GroupId(entity.getId()),
                entity.getName(),
                new UserId(entity.getCreatedBy()),
                entity.getCreatedAt(),
                members
        );
    }
}