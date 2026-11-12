package pl.sylwestergladki.split_payment_api.expenses.infrastructure.persistance;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Settlement;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.SettlementId;
import pl.sylwestergladki.split_payment_api.expenses.domain.repository.SettlementRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

@Repository
public class SettlementPersistenceAdapter implements SettlementRepository {

    private final SpringDataSettlementRepository jpaRepository;

    public SettlementPersistenceAdapter(
            SpringDataSettlementRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Settlement save(Settlement settlement) {
        SettlementEntity entity = toEntity(settlement);

        SettlementEntity saved = jpaRepository.save(entity);

        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Settlement> findAllByGroupId(GroupId groupId) {
        return jpaRepository.findAllByGroupId(groupId.value())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private SettlementEntity toEntity(Settlement settlement) {
        return new SettlementEntity(
                settlement.id().value(),
                settlement.groupId().value(),
                settlement.fromUserId().value(),
                settlement.toUserId().value(),
                settlement.amount().amount(),
                settlement.createdAt()
        );
    }

    private Settlement toDomain(SettlementEntity entity) {
        return Settlement.reconstitute(
                new SettlementId(entity.getId()),
                new GroupId(entity.getGroupId()),
                new UserId(entity.getFromUserId()),
                new UserId(entity.getToUserId()),
                Money.of(entity.getAmount().toPlainString()),
                entity.getCreatedAt()
        );
    }
}