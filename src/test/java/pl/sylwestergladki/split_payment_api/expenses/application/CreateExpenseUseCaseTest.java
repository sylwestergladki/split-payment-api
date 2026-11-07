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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateExpenseUseCaseTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private CreateExpenseUseCase useCase;

    @Test
    void shouldCreateExpenseForGroupMembers() {
        UserId creator = new UserId(UUID.randomUUID());
        UserId secondUser = new UserId(UUID.randomUUID());

        Group group = Group.create("Mountain trip", creator);
        group.addMember(secondUser);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        when(expenseRepository.save(any(Expense.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Expense expense = useCase.execute(
                group.id(),
                "Dinner",
                Money.of("100.00"),
                creator,
                List.of(creator, secondUser),
                creator
        );

        assertEquals("Dinner", expense.description());
        assertEquals(Money.of("100.00"), expense.amount());
        assertEquals(2, expense.splits().size());

        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void shouldRejectExpenseWhenRequesterIsNotGroupMember() {
        UserId creator = new UserId(UUID.randomUUID());
        UserId outsider = new UserId(UUID.randomUUID());

        Group group = Group.create("Mountain trip", creator);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        assertThrows(
                GroupNotFoundException.class,
                () -> useCase.execute(
                        group.id(),
                        "Dinner",
                        Money.of("100.00"),
                        creator,
                        List.of(creator),
                        outsider
                )
        );

        verifyNoInteractions(expenseRepository);
    }

    @Test
    void shouldRejectExpenseWhenPayerIsNotGroupMember() {
        UserId creator = new UserId(UUID.randomUUID());
        UserId outsider = new UserId(UUID.randomUUID());

        Group group = Group.create("Mountain trip", creator);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(
                        group.id(),
                        "Dinner",
                        Money.of("100.00"),
                        outsider,
                        List.of(creator, outsider),
                        creator
                )
        );

        verifyNoInteractions(expenseRepository);
    }

    @Test
    void shouldRejectExpenseWhenParticipantIsNotGroupMember() {
        UserId creator = new UserId(UUID.randomUUID());
        UserId outsider = new UserId(UUID.randomUUID());

        Group group = Group.create("Mountain trip", creator);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(
                        group.id(),
                        "Dinner",
                        Money.of("100.00"),
                        creator,
                        List.of(creator, outsider),
                        creator
                )
        );

        verifyNoInteractions(expenseRepository);
    }

    @Test
    void shouldRejectExpenseWhenGroupDoesNotExist() {
        GroupId groupId = GroupId.generate();
        UserId userId = new UserId(UUID.randomUUID());

        when(groupRepository.findById(groupId))
                .thenReturn(Optional.empty());

        assertThrows(
                GroupNotFoundException.class,
                () -> useCase.execute(
                        groupId,
                        "Dinner",
                        Money.of("100.00"),
                        userId,
                        List.of(userId),
                        userId
                )
        );

        verifyNoInteractions(expenseRepository);
    }
}