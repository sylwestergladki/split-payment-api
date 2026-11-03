package pl.sylwestergladki.split_payment_api.groups.infrastructure.persistance;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupMember;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class GroupPersistenceAdapter implements GroupRepository {

    private final SpringDataGroupRepository jpaRepository;

    public GroupPersistenceAdapter(SpringDataGroupRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Group save(Group group) {
        GroupEntity entity = toEntity(group);

        GroupEntity saved = jpaRepository.save(entity);

        return toDomain(saved);
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