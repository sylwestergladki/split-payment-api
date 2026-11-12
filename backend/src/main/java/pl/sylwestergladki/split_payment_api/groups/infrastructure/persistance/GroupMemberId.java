package pl.sylwestergladki.split_payment_api.groups.infrastructure.persistance;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class GroupMemberId implements Serializable {

    private UUID groupId;
    private UUID userId;

    public GroupMemberId() {
    }

    public GroupMemberId(UUID groupId, UUID userId) {
        this.groupId = groupId;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupMemberId that)) return false;
        return Objects.equals(groupId, that.groupId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, userId);
    }
}