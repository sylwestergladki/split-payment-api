package pl.sylwestergladki.split_payment_api.expenses.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Balance;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.MemberBalance;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.SettlementSuggestion;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class SettlementSuggestionCalculatorTest {

    private final SettlementSuggestionCalculator calculator =
            new SettlementSuggestionCalculator();

    @Test
    void shouldSuggestSingleSettlement() {
        UserId anna = new UserId(UUID.randomUUID());
        UserId john = new UserId(UUID.randomUUID());

        List<MemberBalance> balances = List.of(
                new MemberBalance(
                        anna,
                        new Balance(new BigDecimal("50.00"))
                ),
                new MemberBalance(
                        john,
                        new Balance(new BigDecimal("-50.00"))
                )
        );

        List<SettlementSuggestion> result =
                calculator.calculate(balances);

        assertThat(result).hasSize(1);

        SettlementSuggestion suggestion = result.getFirst();

        assertThat(suggestion.fromUserId()).isEqualTo(john);
        assertThat(suggestion.toUserId()).isEqualTo(anna);
        assertThat(suggestion.amount().amount())
                .isEqualByComparingTo("50.00");
    }

    @Test
    void shouldReturnEmptyListWhenEveryoneIsSettled() {
        UserId anna = new UserId(UUID.randomUUID());
        UserId john = new UserId(UUID.randomUUID());

        List<MemberBalance> balances = List.of(
                new MemberBalance(anna, Balance.zero()),
                new MemberBalance(john, Balance.zero())
        );

        assertThat(calculator.calculate(balances)).isEmpty();
    }

    @Test
    void shouldRejectBalancesThatDoNotSumToZero() {
        UserId anna = new UserId(UUID.randomUUID());

        List<MemberBalance> balances = List.of(
                new MemberBalance(
                        anna,
                        new Balance(new BigDecimal("10.00"))
                )
        );

        assertThatThrownBy(() -> calculator.calculate(balances))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Balances must sum to zero");
    }
}