package pl.sylwestergladki.split_payment_api.expenses.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ExpenseId(UUID value) {

    public ExpenseId {
        Objects.requireNonNull(value, "Expense ID cannot be null");
    }

    public static ExpenseId generate() {
        return new ExpenseId(UUID.randomUUID());
    }
}