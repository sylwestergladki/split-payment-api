package pl.sylwestergladki.split_payment_api.expenses.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.sylwestergladki.split_payment_api.expenses.domain.model.Expense;
import pl.sylwestergladki.split_payment_api.expenses.domain.repository.ExpenseRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.exception.GroupNotFoundException;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

@Service
public class GetGroupExpensesUseCase {

    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;

    public GetGroupExpensesUseCase(
            ExpenseRepository expenseRepository,
            GroupRepository groupRepository
    ) {
        this.expenseRepository = expenseRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional(readOnly = true)
    public List<Expense> execute(GroupId groupId, UserId requestedBy) {
        var group = groupRepository.findById(groupId)
                .orElseThrow(GroupNotFoundException::new);

        if (!group.hasMember(requestedBy)) {
            throw new GroupNotFoundException();
        }

        return expenseRepository.findAllByGroupId(groupId);
    }
}