package pl.sylwestergladki.split_payment_api.expenses.domain.model;

import java.util.Objects;
import java.util.UUID;

public record SettlementId(UUID value) {

    public SettlementId {
        Objects.requireNonNull(value, "Settlement ID cannot be null");
    }

    public static SettlementId newId() {
        return new SettlementId(UUID.randomUUID());
    }
}