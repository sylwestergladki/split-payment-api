package pl.sylwestergladki.split_payment_api.expenses.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.sylwestergladki.split_payment_api.expenses.domain.model.Expense;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.MemberBalance;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Settlement;
import pl.sylwestergladki.split_payment_api.expenses.domain.repository.ExpenseRepository;
import pl.sylwestergladki.split_payment_api.expenses.domain.repository.SettlementRepository;
import pl.sylwestergladki.split_payment_api.expenses.domain.service.GroupBalanceCalculator;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.groups.domain.model.GroupMember;
import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.exception.GroupNotFoundException;

import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

@Service
public class CalculateGroupBalancesUseCase {

    private final GroupRepository groupRepository;
    private final ExpenseRepository expenseRepository;
    private final GroupBalanceCalculator calculator;
    private final SettlementRepository settlementRepository;

    public CalculateGroupBalancesUseCase(
            GroupRepository groupRepository,
            ExpenseRepository expenseRepository, SettlementRepository settlementRepository
    ) {
        this.groupRepository = groupRepository;
        this.expenseRepository = expenseRepository;
        this.settlementRepository = settlementRepository;
        this.calculator = new GroupBalanceCalculator();
    }


    @Transactional(readOnly = true)
    public List<MemberBalance> execute(
            GroupId groupId,
            UserId requestedBy
    ) {
        var group = groupRepository.findById(groupId)
                .orElseThrow(GroupNotFoundException::new);

        if (!group.hasMember(requestedBy)) {
            throw new GroupNotFoundException();
        }

        List<Expense> expenses =
                expenseRepository.findAllByGroupId(groupId);

        List<Settlement> settlements =
                settlementRepository.findAllByGroupId(groupId);

        return calculator.calculate(
                group.members()
                        .stream()
                        .map(GroupMember::userId)
                        .toList(),
                expenses,
                settlements
        );
    }

}