
package pl.sylwestergladki.split_payment_api.ai.application;

import org.springframework.stereotype.Service;
import pl.sylwestergladki.split_payment_api.ai.application.model.ExpenseParsingContext;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsingGroupMember;
import pl.sylwestergladki.split_payment_api.ai.application.model.ResolvedExpense;
import pl.sylwestergladki.split_payment_api.ai.application.port.ExpenseTextParser;
import pl.sylwestergladki.split_payment_api.ai.application.port.GroupMemberDisplayNameProvider;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;

import pl.sylwestergladki.split_payment_api.groups.domain.exception.GroupNotFoundException;
import pl.sylwestergladki.split_payment_api.groups.domain.repository.GroupRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParseExpenseUseCase {

    private final GroupRepository groupRepository;
    private final ExpenseTextParser expenseTextParser;
    private final GroupMemberDisplayNameProvider memberProvider;
    private final ParsedExpenseResolver resolver;

    public ParseExpenseUseCase(
            GroupRepository groupRepository,
            ExpenseTextParser expenseTextParser,
            GroupMemberDisplayNameProvider memberProvider,
            ParsedExpenseResolver resolver
    ) {
        this.groupRepository = groupRepository;
        this.expenseTextParser = expenseTextParser;
        this.memberProvider = memberProvider;
        this.resolver = resolver;
    }

    public ResolvedExpense execute(
            GroupId groupId,
            UserId requesterId,
            String text
    ) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Expense text cannot be blank"
            );
        }

        if (text.length() > 1000) {
            throw new IllegalArgumentException(
                    "Expense text is too long"
            );
        }

        var group = groupRepository.findById(groupId)
                .orElseThrow(GroupNotFoundException::new);

        if (!group.hasMember(requesterId)) {
            throw new IllegalArgumentException(
                    "Requester is not a member of the group"
            );
        }

        List<UserId> memberIds = group.members()
                .stream()
                .map(member -> member.userId())
                .toList();

        var members = memberProvider.findMembers(memberIds);

        var resolvedIds = members.stream()
                .map(ParsingGroupMember::userId)
                .collect(Collectors.toSet());

        if (members.size() != memberIds.size()
                || !resolvedIds.equals(new HashSet<>(memberIds))) {
            throw new IllegalStateException(
                    "Could not resolve all group members"
            );
        }

        var context = new ExpenseParsingContext(
                requesterId,
                members
        );

        var parsed = expenseTextParser.parse(text, context);

        return resolver.resolve(parsed, context);
    }
}
