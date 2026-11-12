package pl.sylwestergladki.split_payment_api.groups.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GroupResponse(
        UUID id,
        String name,
        UUID createdBy,
        Instant createdAt,
        List<GroupMemberResponse> members
) {

    public record GroupMemberResponse(
            UUID userId,
            Instant joinedAt
    ) {}
}
