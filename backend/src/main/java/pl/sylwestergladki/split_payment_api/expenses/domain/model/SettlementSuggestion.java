package pl.sylwestergladki.split_payment_api.expenses.domain.model;

import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

public record SettlementSuggestion(
        UserId fromUserId,
        UserId toUserId,
        Money amount
) {
    public SettlementSuggestion {
        if (fromUserId == null || toUserId == null || amount == null) {
            throw new IllegalArgumentException(
                    "Settlement fields cannot be null"
            );
        }

        if (fromUserId.equals(toUserId)) {
            throw new IllegalArgumentException(
                    "Cannot settle with yourself"
            );
        }

        if (amount.amount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Settlement amount must be positive"
            );
        }
    }
}