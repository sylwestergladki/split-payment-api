package pl.sylwestergladki.split_payment_api.groups.application;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

@Service
public class CreateGroupUseCase {

    private final GroupRepository groupRepository;

    public CreateGroupUseCase(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Transactional
    public Group execute(String name, UserId createdBy) {
        Group group = Group.create(name, createdBy);

        return groupRepository.save(group);
    }
}