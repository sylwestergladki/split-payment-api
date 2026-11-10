package pl.sylwestergladki.split_payment_api.ai.application.model;

import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

public record ParsingGroupMember(
        UserId userId,
        String displayName
) {
}
