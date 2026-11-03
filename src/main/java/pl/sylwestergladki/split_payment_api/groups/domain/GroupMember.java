package pl.sylwestergladki.split_payment_api.groups.domain;

import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.time.Instant;
import java.util.Objects;

public record GroupMember(
        UserId userId,
        Instant joinedAt
) {
    public GroupMember {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(joinedAt, "joinedAt cannot be null");
    }

    public static GroupMember join(UserId userId) {
        return new GroupMember(userId, Instant.now());
    }
}