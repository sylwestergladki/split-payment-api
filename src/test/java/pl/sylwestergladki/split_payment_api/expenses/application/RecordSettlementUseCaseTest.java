package pl.sylwestergladki.split_payment_api.expenses.application;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Settlement;
import pl.sylwestergladki.split_payment_api.expenses.domain.repository.SettlementRepository;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.groups.domain.exception.GroupNotFoundException;
import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordSettlementUseCaseTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @InjectMocks
    private RecordSettlementUseCase useCase;

    @Test
    void shouldRecordSettlement() {
        // given
        UserId anna = new UserId(UUID.randomUUID());
        UserId john = new UserId(UUID.randomUUID());

        Group group = Group.create("Mountain trip", anna);
        group.addMember(john);

        Money amount = Money.of("50.00");

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        when(settlementRepository.save(any(Settlement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Settlement result = useCase.execute(
                group.id(),
                john,
                anna,
                amount,
                john
        );

        // then
        assertThat(result.groupId()).isEqualTo(group.id());
        assertThat(result.fromUserId()).isEqualTo(john);
        assertThat(result.toUserId()).isEqualTo(anna);
        assertThat(result.amount().amount())
                .isEqualByComparingTo("50.00");

        verify(settlementRepository).save(any(Settlement.class));
    }

    @Test
    void shouldRejectSettlementRecordedByAnotherUser() {
        // given
        UserId anna = new UserId(UUID.randomUUID());
        UserId john = new UserId(UUID.randomUUID());

        Group group = Group.create("Mountain trip", anna);
        group.addMember(john);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));
        
        assertThatThrownBy(() -> useCase.execute(
                group.id(),
                john,
                anna,
                Money.of("50.00"),
                anna
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only the sender can record a settlement");

        verifyNoInteractions(settlementRepository);
    }

    @Test
    void shouldRejectSettlementWithUserOutsideGroup() {
        // given
        UserId anna = new UserId(UUID.randomUUID());
        UserId outsider = new UserId(UUID.randomUUID());

        Group group = Group.create("Mountain trip", anna);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        assertThatThrownBy(() -> useCase.execute(
                group.id(),
                anna,
                outsider,
                Money.of("50.00"),
                anna
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Both settlement participants must belong to the group"
                );

        verifyNoInteractions(settlementRepository);
    }

    @Test
    void shouldRejectRequestFromUserOutsideGroup() {
        // given
        UserId anna = new UserId(UUID.randomUUID());
        UserId outsider = new UserId(UUID.randomUUID());

        Group group = Group.create("Mountain trip", anna);

        when(groupRepository.findById(group.id()))
                .thenReturn(Optional.of(group));

        assertThatThrownBy(() -> useCase.execute(
                group.id(),
                outsider,
                anna,
                Money.of("50.00"),
                outsider
        ))
                .isInstanceOf(GroupNotFoundException.class);

        verifyNoInteractions(settlementRepository);
    }

    @Test
    void shouldRejectSettlementForNonExistingGroup() {
        // given
        GroupId groupId = new GroupId(UUID.randomUUID());

        UserId anna = new UserId(UUID.randomUUID());
        UserId john = new UserId(UUID.randomUUID());

        when(groupRepository.findById(groupId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(
                groupId,
                john,
                anna,
                Money.of("50.00"),
                john
        ))
                .isInstanceOf(GroupNotFoundException.class);

        verifyNoInteractions(settlementRepository);
    }
}