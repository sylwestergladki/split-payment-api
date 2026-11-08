package pl.sylwestergladki.split_payment_api.expenses.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.sylwestergladki.split_payment_api.expenses.domain.model.Settlement;
import pl.sylwestergladki.split_payment_api.expenses.domain.repository.SettlementRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.groups.domain.exception.GroupNotFoundException;
import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

@Service
public class GetGroupSettlementsUseCase {

    private final GroupRepository groupRepository;
    private final SettlementRepository settlementRepository;

    public GetGroupSettlementsUseCase(
            GroupRepository groupRepository,
            SettlementRepository settlementRepository
    ) {
        this.groupRepository = groupRepository;
        this.settlementRepository = settlementRepository;
    }

    @Transactional(readOnly = true)
    public List<Settlement> execute(
            GroupId groupId,
            UserId requestedBy
    ) {
        var group = groupRepository.findById(groupId)
                .orElseThrow(GroupNotFoundException::new);

        if (!group.hasMember(requestedBy)) {
            throw new GroupNotFoundException();
        }

        return settlementRepository.findAllByGroupId(groupId)
                .stream()
                .sorted((first, second) -> {
                    int byDate = second.createdAt()
                            .compareTo(first.createdAt());

                    if (byDate != 0) {
                        return byDate;
                    }

                    return first.id().value()
                            .compareTo(second.id().value());
                })
                .toList();
    }
}
