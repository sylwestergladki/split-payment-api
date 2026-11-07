package pl.sylwestergladki.split_payment_api.expenses.domain.model;

import org.junit.jupiter.api.Test;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseTest {

    private final GroupId groupId = GroupId.generate();

    private UserId user() {
        return new UserId(UUID.randomUUID());
    }

    @Test
    void shouldCreateExpenseWithEqualSplits() {
        UserId first = user();
        UserId second = user();
        UserId third = user();

        Expense expense = Expense.create(
                groupId,
                "Dinner",
                Money.of("100.00"),
                first,
                List.of(first, second, third)
        );

        assertNotNull(expense.id());
        assertEquals("Dinner", expense.description());
        assertEquals(first, expense.paidBy());
        assertEquals(3, expense.splits().size());

        BigDecimal total = expense.splits().stream()
                .map(split -> split.amount().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(0, new BigDecimal("100.00").compareTo(total));

        long sharesOf3334 = expense.splits().stream()
                .filter(split -> split.amount().equals(Money.of("33.34")))
                .count();

        assertEquals(1, sharesOf3334);
    }

    @Test
    void shouldRejectPayerWhoIsNotParticipant() {
        UserId payer = user();
        UserId participant = user();

        assertThrows(
                IllegalArgumentException.class,
                () -> Expense.create(
                        groupId,
                        "Dinner",
                        Money.of("100.00"),
                        payer,
                        List.of(participant)
                )
        );
    }

    @Test
    void shouldRejectDuplicateParticipants() {
        UserId participant = user();

        assertThrows(
                IllegalArgumentException.class,
                () -> Expense.create(
                        groupId,
                        "Dinner",
                        Money.of("100.00"),
                        participant,
                        List.of(participant, participant)
                )
        );
    }

    @Test
    void shouldRejectZeroAmount() {
        UserId participant = user();

        assertThrows(
                IllegalArgumentException.class,
                () -> Expense.create(
                        groupId,
                        "Dinner",
                        Money.of("0.00"),
                        participant,
                        List.of(participant)
                )
        );
    }

    @Test
    void shouldRejectEmptyParticipants() {
        UserId payer = user();

        assertThrows(
                IllegalArgumentException.class,
                () -> Expense.create(
                        groupId,
                        "Dinner",
                        Money.of("10.00"),
                        payer,
                        List.of()
                )
        );
    }

    @Test
    void shouldSplitOneCentBetweenThreeParticipants() {
        UserId first = user();
        UserId second = user();
        UserId third = user();

        Expense expense = Expense.create(
                groupId,
                "Tiny expense",
                Money.of("0.01"),
                first,
                List.of(first, second, third)
        );

        long positiveShares = expense.splits().stream()
                .filter(split -> split.amount().amount().signum() > 0)
                .count();

        assertEquals(1, positiveShares);
    }

    @Test
    void shouldProduceSameSplitsRegardlessOfParticipantOrder() {
        UserId first = user();
        UserId second = user();
        UserId third = user();

        Expense firstExpense = Expense.create(
                groupId,
                "Dinner",
                Money.of("100.00"),
                first,
                List.of(first, second, third)
        );

        Expense secondExpense = Expense.create(
                groupId,
                "Dinner",
                Money.of("100.00"),
                first,
                List.of(third, first, second)
        );

        assertEquals(firstExpense.splits(), secondExpense.splits());
    }
}