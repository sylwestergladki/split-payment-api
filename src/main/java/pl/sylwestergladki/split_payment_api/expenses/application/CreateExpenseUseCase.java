package pl.sylwestergladki.split_payment_api.expenses.application;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Expense;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.expenses.domain.repository.ExpenseRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;

import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.exception.GroupNotFoundException;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

@Service
public class CreateExpenseUseCase {

    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;

    public CreateExpenseUseCase(
            ExpenseRepository expenseRepository,
            GroupRepository groupRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional
    public Expense execute(
            GroupId groupId,
            String description,
            Money amount,
            UserId paidBy,
            List<UserId> participants,
            UserId requestedBy
    ) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(GroupNotFoundException::new);

        if (!group.hasMember(requestedBy)) {
            throw new GroupNotFoundException();
        }

        if (!group.hasMember(paidBy)) {
            throw new IllegalArgumentException(
                    "Payer must be a group member"
            );
        }

        if (participants == null || participants.stream()
                .anyMatch(userId -> userId == null || !group.hasMember(userId))) {
            throw new IllegalArgumentException(
                    "All participants must be group members"
            );
        }

        Expense expense = Expense.create(
                groupId,
                description,
                amount,
                paidBy,
                participants
        );

        return expenseRepository.save(expense);
    }
}