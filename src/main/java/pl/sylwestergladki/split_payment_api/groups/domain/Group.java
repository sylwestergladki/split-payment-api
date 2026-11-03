package pl.sylwestergladki.split_payment_api.groups.domain;


import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.time.Instant;
import java.util.*;

public class Group {

    private final GroupId id;
    private String name;
    private final UserId createdBy;
    private final Instant createdAt;
    private final List<GroupMember> members;

    private Group(
            GroupId id,
            String name,
            UserId createdBy,
            Instant createdAt,
            List<GroupMember> members
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = requireText(name, "name");
        this.createdBy = Objects.requireNonNull(createdBy);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.members = new ArrayList<>(members);
    }

    public static Group create(String name, UserId createdBy) {
        Instant now = Instant.now();

        Group group = new Group(
                GroupId.generate(),
                name,
                createdBy,
                now,
                List.of()
        );

        group.addMember(createdBy);

        return group;
    }

    public void addMember(UserId userId) {
        Objects.requireNonNull(userId, "userId cannot be null");

        boolean alreadyMember = members.stream()
                .anyMatch(member -> member.userId().equals(userId));

        if (alreadyMember) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        members.add(GroupMember.join(userId));
    }

    public boolean hasMember(UserId userId) {
        return members.stream()
                .anyMatch(member -> member.userId().equals(userId));
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }

        return value.trim();
    }

    public static Group reconstitute(
            GroupId id,
            String name,
            UserId createdBy,
            Instant createdAt,
            List<GroupMember> members
    ) {
        return new Group(id, name, createdBy, createdAt, members);
    }

    public GroupId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public UserId createdBy() {
        return createdBy;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public List<GroupMember> members() {
        return Collections.unmodifiableList(members);
    }
}