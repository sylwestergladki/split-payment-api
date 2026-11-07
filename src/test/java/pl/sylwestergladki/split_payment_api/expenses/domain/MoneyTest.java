package pl.sylwestergladki.split_payment_api.expenses.domain;


import org.junit.jupiter.api.Test;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithTwoDecimalPlaces() {
        Money money = Money.of("12.5");

        assertEquals(new BigDecimal("12.50"), money.amount());
    }

    @Test
    void shouldRejectNegativeAmount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Money.of("-5.00")
        );
    }

    @Test
    void shouldRejectAmountRequiringRounding() {
        assertThrows(
                ArithmeticException.class,
                () -> Money.of("12.345")
        );
    }

    @Test
    void shouldAllowZeroAmount() {
        Money money = Money.of("0");

        assertEquals(new BigDecimal("0.00"), money.amount());
    }
}