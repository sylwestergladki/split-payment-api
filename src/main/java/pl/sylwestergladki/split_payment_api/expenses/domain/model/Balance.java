package pl.sylwestergladki.split_payment_api.expenses.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Balance(BigDecimal amount) {

    public Balance {
        Objects.requireNonNull(amount, "Balance amount cannot be null");

        amount = amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static Balance zero() {
        return new Balance(BigDecimal.ZERO);
    }

    public Balance add(Money money) {
        return new Balance(amount.add(money.amount()));
    }

    public Balance subtract(Money money) {
        return new Balance(amount.subtract(money.amount()));
    }
}