package pl.sylwestergladki.split_payment_api.groups.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.exception.GroupNotFoundException;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

@Service
public class GetGroupDetailsUseCase {

    private final GroupRepository groupRepository;

    public GetGroupDetailsUseCase(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Transactional(readOnly = true)
    public Group execute(GroupId groupId, UserId userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(GroupNotFoundException::new);

        if (!group.hasMember(userId)) {
            throw new GroupNotFoundException();
        }

        return group;
    }
}