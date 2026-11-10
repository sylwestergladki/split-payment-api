package pl.sylwestergladki.split_payment_api.ai.application.model;

import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

public record ExpenseParsingContext(
        UserId requesterId,
        List<ParsingGroupMember> members
) {
    public ExpenseParsingContext {
        members = List.copyOf(members);
    }
}