package pl.sylwestergladki.split_payment_api.groups.application;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.exception.GroupNotFoundException;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUserRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

@Service
public class AddGroupMemberUseCase {

    private final GroupRepository groupRepository;
    private final AppUserRepository userRepository;

    public AddGroupMemberUseCase(GroupRepository groupRepository, AppUserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void execute(
            GroupId groupId,
            UserId newMemberId,
            UserId requestedBy
    ) {
        var group = groupRepository.findById(groupId)
                .orElseThrow(GroupNotFoundException::new);

        if (!group.hasMember(requestedBy)) {
            throw new GroupNotFoundException();
        }

        if (!userRepository.existsById(newMemberId)) {
            throw new IllegalArgumentException("User does not exist");
        }

        group.addMember(newMemberId);

        groupRepository.save(group);
    }
}