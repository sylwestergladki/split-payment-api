package pl.sylwestergladki.split_payment_api.expenses.infrastructure.persistance;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataExpenseRepository
        extends JpaRepository<ExpenseEntity, UUID> {

    @EntityGraph(attributePaths = "splits")
    List<ExpenseEntity> findAllByGroupIdOrderByCreatedAtDesc(UUID groupId);
}