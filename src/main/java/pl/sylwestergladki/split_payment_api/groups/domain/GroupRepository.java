package pl.sylwestergladki.split_payment_api.groups.domain;

import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface GroupRepository {

    Group save(Group group);

    Optional<Group> findById(GroupId id);

    List<Group> findAllByMemberId(UserId userId);
}