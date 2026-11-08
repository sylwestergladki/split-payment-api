package pl.sylwestergladki.split_payment_api.expenses.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Settlement;
import pl.sylwestergladki.split_payment_api.expenses.domain.repository.SettlementRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.groups.domain.exception.GroupNotFoundException;
import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

@Service
public class RecordSettlementUseCase {

    private final GroupRepository groupRepository;
    private final SettlementRepository settlementRepository;

    public RecordSettlementUseCase(
            GroupRepository groupRepository,
            SettlementRepository settlementRepository
    ) {
        this.groupRepository = groupRepository;
        this.settlementRepository = settlementRepository;
    }

    @Transactional
    public Settlement execute(
            GroupId groupId,
            UserId fromUserId,
            UserId toUserId,
            Money amount,
            UserId requestedBy
    ) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(GroupNotFoundException::new);

        if (!group.hasMember(requestedBy)) {
            throw new GroupNotFoundException();
        }

        if (!group.hasMember(fromUserId)
                || !group.hasMember(toUserId)) {
            throw new IllegalArgumentException(
                    "Both settlement participants must belong to the group"
            );
        }

        if (!requestedBy.equals(fromUserId)) {
            throw new IllegalArgumentException(
                    "Only the sender can record a settlement"
            );
        }

        Settlement settlement = Settlement.create(
                groupId,
                fromUserId,
                toUserId,
                amount
        );

        return settlementRepository.save(settlement);
    }
}