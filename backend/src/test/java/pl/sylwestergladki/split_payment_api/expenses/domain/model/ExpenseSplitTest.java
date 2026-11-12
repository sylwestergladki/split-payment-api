package pl.sylwestergladki.split_payment_api.expenses.domain.model;

import org.junit.jupiter.api.Test;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseSplitTest {

    @Test
    void shouldCreateExpenseSplit() {
        UserId userId = new UserId(UUID.randomUUID());
        Money amount = Money.of("33.34");

        ExpenseSplit split = new ExpenseSplit(userId, amount);

        assertEquals(userId, split.userId());
        assertEquals(amount, split.amount());
    }

    @Test
    void shouldRejectNullUserId() {
        assertThrows(
                NullPointerException.class,
                () -> new ExpenseSplit(null, Money.of("10.00"))
        );
    }

    @Test
    void shouldRejectNullAmount() {
        UserId userId = new UserId(UUID.randomUUID());

        assertThrows(
                NullPointerException.class,
                () -> new ExpenseSplit(userId, null)
        );
    }
}