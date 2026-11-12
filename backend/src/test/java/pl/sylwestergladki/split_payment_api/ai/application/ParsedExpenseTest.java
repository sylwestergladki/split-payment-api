package pl.sylwestergladki.split_payment_api.ai.application;

import org.junit.jupiter.api.Test;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsedExpense;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParsedExpenseTest {

    @Test
    void shouldHoldParsedExpenseData() {
        // given
        var parsedExpense = new ParsedExpense(
                "Dinner",
                new BigDecimal("180.00"),
                "PLN",
                "me",
                List.of("me", "Anna", "John")
        );

        // then
        assertThat(parsedExpense.description())
                .isEqualTo("Dinner");

        assertThat(parsedExpense.amount())
                .isEqualByComparingTo("180.00");

        assertThat(parsedExpense.currency())
                .isEqualTo("PLN");

        assertThat(parsedExpense.paidBy())
                .isEqualTo("me");

        assertThat(parsedExpense.participants())
                .containsExactly("me", "Anna", "John");
    }
}
