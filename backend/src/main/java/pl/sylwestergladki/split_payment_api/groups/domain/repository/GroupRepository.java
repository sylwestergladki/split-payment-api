package pl.sylwestergladki.split_payment_api.groups.domain.repository;

import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface GroupRepository {

    Group save(Group group);

    Optional<Group> findById(GroupId id);

    List<Group> findAllByMemberId(UserId userId);
}