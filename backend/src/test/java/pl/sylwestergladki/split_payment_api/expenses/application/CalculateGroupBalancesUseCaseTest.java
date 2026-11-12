package pl.sylwestergladki.split_payment_api.expenses.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.sylwestergladki.split_payment_api.expenses.domain.model.Expense;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.MemberBalance;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Settlement;
import pl.sylwestergladki.split_payment_api.expenses.domain.repository.ExpenseRepository;
import pl.sylwestergladki.split_payment_api.expenses.domain.repository.SettlementRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.exception.GroupNotFoundException;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculateGroupBalancesUseCaseTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @InjectMocks
    private CalculateGroupBalancesUseCase useCase;

    @Test
    void shouldCalculateBalancesForGroupMembers() {
        UserId anna = new UserId(UUID.randomUUID());
        UserId john = new UserId(UUID.randomUUID());

        Group group = Group.create("Vacation", anna);
        group.addMember(john);

        Expense expense = Expense.create(
                group.id(),
                "Dinner",
                Money.of("100.00"),
                anna,
                List.of(anna, john)
        );

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        when(expenseRepository.findAllByGroupId(group.id()))
                .thenReturn(List.of(expense));

        when(settlementRepository.findAllByGroupId(group.id()))
                .thenReturn(List.of());


        List<MemberBalance> result =
                useCase.execute(group.id(), anna);

        Map<UserId, BigDecimal> balances = result.stream()
                .collect(Collectors.toMap(
                        MemberBalance::userId,
                        member -> member.balance().amount()
                ));

        assertThat(balances)
                .hasSize(2)
                .containsEntry(anna, new BigDecimal("50.00"))
                .containsEntry(john, new BigDecimal("-50.00"));
    }

    @Test
    void shouldReturnZeroBalancesForGroupWithoutExpenses() {
        UserId anna = new UserId(UUID.randomUUID());

        Group group = Group.create("Vacation", anna);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        when(expenseRepository.findAllByGroupId(group.id()))
                .thenReturn(List.of());

        List<MemberBalance> result =
                useCase.execute(group.id(), anna);

        assertThat(result).hasSize(1);

        assertThat(result.getFirst().balance().amount())
                .isEqualByComparingTo("0.00");
    }

    @Test
    void shouldRejectUserWhoIsNotGroupMember() {
        UserId creator = new UserId(UUID.randomUUID());
        UserId outsider = new UserId(UUID.randomUUID());

        Group group = Group.create("Dinner", creator);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        assertThatThrownBy(
                () -> useCase.execute(group.id(), outsider)
        ).isInstanceOf(GroupNotFoundException.class);

        verifyNoInteractions(
                expenseRepository,
                settlementRepository
        );
    }

    @Test
    void shouldRejectNonexistentGroup() {
        GroupId groupId = new GroupId(UUID.randomUUID());
        UserId user = new UserId(UUID.randomUUID());

        when(groupRepository.findById(groupId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> useCase.execute(groupId, user)
        ).isInstanceOf(GroupNotFoundException.class);

        verifyNoInteractions(
                expenseRepository,
                settlementRepository
        );
    }


    @Test
    void shouldCalculateBalancesIncludingSettlements() {
        // given
        UserId anna = new UserId(UUID.randomUUID());
        UserId john = new UserId(UUID.randomUUID());

        Group group = Group.create("Vacation", anna);
        group.addMember(john);
        
        Expense expense = Expense.create(
                group.id(),
                "Dinner",
                Money.of("100.00"),
                anna,
                List.of(anna, john)
        );

        
        Settlement settlement = Settlement.create(
                group.id(),
                john,
                anna,
                Money.of("30.00")
        );

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        when(expenseRepository.findAllByGroupId(group.id()))
                .thenReturn(List.of(expense));

        when(settlementRepository.findAllByGroupId(group.id()))
                .thenReturn(List.of(settlement));

        // when
        List<MemberBalance> result =
                useCase.execute(group.id(), anna);

        // then
        Map<UserId, BigDecimal> balances = result.stream()
                .collect(Collectors.toMap(
                        MemberBalance::userId,
                        member -> member.balance().amount()
                ));

        assertThat(balances)
                .containsEntry(anna, new BigDecimal("20.00"))
                .containsEntry(john, new BigDecimal("-20.00"));

        verify(expenseRepository)
                .findAllByGroupId(group.id());

        verify(settlementRepository)
                .findAllByGroupId(group.id());
    }


    @Test
    void shouldCalculateBalancesWhenThereAreOnlySettlements() {
        // given
        UserId anna = new UserId(UUID.randomUUID());
        UserId john = new UserId(UUID.randomUUID());

        Group group = Group.create("Vacation", anna);
        group.addMember(john);

        Settlement settlement = Settlement.create(
                group.id(),
                john,
                anna,
                Money.of("30.00")
        );

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        when(expenseRepository.findAllByGroupId(group.id()))
                .thenReturn(List.of());

        when(settlementRepository.findAllByGroupId(group.id()))
                .thenReturn(List.of(settlement));

        // when
        List<MemberBalance> result =
                useCase.execute(group.id(), anna);

        // then
        Map<UserId, BigDecimal> balances = result.stream()
                .collect(Collectors.toMap(
                        MemberBalance::userId,
                        member -> member.balance().amount()
                ));

        assertThat(balances)
                .hasSize(2)
                .containsEntry(anna, new BigDecimal("-30.00"))
                .containsEntry(john, new BigDecimal("30.00"));
    }


}