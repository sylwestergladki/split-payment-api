package pl.sylwestergladki.split_payment_api.expenses.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataSettlementRepository
        extends JpaRepository<SettlementEntity, UUID> {

        List<SettlementEntity> findAllByGroupId(UUID groupId);
}