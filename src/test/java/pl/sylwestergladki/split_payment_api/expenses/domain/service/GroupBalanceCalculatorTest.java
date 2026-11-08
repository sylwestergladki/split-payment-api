package pl.sylwestergladki.split_payment_api.expenses.domain.service;

import org.junit.jupiter.api.Test;

import pl.sylwestergladki.split_payment_api.expenses.domain.model.Expense;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.MemberBalance;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


import static org.assertj.core.api.Assertions.assertThat;


import org.junit.jupiter.api.BeforeEach;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Settlement;



class GroupBalanceCalculatorTest {

    private GroupBalanceCalculator calculator;

    private GroupId groupId;
    private UserId anna;
    private UserId john;

    @BeforeEach
    void setUp() {
        calculator = new GroupBalanceCalculator();

        groupId = new GroupId(UUID.randomUUID());
        anna = new UserId(UUID.randomUUID());
        john = new UserId(UUID.randomUUID());
    }

    @Test
    void shouldReturnZeroBalancesWhenThereAreNoExpensesOrSettlements() {
        List<MemberBalance> result = calculator.calculate(
                List.of(anna, john),
                List.of(),
                List.of()
        );

        assertBalance(result, anna, "0.00");
        assertBalance(result, john, "0.00");
    }

    @Test
    void shouldCalculateBalancesFromExpense() {
        Expense expense = Expense.create(
                groupId,
                "Dinner",
                Money.of("100.00"),
                anna,
                List.of(anna, john)
        );

        List<MemberBalance> result = calculator.calculate(
                List.of(anna, john),
                List.of(expense),
                List.of()
        );

        assertBalance(result, anna, "50.00");
        assertBalance(result, john, "-50.00");
    }

    @Test
    void shouldReduceDebtAfterPartialSettlement() {
        // Anna płaci 100 zł, john jest jej winien 50 zł.
        Expense expense = Expense.create(
                groupId,
                "Dinner",
                Money.of("100.00"),
                anna,
                List.of(anna, john)
        );

        // john oddaje Annie 30 zł.
        Settlement settlement = Settlement.create(
                groupId,
                john,
                anna,
                Money.of("30.00")
        );

        List<MemberBalance> result = calculator.calculate(
                List.of(anna, john),
                List.of(expense),
                List.of(settlement)
        );

        assertBalance(result, anna, "20.00");
        assertBalance(result, john, "-20.00");
    }

    @Test
    void shouldReturnZeroBalancesAfterFullSettlement() {
        Expense expense = Expense.create(
                groupId,
                "Dinner",
                Money.of("100.00"),
                anna,
                List.of(anna, john)
        );

        Settlement settlement = Settlement.create(
                groupId,
                john,
                anna,
                Money.of("50.00")
        );

        List<MemberBalance> result = calculator.calculate(
                List.of(anna, john),
                List.of(expense),
                List.of(settlement)
        );

        assertBalance(result, anna, "0.00");
        assertBalance(result, john, "0.00");
    }

    @Test
    void shouldIncreaseDebtWhenSettlementGoesInOppositeDirection() {
        Expense expense = Expense.create(
                groupId,
                "Dinner",
                Money.of("100.00"),
                anna,
                List.of(anna, john)
        );

        // Anna wysyła Bartkowi 30 zł, mimo że john jest jej winien.
        Settlement settlement = Settlement.create(
                groupId,
                anna,
                john,
                Money.of("30.00")
        );

        List<MemberBalance> result = calculator.calculate(
                List.of(anna, john),
                List.of(expense),
                List.of(settlement)
        );

        assertBalance(result, anna, "80.00");
        assertBalance(result, john, "-80.00");
    }

    @Test
    void shouldAccumulateMultipleSettlements() {
        Expense expense = Expense.create(
                groupId,
                "Dinner",
                Money.of("100.00"),
                anna,
                List.of(anna, john)
        );

        Settlement firstSettlement = Settlement.create(
                groupId,
                john,
                anna,
                Money.of("10.00")
        );

        Settlement secondSettlement = Settlement.create(
                groupId,
                john,
                anna,
                Money.of("15.00")
        );

        List<MemberBalance> result = calculator.calculate(
                List.of(anna, john),
                List.of(expense),
                List.of(firstSettlement, secondSettlement)
        );

        assertBalance(result, anna, "25.00");
        assertBalance(result, john, "-25.00");
    }

    private void assertBalance(
            List<MemberBalance> balances,
            UserId userId,
            String expectedAmount
    ) {
        MemberBalance memberBalance = balances.stream()
                .filter(balance -> balance.userId().equals(userId))
                .findFirst()
                .orElseThrow();

        assertThat(memberBalance.balance().amount())
                .isEqualByComparingTo(new BigDecimal(expectedAmount));
    }
}
