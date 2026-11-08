package pl.sylwestergladki.split_payment_api.expenses.domain.model;

import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.Objects;

public record MemberBalance(
        UserId userId,
        Balance balance
) {
    public MemberBalance {
        Objects.requireNonNull(userId, "UserId cannot be null");
        Objects.requireNonNull(balance, "Balance cannot be null");
    }
}