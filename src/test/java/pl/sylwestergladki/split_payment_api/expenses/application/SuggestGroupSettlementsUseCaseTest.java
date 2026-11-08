package pl.sylwestergladki.split_payment_api.expenses.application;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Balance;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.MemberBalance;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.SettlementSuggestion;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuggestGroupSettlementsUseCaseTest {

    @Mock
    private CalculateGroupBalancesUseCase calculateGroupBalancesUseCase;

    private SuggestGroupSettlementsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SuggestGroupSettlementsUseCase(
                calculateGroupBalancesUseCase
        );
    }

    @Test
    void shouldSuggestSettlementsBasedOnGroupBalances() {
        // given
        GroupId groupId = new GroupId(UUID.randomUUID());

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

        when(calculateGroupBalancesUseCase.execute(groupId, anna))
                .thenReturn(balances);

        // when
        List<SettlementSuggestion> result =
                useCase.execute(groupId, anna);

        // then
        assertThat(result).hasSize(1);

        SettlementSuggestion suggestion = result.getFirst();

        assertThat(suggestion.fromUserId()).isEqualTo(john);
        assertThat(suggestion.toUserId()).isEqualTo(anna);
        assertThat(suggestion.amount().amount())
                .isEqualByComparingTo("50.00");

        verify(calculateGroupBalancesUseCase)
                .execute(groupId, anna);
    }

    @Test
    void shouldReturnEmptyListWhenBalancesAreZero() {
        // given
        GroupId groupId = new GroupId(UUID.randomUUID());
        UserId userId = new UserId(UUID.randomUUID());

        when(calculateGroupBalancesUseCase.execute(groupId, userId))
                .thenReturn(List.of(
                        new MemberBalance(
                                userId,
                                Balance.zero()
                        )
                ));

        // when
        List<SettlementSuggestion> result =
                useCase.execute(groupId, userId);

        // then
        assertThat(result).isEmpty();
    }
}