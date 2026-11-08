package pl.sylwestergladki.split_payment_api.expenses.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.SettlementSuggestion;
import pl.sylwestergladki.split_payment_api.expenses.domain.service.SettlementSuggestionCalculator;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

@Service
public class SuggestGroupSettlementsUseCase {

    private final CalculateGroupBalancesUseCase calculateGroupBalancesUseCase;
    private final SettlementSuggestionCalculator calculator;

    public SuggestGroupSettlementsUseCase(
            CalculateGroupBalancesUseCase calculateGroupBalancesUseCase
    ) {
        this.calculateGroupBalancesUseCase = calculateGroupBalancesUseCase;
        this.calculator = new SettlementSuggestionCalculator();
    }

    @Transactional(readOnly = true)
    public List<SettlementSuggestion> execute(
            GroupId groupId,
            UserId requestedBy
    ) {
        var balances = calculateGroupBalancesUseCase.execute(
                groupId,
                requestedBy
        );

        return calculator.calculate(balances);
    }
}