package pl.sylwestergladki.split_payment_api.groups.api;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddGroupMemberRequest(
        @NotNull UUID userId
) {
}