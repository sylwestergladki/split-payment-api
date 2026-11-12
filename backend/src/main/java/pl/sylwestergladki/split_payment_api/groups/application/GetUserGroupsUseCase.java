package pl.sylwestergladki.split_payment_api.groups.application;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

@Service
public class GetUserGroupsUseCase {

    private final GroupRepository groupRepository;

    public GetUserGroupsUseCase(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Transactional(readOnly = true)
    public List<Group> execute(UserId userId) {
        return groupRepository.findAllByMemberId(userId);
    }
}