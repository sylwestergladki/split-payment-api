
package pl.sylwestergladki.split_payment_api.ai.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sylwestergladki.split_payment_api.ai.application.model.ExpenseParsingContext;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsedExpense;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsingGroupMember;
import pl.sylwestergladki.split_payment_api.ai.application.model.ResolvedExpense;
import pl.sylwestergladki.split_payment_api.ai.application.port.ExpenseTextParser;
import pl.sylwestergladki.split_payment_api.ai.application.port.GroupMemberDisplayNameProvider;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;

import pl.sylwestergladki.split_payment_api.groups.domain.exception.GroupNotFoundException;
import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParseExpenseUseCaseTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ExpenseTextParser expenseTextParser;

    @Mock
    private GroupMemberDisplayNameProvider memberProvider;

    private ParsedExpenseResolver resolver;

    private ParseExpenseUseCase useCase;

    @BeforeEach
    void setUp() {
        resolver = new ParsedExpenseResolver();

        useCase = new ParseExpenseUseCase(
                groupRepository,
                expenseTextParser,
                memberProvider,
                resolver
        );
    }

    @Test
    void shouldParseExpenseWithGroupMembersContext() {
        // given
        var requesterId = UserId.generate();
        var annaId = UserId.generate();

        var group = Group.create("Trip", requesterId);
        group.addMember(annaId);

        var groupId = group.id();

        when(groupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        var parsedExpense = new ParsedExpense(
                "Dinner",
                new BigDecimal("180.00"),
                "PLN",
                "me",
                List.of("me", "Anna")
        );

        var members = List.of(
                new ParsingGroupMember(requesterId, "John"),
                new ParsingGroupMember(annaId, "Anna")
        );

        // Tutaj pozostaw swoje mockowanie GroupRepository,
        // tak aby grupa zawierała requesterId oraz annaId.

        when(memberProvider.findMembers(
                List.of(requesterId, annaId)
        )).thenReturn(members);

        when(expenseTextParser.parse(
                eq("I paid 180 pln for dinner for me and Anna"),
                any(ExpenseParsingContext.class)
        )).thenReturn(parsedExpense);

        // when
        ResolvedExpense result = useCase.execute(
                groupId,
                requesterId,
                "I paid 180 pln for dinner for me and Anna"
        );

        // then
        assertThat(result.description()).isEqualTo("Dinner");
        assertThat(result.amount()).isEqualByComparingTo("180.00");
        assertThat(result.currency()).isEqualTo("PLN");
        assertThat(result.paidByUserId()).isEqualTo(requesterId);
        assertThat(result.participantUserIds())
                .containsExactly(requesterId, annaId);
    }

    @Test
    void shouldRejectParsingForNonExistingGroup() {
        // given
        var groupId = new GroupId(UUID.randomUUID());
        var requesterId = new UserId(UUID.randomUUID());

        when(groupRepository.findById(groupId))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                useCase.execute(
                        groupId,
                        requesterId,
                        "Dinner 180 pln"
                )
        ).isInstanceOf(GroupNotFoundException.class);

        verifyNoInteractions(memberProvider, expenseTextParser);
    }

    @Test
    void shouldRejectParsingForNonMember() {
        // given
        var ownerId = new UserId(UUID.randomUUID());
        var outsiderId = new UserId(UUID.randomUUID());

        var group = Group.create("Vacation", ownerId);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        // when / then
        assertThatThrownBy(() ->
                useCase.execute(
                        group.id(),
                        outsiderId,
                        "Dinner 180 pln"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Requester is not a member of the group");

        verifyNoInteractions(memberProvider, expenseTextParser);
    }

    @Test
    void shouldRejectBlankText() {
        // given
        var groupId = new GroupId(UUID.randomUUID());
        var requesterId = new UserId(UUID.randomUUID());

        // when / then
        assertThatThrownBy(() ->
                useCase.execute(
                        groupId,
                        requesterId,
                        "   "
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expense text cannot be blank");

        verifyNoInteractions(
                groupRepository,
                memberProvider,
                expenseTextParser
        );
    }

    @Test
    void shouldRejectIncompleteMemberContext() {
        // given
        var requesterId = new UserId(UUID.randomUUID());
        var annaId = new UserId(UUID.randomUUID());

        var group = Group.create("Vacation", requesterId);
        group.addMember(annaId);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        when(memberProvider.findMembers(any()))
                .thenReturn(List.of(
                        new ParsingGroupMember(requesterId, "John")
                ));

        // when / then
        assertThatThrownBy(() ->
                useCase.execute(
                        group.id(),
                        requesterId,
                        "Dinner 180 pln"
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Could not resolve all group members");

        verifyNoInteractions(expenseTextParser);
    }
}
