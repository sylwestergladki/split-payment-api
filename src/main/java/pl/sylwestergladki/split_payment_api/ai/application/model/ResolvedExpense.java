package pl.sylwestergladki.split_payment_api.ai.application.model;

import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.math.BigDecimal;
import java.util.List;

public record ResolvedExpense(
        String description,
        BigDecimal amount,
        String currency,
        UserId paidByUserId,
        List<UserId> participantUserIds
) {
    public ResolvedExpense {
        participantUserIds = List.copyOf(participantUserIds);
    }
}
