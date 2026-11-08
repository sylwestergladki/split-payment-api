package pl.sylwestergladki.split_payment_api.expenses.api;

import java.math.BigDecimal;
import java.util.UUID;

public record SettlementSuggestionResponse(
        UUID fromUserId,
        UUID toUserId,
        BigDecimal amount
) {
}