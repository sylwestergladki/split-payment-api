package pl.sylwestergladki.split_payment_api.ai.api;

import pl.sylwestergladki.split_payment_api.ai.application.model.ResolvedExpense;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ParseExpenseResponse(
        String description,
        BigDecimal amount,
        String currency,
        UUID paidByUserId,
        List<UUID> participantUserIds
) {

    public static ParseExpenseResponse fromResolvedExpense(
            ResolvedExpense expense
    ) {
        return new ParseExpenseResponse(
                expense.description(),
                expense.amount(),
                expense.currency(),
                expense.paidByUserId().value(),
                expense.participantUserIds().stream()
                        .map(UserId::value)
                        .toList()
        );
    }
}