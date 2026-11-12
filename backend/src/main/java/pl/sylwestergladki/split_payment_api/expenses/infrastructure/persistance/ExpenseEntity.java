package pl.sylwestergladki.split_payment_api.expenses.infrastructure.persistance;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "expenses")
public class ExpenseEntity {

    @Id
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "description", nullable = false, length = 255)
    private String description;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_by", nullable = false)
    private UUID paidBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "expense",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ExpenseSplitEntity> splits = new ArrayList<>();

    protected ExpenseEntity() {
        // Required by JPA
    }

    public ExpenseEntity(
            UUID id,
            UUID groupId,
            String description,
            BigDecimal amount,
            UUID paidBy,
            Instant createdAt
    ) {
        this.id = id;
        this.groupId = groupId;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.createdAt = createdAt;
    }

    public void addSplit(ExpenseSplitEntity split) {
        splits.add(split);
        split.setExpense(this);
    }

    public UUID getId() {
        return id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public UUID getPaidBy() {
        return paidBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<ExpenseSplitEntity> getSplits() {
        return splits;
    }
}
