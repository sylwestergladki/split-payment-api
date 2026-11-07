package pl.sylwestergladki.split_payment_api.expenses.infrastructure.persistance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.testcontainers.postgresql.PostgreSQLContainer;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Expense;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExpensePersistenceAdapterIT {

    @Container
    static final org.testcontainers.postgresql.PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17")
                    .withDatabaseName("splitpayment")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private ExpensePersistenceAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldSaveExpenseAndAllSplits() {
        // given
        UserId user1 = new UserId(UUID.randomUUID());
        UserId user2 = new UserId(UUID.randomUUID());
        UserId user3 = new UserId(UUID.randomUUID());

        GroupId groupId = new GroupId(UUID.randomUUID());

        insertUser(user1);
        insertUser(user2);
        insertUser(user3);
        insertGroup(groupId, user1);

        Expense expense = Expense.create(
                groupId,
                "Dinner",
                Money.of("100.00"),
                user1,
                List.of(user1, user2, user3)
        );

        // when
        Expense savedExpense = adapter.save(expense);

        // then
        assertThat(savedExpense.id()).isEqualTo(expense.id());
        assertThat(savedExpense.groupId()).isEqualTo(groupId);
        assertThat(savedExpense.amount()).isEqualTo(Money.of("100.00"));
        assertThat(savedExpense.splits()).hasSize(3);

        Integer expenseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM expenses WHERE id = ?",
                Integer.class,
                expense.id().value()
        );

        assertThat(expenseCount).isEqualTo(1);

        Integer splitsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM expense_splits WHERE expense_id = ?",
                Integer.class,
                expense.id().value()
        );

        assertThat(splitsCount).isEqualTo(3);

        BigDecimal totalSplits = jdbcTemplate.queryForObject(
                """
                SELECT SUM(amount)
                FROM expense_splits
                WHERE expense_id = ?
                """,
                BigDecimal.class,
                expense.id().value()
        );

        assertThat(totalSplits)
                .isEqualByComparingTo(new BigDecimal("100.00"));
    }

    private void insertUser(UserId userId) {
        jdbcTemplate.update(
                """
                INSERT INTO app_users (
                    id,
                    keycloak_subject,
                    email,
                    display_name,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, NOW(), NOW())
                """,
                userId.value(),
                UUID.randomUUID().toString(),
                "test-" + userId.value() + "@example.com",
                "Test User"
        );
    }

    private void insertGroup(GroupId groupId, UserId creatorId) {
        jdbcTemplate.update(
                """
                INSERT INTO groups (id, name, created_by, created_at)
                VALUES (?, ?, ?, NOW())
                """,
                groupId.value(),
                "Test group",
                creatorId.value()
        );
    }

    @Test
    void shouldFindAllExpensesByGroupId() {
        // given
        UserId user1 = new UserId(UUID.randomUUID());
        UserId user2 = new UserId(UUID.randomUUID());

        GroupId groupId = new GroupId(UUID.randomUUID());
        GroupId otherGroupId = new GroupId(UUID.randomUUID());

        insertUser(user1);
        insertUser(user2);

        insertGroup(groupId, user1);
        insertGroup(otherGroupId, user1);

        Expense dinner = Expense.create(
                groupId,
                "Kolacja",
                Money.of("100.00"),
                user1,
                List.of(user1, user2)
        );

        Expense taxi = Expense.create(
                groupId,
                "Taxi",
                Money.of("30.00"),
                user2,
                List.of(user1, user2)
        );

        Expense otherExpense = Expense.create(
                otherGroupId,
                "Hotel",
                Money.of("200.00"),
                user1,
                List.of(user1)
        );

        adapter.save(dinner);
        adapter.save(taxi);
        adapter.save(otherExpense);

        // when
        List<Expense> expenses = adapter.findAllByGroupId(groupId);

        // then
        assertThat(expenses).hasSize(2);

        assertThat(expenses)
                .extracting(Expense::id)
                .containsExactlyInAnyOrder(
                        dinner.id(),
                        taxi.id()
                );

        assertThat(expenses)
                .flatExtracting(Expense::splits)
                .hasSize(4);

        assertThat(expenses)
                .extracting(Expense::groupId)
                .containsOnly(groupId);
    }

    @Test
    void shouldReturnEmptyListWhenGroupHasNoExpenses() {
        // given
        UserId user = new UserId(UUID.randomUUID());
        GroupId groupId = new GroupId(UUID.randomUUID());

        insertUser(user);
        insertGroup(groupId, user);

        // when
        List<Expense> expenses = adapter.findAllByGroupId(groupId);

        // then
        assertThat(expenses).isEmpty();
    }
}