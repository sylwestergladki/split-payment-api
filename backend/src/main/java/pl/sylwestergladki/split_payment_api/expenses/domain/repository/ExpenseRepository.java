package pl.sylwestergladki.split_payment_api.expenses.domain.repository;

import pl.sylwestergladki.split_payment_api.expenses.domain.model.Expense;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;

import java.util.List;

public interface ExpenseRepository {

    Expense save(Expense expense);

    List<Expense> findAllByGroupId(GroupId groupId);
}
