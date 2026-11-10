package pl.sylwestergladki.split_payment_api.ai.application;



import org.springframework.stereotype.Component;
import pl.sylwestergladki.split_payment_api.ai.application.model.ExpenseParsingContext;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsedExpense;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsingGroupMember;
import pl.sylwestergladki.split_payment_api.ai.application.model.ResolvedExpense;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Component
public class ParsedExpenseResolver {

    public ResolvedExpense resolve(
            ParsedExpense parsed,
            ExpenseParsingContext context
    ) {
        if (parsed == null) {
            throw new IllegalArgumentException("AI returned no expense");
        }

        if (parsed.description() == null
                || parsed.description().isBlank()) {
            throw new IllegalArgumentException(
                    "Expense description is required"
            );
        }

        if (parsed.amount() == null
                || parsed.amount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Expense amount must be positive"
            );
        }

        if (!"PLN".equals(parsed.currency())) {
            throw new IllegalArgumentException(
                    "Only PLN is supported"
            );
        }

        if (parsed.participants() == null
                || parsed.participants().isEmpty()) {
            throw new IllegalArgumentException(
                    "Expense participants are required"
            );
        }

        UserId payerId = resolveUser(
                parsed.paidBy(),
                context
        );

        var participantIds = new LinkedHashSet<UserId>();

        for (String participant : parsed.participants()) {
            UserId participantId = resolveUser(
                    participant,
                    context
            );

            if (!participantIds.add(participantId)) {
                throw new IllegalArgumentException(
                        "Duplicate expense participant: " + participant
                );
            }
        }

        return new ResolvedExpense(
                parsed.description().trim(),
                parsed.amount(),
                parsed.currency(),
                payerId,
                List.copyOf(participantIds)
        );
    }

    private UserId resolveUser(
            String name,
            ExpenseParsingContext context
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Participant name cannot be blank"
            );
        }

        String normalized = name.trim()
                .toLowerCase(Locale.ROOT);

        if ("me".equals(normalized)) {
            return context.requesterId();
        }

        List<ParsingGroupMember> matches = context.members()
                .stream()
                .filter(member -> member.displayName()
                        .trim()
                        .toLowerCase(Locale.ROOT)
                        .equals(normalized))
                .toList();

        if (matches.isEmpty()) {
            throw new IllegalArgumentException(
                    "Unknown group member: " + name
            );
        }

        if (matches.size() > 1) {
            throw new IllegalArgumentException(
                    "Ambiguous group member: " + name
            );
        }

        return matches.getFirst().userId();
    }
}
