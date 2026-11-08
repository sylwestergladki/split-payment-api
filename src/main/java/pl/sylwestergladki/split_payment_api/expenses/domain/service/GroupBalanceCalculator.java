package pl.sylwestergladki.split_payment_api.expenses.domain.service;

import pl.sylwestergladki.split_payment_api.expenses.domain.model.*;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import pl.sylwestergladki.split_payment_api.expenses.domain.model.Expense;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.ExpenseSplit;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Settlement;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.MemberBalance;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Balance;

public class GroupBalanceCalculator {

    public List<MemberBalance> calculate(
            List<UserId> members,
            List<Expense> expenses,
            List<Settlement> settlements
    ) {
        Map<UserId, BigDecimal> balances = new HashMap<>();

        for (UserId member : members) {
            balances.put(member, BigDecimal.ZERO);
        }

        for (Expense expense : expenses) {
            balances.merge(
                    expense.paidBy(),
                    expense.amount().amount(),
                    BigDecimal::add
            );

            for (ExpenseSplit split : expense.splits()) {
                balances.merge(
                        split.userId(),
                        split.amount().amount().negate(),
                        BigDecimal::add
                );
            }
        }


        for (Settlement settlement : settlements) {

            BigDecimal amount = settlement.amount().amount();

            balances.merge(
                    settlement.fromUserId(),
                    amount,
                    BigDecimal::add
            );

            balances.merge(
                    settlement.toUserId(),
                    amount.negate(),
                    BigDecimal::add
            );
        }

        return members.stream()
                .map(member -> new MemberBalance(
                        member,
                        new Balance(balances.get(member))
                ))
                .toList();
    }
}
