package pl.sylwestergladki.split_payment_api.ai.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParseExpenseRequest(
        @NotBlank
        @Size(max = 1000)
        String text
) {
}