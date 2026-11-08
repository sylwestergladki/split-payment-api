package pl.sylwestergladki.split_payment_api.expenses.application;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Settlement;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.SettlementId;
import pl.sylwestergladki.split_payment_api.expenses.domain.repository.SettlementRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.groups.domain.exception.GroupNotFoundException;
import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetGroupSettlementsUseCaseTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @InjectMocks
    private GetGroupSettlementsUseCase useCase;

    @Test
    void shouldReturnSettlementsNewestFirst() {
        // given
        UserId anna = new UserId(UUID.randomUUID());
        UserId john = new UserId(UUID.randomUUID());

        Group group = Group.create("Vacation", anna);
        group.addMember(john);

        Settlement older = Settlement.reconstitute(
                SettlementId.newId(),
                group.id(),
                john,
                anna,
                Money.of("20.00"),
                Instant.parse("2026-11-01T12:00:00Z")
        );

        Settlement newer = Settlement.reconstitute(
                SettlementId.newId(),
                group.id(),
                john,
                anna,
                Money.of("30.00"),
                Instant.parse("2026-11-08T12:00:00Z")
        );

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        // Celowo zwracamy starszą spłatę jako pierwszą.
        when(settlementRepository.findAllByGroupId(group.id()))
                .thenReturn(List.of(older, newer));

        // when
        List<Settlement> result =
                useCase.execute(group.id(), anna);

        // then
        assertThat(result)
                .extracting(Settlement::id)
                .containsExactly(newer.id(), older.id());

        verify(settlementRepository)
                .findAllByGroupId(group.id());
    }

    @Test
    void shouldReturnEmptyListWhenGroupHasNoSettlements() {
        UserId anna = new UserId(UUID.randomUUID());
        Group group = Group.create("Vacation", anna);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        when(settlementRepository.findAllByGroupId(group.id()))
                .thenReturn(List.of());

        List<Settlement> result =
                useCase.execute(group.id(), anna);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRejectUserOutsideGroup() {
        UserId anna = new UserId(UUID.randomUUID());
        UserId outsider = new UserId(UUID.randomUUID());

        Group group = Group.create("Vacation", anna);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        assertThatThrownBy(() ->
                useCase.execute(group.id(), outsider)
        ).isInstanceOf(GroupNotFoundException.class);

        verifyNoInteractions(settlementRepository);
    }

    @Test
    void shouldRejectNonexistentGroup() {
        GroupId groupId = new GroupId(UUID.randomUUID());
        UserId anna = new UserId(UUID.randomUUID());

        when(groupRepository.findById(groupId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.execute(groupId, anna)
        ).isInstanceOf(GroupNotFoundException.class);

        verifyNoInteractions(settlementRepository);
    }
}
