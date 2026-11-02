package pl.sylwestergladki.split_payment_api.identity.domain;


import java.time.Instant;
import java.util.Objects;

public class AppUser {

    private final UserId id;
    private final String keycloakSubject;

    private String email;
    private String displayName;

    private final Instant createdAt;
    private Instant updatedAt;

    private AppUser(
            UserId id,
            String keycloakSubject,
            String email,
            String displayName,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.keycloakSubject = requireText(
                keycloakSubject, "keycloakSubject"
        );
        this.email = requireText(email, "email");
        this.displayName = requireText(
                displayName, "displayName"
        );
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static AppUser register(
            String keycloakSubject,
            String email,
            String displayName
    ) {
        Instant now = Instant.now();

        return new AppUser(
                UserId.generate(),
                keycloakSubject,
                email,
                displayName,
                now,
                now
        );
    }

    public void updateProfile(
            String email,
            String displayName
    ) {
        this.email = requireText(email, "email");
        this.displayName = requireText(
                displayName, "displayName"
        );
        this.updatedAt = Instant.now();
    }

    private static String requireText(
            String value,
            String field
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " cannot be blank"
            );
        }

        return value.trim();
    }

    public UserId id() {
        return id;
    }

    public String keycloakSubject() {
        return keycloakSubject;
    }

    public String email() {
        return email;
    }

    public String displayName() {
        return displayName;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}