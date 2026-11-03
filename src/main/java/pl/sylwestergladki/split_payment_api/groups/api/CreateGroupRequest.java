package pl.sylwestergladki.split_payment_api.groups.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(

        @NotBlank(message = "Group name cannot be empty")
        @Size(max = 100, message = "Group name cannot exceed 100 characters")
        String name

) {}