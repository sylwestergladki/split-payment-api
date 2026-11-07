package pl.sylwestergladki.split_payment_api.expenses.domain.model;

import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class Expense {

    private final ExpenseId id;
    private final GroupId groupId;
    private final String description;
    private final Money amount;
    private final UserId paidBy;
    private final List<ExpenseSplit> splits;
    private final Instant createdAt;

    private Expense(
            ExpenseId id,
            GroupId groupId,
            String description,
            Money amount,
            UserId paidBy,
            List<ExpenseSplit> splits,
            Instant createdAt
    ) {
        this.id = id;
        this.groupId = groupId;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.splits = List.copyOf(splits);
        this.createdAt = createdAt;
    }

    public static Expense create(
            GroupId groupId,
            String description,
            Money amount,
            UserId paidBy,
            List<UserId> participants
    ) {
        Objects.requireNonNull(groupId, "Group ID cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(paidBy, "Payer cannot be null");
        Objects.requireNonNull(participants, "Participants cannot be null");

        if (description.isBlank()) {
            throw new IllegalArgumentException(
                    "Description cannot be blank"
            );
        }

        if (amount.amount().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Expense amount must be positive"
            );
        }

        if (participants.isEmpty()) {
            throw new IllegalArgumentException(
                    "Expense must have at least one participant"
            );
        }

        if (participants.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "Participants cannot contain null"
            );
        }

        if (new HashSet<>(participants).size() != participants.size()) {
            throw new IllegalArgumentException(
                    "Participants cannot contain duplicates"
            );
        }

        if (!participants.contains(paidBy)) {
            throw new IllegalArgumentException(
                    "Payer must be a participant"
            );
        }

        List<ExpenseSplit> splits = splitEqually(amount, participants);

        return new Expense(
                ExpenseId.generate(),
                groupId,
                description,
                amount,
                paidBy,
                splits,
                Instant.now()
        );
    }

    private static List<ExpenseSplit> splitEqually(
            Money amount,
            List<UserId> participants
    ) {
        List<UserId> sortedParticipants = participants.stream()
                .sorted(Comparator.comparing(userId -> userId.value().toString()))
                .toList();

        long totalCents = amount.amount()
                .movePointRight(2)
                .longValueExact();

        long baseCents = totalCents / sortedParticipants.size();
        long remainder = totalCents % sortedParticipants.size();

        List<ExpenseSplit> result = new ArrayList<>();

        for (int i = 0; i < sortedParticipants.size(); i++) {
            long cents = baseCents + (i < remainder ? 1 : 0);

            Money share = new Money(
                    BigDecimal.valueOf(cents, 2)
            );

            result.add(new ExpenseSplit(sortedParticipants.get(i), share));
        }

        return result;
    }

    public static Expense reconstitute(
            ExpenseId id,
            GroupId groupId,
            String description,
            Money amount,
            UserId paidBy,
            List<ExpenseSplit> splits,
            Instant createdAt
    ) {
        Objects.requireNonNull(id, "Expense ID cannot be null");
        Objects.requireNonNull(groupId, "Group ID cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(paidBy, "Payer cannot be null");
        Objects.requireNonNull(splits, "Splits cannot be null");
        Objects.requireNonNull(createdAt, "Created at cannot be null");

        return new Expense(
                id,
                groupId,
                description,
                amount,
                paidBy,
                splits,
                createdAt
        );
    }

    public ExpenseId id() {
        return id;
    }

    public GroupId groupId() {
        return groupId;
    }

    public String description() {
        return description;
    }

    public Money amount() {
        return amount;
    }

    public UserId paidBy() {
        return paidBy;
    }

    public List<ExpenseSplit> splits() {
        return splits;
    }

    public Instant createdAt() {
        return createdAt;
    }
}