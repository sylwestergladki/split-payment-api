package pl.sylwestergladki.split_payment_api.expenses.domain.service;

import pl.sylwestergladki.split_payment_api.expenses.domain.model.MemberBalance;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.SettlementSuggestion;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SettlementSuggestionCalculator {

    public List<SettlementSuggestion> calculate(
            List<MemberBalance> balances
    ) {
        BigDecimal total = balances.stream()
                .map(balance -> balance.balance().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.signum() != 0) {
            throw new IllegalArgumentException(
                    "Balances must sum to zero"
            );
        }

        List<RemainingBalance> debtors = balances.stream()
                .filter(balance -> balance.balance().amount().signum() < 0)
                .map(balance -> new RemainingBalance(
                        balance.userId(),
                        balance.balance().amount().negate()
                ))
                .sorted(Comparator.comparing(
                        balance -> balance.userId().value()
                ))
                .toList();

        List<RemainingBalance> creditors = balances.stream()
                .filter(balance -> balance.balance().amount().signum() > 0)
                .map(balance -> new RemainingBalance(
                        balance.userId(),
                        balance.balance().amount()
                ))
                .sorted(Comparator.comparing(
                        balance -> balance.userId().value()
                ))
                .toList();

        List<SettlementSuggestion> suggestions = new ArrayList<>();

        int debtorIndex = 0;
        int creditorIndex = 0;

        while (debtorIndex < debtors.size()
                && creditorIndex < creditors.size()) {

            RemainingBalance debtor = debtors.get(debtorIndex);
            RemainingBalance creditor = creditors.get(creditorIndex);

            BigDecimal amount = debtor.remaining()
                    .min(creditor.remaining());

            suggestions.add(new SettlementSuggestion(
                    debtor.userId(),
                    creditor.userId(),
                    Money.of(amount.toPlainString())
            ));

            debtor.subtract(amount);
            creditor.subtract(amount);

            if (debtor.remaining().signum() == 0) {
                debtorIndex++;
            }

            if (creditor.remaining().signum() == 0) {
                creditorIndex++;
            }
        }

        return List.copyOf(suggestions);
    }

    private static class RemainingBalance {

        private final UserId userId;
        private BigDecimal remaining;

        private RemainingBalance(UserId userId, BigDecimal remaining) {
            this.userId = userId;
            this.remaining = remaining;
        }

        UserId userId() {
            return userId;
        }

        BigDecimal remaining() {
            return remaining;
        }

        void subtract(BigDecimal amount) {
            remaining = remaining.subtract(amount);
        }
    }
}