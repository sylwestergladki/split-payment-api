package pl.sylwestergladki.split_payment_api.identity.infrastructure.persistance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class AppUserEntity {

    @Id
    private UUID id;

    @Column(
            name = "keycloak_subject",
            nullable = false,
            unique = true,
            updatable = false,
            length = 255
    )
    private String keycloakSubject;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AppUserEntity() {
    }

    public AppUserEntity(
            UUID id,
            String keycloakSubject,
            String email,
            String displayName,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.keycloakSubject = keycloakSubject;
        this.email = email;
        this.displayName = displayName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getKeycloakSubject() {
        return keycloakSubject;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}