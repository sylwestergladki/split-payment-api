package pl.sylwestergladki.split_payment_api.expenses.infrastructure.persistance;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "expense_splits")
@IdClass(ExpenseSplitId.class)
public class ExpenseSplitEntity {

    @Id
    @Column(name = "expense_id", nullable = false)
    private UUID expenseId;

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "expense_id",
            insertable = false,
            updatable = false
    )
    private ExpenseEntity expense;

    protected ExpenseSplitEntity() {
    }

    public ExpenseSplitEntity(UUID userId, BigDecimal amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public void setExpense(ExpenseEntity expense) {
        this.expense = expense;
        this.expenseId = expense.getId();
    }

    public UUID getExpenseId() {
        return expenseId;
    }

    public UUID getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}