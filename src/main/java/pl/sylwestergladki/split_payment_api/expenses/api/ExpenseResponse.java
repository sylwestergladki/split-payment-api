package pl.sylwestergladki.split_payment_api.expenses.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        UUID groupId,
        String description,
        BigDecimal amount,
        UUID paidBy,
        List<ExpenseSplitResponse> splits,
        Instant createdAt
) {

    public record ExpenseSplitResponse(
            UUID userId,
            BigDecimal amount
    ) {
    }
}