package pl.sylwestergladki.split_payment_api.expenses.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SettlementResponse(
        UUID id,
        UUID groupId,
        UUID fromUserId,
        UUID toUserId,
        BigDecimal amount,
        Instant createdAt
) {
}
