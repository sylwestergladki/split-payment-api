package pl.sylwestergladki.split_payment_api.groups.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataGroupRepository
        extends JpaRepository<GroupEntity, UUID> {

    List<GroupEntity> findDistinctByMembersUserId(UUID userId);
}