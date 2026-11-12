package pl.sylwestergladki.split_payment_api.expenses.domain.model;


import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.Objects;

public record ExpenseSplit(
        UserId userId,
        Money amount
) {

    public ExpenseSplit {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(amount, "Split amount cannot be null");
    }
}
