package pl.sylwestergladki.split_payment_api.expenses.api;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateExpenseRequest(

        @NotBlank
        @Size(max = 255)
        String description,

        @NotNull
        @DecimalMin(value = "0.00", inclusive = false)
        @Digits(integer = 17, fraction = 2)
        BigDecimal amount,

        @NotNull
        UUID paidBy,

        @NotEmpty
        List<@NotNull UUID> participants
) {
}