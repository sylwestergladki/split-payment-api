package pl.sylwestergladki.split_payment_api.expenses.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record RecordSettlementRequest(
        @NotNull UUID toUserId,

        @NotNull
        @DecimalMin(value = "0.00", inclusive = false)
        BigDecimal amount
) {
}