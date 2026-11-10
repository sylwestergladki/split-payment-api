package pl.sylwestergladki.split_payment_api.ai.application.model;

import java.math.BigDecimal;
import java.util.List;

public record ParsedExpense(
        String description,
        BigDecimal amount,
        String currency,
        String paidBy,
        List<String> participants
) {
}
