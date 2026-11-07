package pl.sylwestergladki.split_payment_api.expenses.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.sylwestergladki.split_payment_api.expenses.domain.model.Expense;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.expenses.domain.repository.ExpenseRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.exception.GroupNotFoundException;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetGroupExpensesUseCaseTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GetGroupExpensesUseCase useCase;

    @Test
    void shouldReturnExpensesWhenUserIsGroupMember() {
        UserId user = new UserId(UUID.randomUUID());

        Group group = Group.create("Vacation", user);

        Expense expense = Expense.create(
                group.id(),
                "Dinner",
                Money.of("100.00"),
                user,
                List.of(user)
        );

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        when(expenseRepository.findAllByGroupId(group.id()))
                .thenReturn(List.of(expense));

        List<Expense> result = useCase.execute(group.id(), user);

        assertThat(result).containsExactly(expense);

        verify(expenseRepository).findAllByGroupId(group.id());
    }

    @Test
    void shouldReturnEmptyListWhenGroupHasNoExpenses() {
        UserId user = new UserId(UUID.randomUUID());

        Group group = Group.create("Vacation", user);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        when(expenseRepository.findAllByGroupId(group.id()))
                .thenReturn(List.of());

        List<Expense> result = useCase.execute(group.id(), user);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRejectUserWhoIsNotGroupMember() {
        UserId creator = new UserId(UUID.randomUUID());
        UserId outsider = new UserId(UUID.randomUUID());

        Group group = Group.create("Vacation", creator);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        assertThatThrownBy(() -> useCase.execute(group.id(), outsider))
                .isInstanceOf(GroupNotFoundException.class);

        verifyNoInteractions(expenseRepository);
    }

    @Test
    void shouldRejectNonexistentGroup() {
        GroupId groupId = new GroupId(UUID.randomUUID());
        UserId user = new UserId(UUID.randomUUID());

        when(groupRepository.findById(groupId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(groupId, user))
                .isInstanceOf(GroupNotFoundException.class);

        verifyNoInteractions(expenseRepository);
    }
}