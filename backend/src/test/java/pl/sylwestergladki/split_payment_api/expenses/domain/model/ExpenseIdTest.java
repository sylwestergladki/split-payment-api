package pl.sylwestergladki.split_payment_api.expenses.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseIdTest {

    @Test
    void shouldGenerateExpenseId() {
        ExpenseId expenseId = ExpenseId.generate();

        assertNotNull(expenseId.value());
    }

    @Test
    void shouldGenerateUniqueExpenseIds() {
        ExpenseId first = ExpenseId.generate();
        ExpenseId second = ExpenseId.generate();

        assertNotEquals(first, second);
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(
                NullPointerException.class,
                () -> new ExpenseId(null)
        );
    }

    @Test
    void shouldCreateExpenseIdFromExistingUuid() {
        UUID uuid = UUID.randomUUID();

        ExpenseId expenseId = new ExpenseId(uuid);

        assertEquals(uuid, expenseId.value());
    }
}