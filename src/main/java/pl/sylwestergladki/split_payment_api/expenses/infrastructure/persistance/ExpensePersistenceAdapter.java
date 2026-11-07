package pl.sylwestergladki.split_payment_api.expenses.infrastructure.persistance;

import org.springframework.stereotype.Repository;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Expense;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.ExpenseId;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.ExpenseSplit;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.expenses.domain.repository.ExpenseRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

@Repository
public class ExpensePersistenceAdapter implements ExpenseRepository {

    private final SpringDataExpenseRepository repository;

    public ExpensePersistenceAdapter(
            SpringDataExpenseRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Expense save(Expense expense) {
        ExpenseEntity entity = toEntity(expense);

        ExpenseEntity savedEntity = repository.save(entity);

        return toDomain(savedEntity);
    }

    @Override
    public List<Expense> findAllByGroupId(GroupId groupId) {
        return repository.findAllByGroupIdOrderByCreatedAtDesc(groupId.value())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private ExpenseEntity toEntity(Expense expense) {
        ExpenseEntity entity = new ExpenseEntity(
                expense.id().value(),
                expense.groupId().value(),
                expense.description(),
                expense.amount().amount(),
                expense.paidBy().value(),
                expense.createdAt()
        );

        for (ExpenseSplit split : expense.splits()) {
            ExpenseSplitEntity splitEntity = new ExpenseSplitEntity(
                    split.userId().value(),
                    split.amount().amount()
            );

            entity.addSplit(splitEntity);
        }

        return entity;
    }

    private Expense toDomain(ExpenseEntity entity) {
        return Expense.reconstitute(
                new ExpenseId(entity.getId()),
                new GroupId(entity.getGroupId()),
                entity.getDescription(),
                new Money(entity.getAmount()),
                new UserId(entity.getPaidBy()),
                entity.getSplits().stream()
                        .map(split -> new ExpenseSplit(
                                new UserId(split.getUserId()),
                                new Money(split.getAmount())
                        ))
                        .toList(),
                entity.getCreatedAt()
        );
    }
}