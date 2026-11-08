package pl.sylwestergladki.split_payment_api.expenses.api;

import java.math.BigDecimal;
import java.util.UUID;

public record MemberBalanceResponse(
        UUID userId,
        BigDecimal balance
) {
}