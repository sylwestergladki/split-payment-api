package pl.sylwestergladki.split_payment_api.expenses.domain.model;

import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.time.Instant;
import java.util.Objects;

public class Settlement {

    private final SettlementId id;
    private final GroupId groupId;
    private final UserId fromUserId;
    private final UserId toUserId;
    private final Money amount;
    private final Instant createdAt;

    private Settlement(
            SettlementId id,
            GroupId groupId,
            UserId fromUserId,
            UserId toUserId,
            Money amount,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.groupId = Objects.requireNonNull(groupId);
        this.fromUserId = Objects.requireNonNull(fromUserId);
        this.toUserId = Objects.requireNonNull(toUserId);
        this.amount = Objects.requireNonNull(amount);
        this.createdAt = Objects.requireNonNull(createdAt);

        if (fromUserId.equals(toUserId)) {
            throw new IllegalArgumentException(
                    "Cannot settle with yourself"
            );
        }

        if (amount.amount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Settlement amount must be positive"
            );
        }
    }

    public static Settlement create(
            GroupId groupId,
            UserId fromUserId,
            UserId toUserId,
            Money amount
    ) {
        return new Settlement(
                SettlementId.newId(),
                groupId,
                fromUserId,
                toUserId,
                amount,
                Instant.now()
        );
    }

    public static Settlement reconstitute(
            SettlementId id,
            GroupId groupId,
            UserId fromUserId,
            UserId toUserId,
            Money amount,
            Instant createdAt
    ) {
        return new Settlement(
                id,
                groupId,
                fromUserId,
                toUserId,
                amount,
                createdAt
        );
    }

    public SettlementId id() {
        return id;
    }

    public GroupId groupId() {
        return groupId;
    }

    public UserId fromUserId() {
        return fromUserId;
    }

    public UserId toUserId() {
        return toUserId;
    }

    public Money amount() {
        return amount;
    }

    public Instant createdAt() {
        return createdAt;
    }
}