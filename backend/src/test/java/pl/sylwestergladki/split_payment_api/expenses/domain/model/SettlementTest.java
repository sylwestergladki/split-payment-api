package pl.sylwestergladki.split_payment_api.expenses.domain.model;

import org.junit.jupiter.api.Test;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettlementTest {

    @Test
    void shouldCreateSettlement() {
        // given
        GroupId groupId = new GroupId(UUID.randomUUID());
        UserId john = new UserId(UUID.randomUUID());
        UserId anna = new UserId(UUID.randomUUID());

        // when
        Settlement settlement = Settlement.create(
                groupId,
                john,
                anna,
                Money.of("50.00")
        );

        // then
        assertThat(settlement.id()).isNotNull();
        assertThat(settlement.groupId()).isEqualTo(groupId);
        assertThat(settlement.fromUserId()).isEqualTo(john);
        assertThat(settlement.toUserId()).isEqualTo(anna);
        assertThat(settlement.amount().amount())
                .isEqualByComparingTo("50.00");
        assertThat(settlement.createdAt()).isNotNull();
    }

    @Test
    void shouldRejectSettlementToYourself() {
        // given
        GroupId groupId = new GroupId(UUID.randomUUID());
        UserId userId = new UserId(UUID.randomUUID());

        // when + then
        assertThatThrownBy(() -> Settlement.create(
                groupId,
                userId,
                userId,
                Money.of("50.00")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot settle with yourself");
    }

    @Test
    void shouldRejectZeroAmount() {
        // given
        GroupId groupId = new GroupId(UUID.randomUUID());
        UserId john = new UserId(UUID.randomUUID());
        UserId anna = new UserId(UUID.randomUUID());

        // when + then
        assertThatThrownBy(() -> Settlement.create(
                groupId,
                john,
                anna,
                Money.of("0.00")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Settlement amount must be positive");
    }

    @Test
    void shouldPreserveDataWhenReconstitutingSettlement() {
        // given
        SettlementId id = SettlementId.newId();
        GroupId groupId = new GroupId(UUID.randomUUID());
        UserId from = new UserId(UUID.randomUUID());
        UserId to = new UserId(UUID.randomUUID());
        Instant createdAt = Instant.parse("2026-01-01T12:00:00Z");

        // when
        Settlement settlement = Settlement.reconstitute(
                id,
                groupId,
                from,
                to,
                Money.of("25.50"),
                createdAt
        );

        // then
        assertThat(settlement.id()).isEqualTo(id);
        assertThat(settlement.createdAt()).isEqualTo(createdAt);
        assertThat(settlement.amount().amount())
                .isEqualByComparingTo("25.50");
    }
}