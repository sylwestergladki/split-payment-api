package pl.sylwestergladki.split_payment_api.expenses.infrastructure.persistance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Settlement;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.infrastructure.persistance.GroupPersistenceAdapter;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUser;
import pl.sylwestergladki.split_payment_api.identity.infrastructure.persistance.AppUserPersistenceAdapter;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;


@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class SettlementPersistenceAdapterIT {

    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17")
                    .withDatabaseName("splitpayment")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );
        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );
    }

    @Autowired
    private SettlementPersistenceAdapter settlementAdapter;

    @Autowired
    private GroupPersistenceAdapter groupAdapter;

    @Autowired
    private AppUserPersistenceAdapter userAdapter;

    @Test
    void shouldSaveAndFindSettlement() {
        // given
        AppUser anna = userAdapter.save(
                AppUser.register(
                        "settlement-test-anna",
                        "settlement-anna@example.com",
                        "Anna"
                )
        );

        AppUser john = userAdapter.save(
                AppUser.register(
                        "settlement-test-john",
                        "settlement-john@example.com",
                        "john"
                )
        );

        Group group = Group.create(
                "Settlement test group",
                anna.id()
        );

        group.addMember(john.id());

        Group savedGroup = groupAdapter.save(group);

        Settlement settlement = Settlement.create(
                savedGroup.id(),
                john.id(),
                anna.id(),
                Money.of("50.00")
        );

        // when
        Settlement saved = settlementAdapter.save(settlement);

        List<Settlement> found =
                settlementAdapter.findAllByGroupId(savedGroup.id());

        // then
        assertThat(found).hasSize(1);

        Settlement persisted = found.getFirst();

        assertThat(persisted.id()).isEqualTo(saved.id());
        assertThat(persisted.groupId()).isEqualTo(savedGroup.id());
        assertThat(persisted.fromUserId()).isEqualTo(john.id());
        assertThat(persisted.toUserId()).isEqualTo(anna.id());

        assertThat(persisted.amount().amount())
                .isEqualByComparingTo("50.00");

        assertThat(persisted.createdAt())
                .isCloseTo(saved.createdAt(),
                        within(1, ChronoUnit.MICROS));
    }

    @Test
    void shouldFindOnlySettlementsForGivenGroup() {
        // given
        AppUser anna = userAdapter.save(
                AppUser.register(
                        "settlement-filter-anna",
                        "filter-anna@example.com",
                        "Anna"
                )
        );

        AppUser john = userAdapter.save(
                AppUser.register(
                        "settlement-filter-john",
                        "filter-john@example.com",
                        "john"
                )
        );

        Group groupA = Group.create("Group A", anna.id());
        groupA.addMember(john.id());

        Group groupB = Group.create("Group B", anna.id());
        groupB.addMember(john.id());

        groupAdapter.save(groupA);
        groupAdapter.save(groupB);

        Settlement settlementA = Settlement.create(
                groupA.id(),
                john.id(),
                anna.id(),
                Money.of("30.00")
        );

        Settlement settlementB = Settlement.create(
                groupB.id(),
                john.id(),
                anna.id(),
                Money.of("70.00")
        );

        settlementAdapter.save(settlementA);
        settlementAdapter.save(settlementB);

        // when
        List<Settlement> result =
                settlementAdapter.findAllByGroupId(groupA.id());

        // then
        assertThat(result).hasSize(1);

        assertThat(result.getFirst().id())
                .isEqualTo(settlementA.id());

        assertThat(result.getFirst().amount().amount())
                .isEqualByComparingTo("30.00");
    }

    @Test
    void shouldReturnEmptyListWhenGroupHasNoSettlements() {
        // given
        AppUser anna = userAdapter.save(
                AppUser.register(
                        "settlement-empty-anna",
                        "empty-anna@example.com",
                        "Anna"
                )
        );

        Group group = groupAdapter.save(
                Group.create("Empty group", anna.id())
        );

        // when
        List<Settlement> result =
                settlementAdapter.findAllByGroupId(group.id());

        // then
        assertThat(result).isEmpty();
    }
}