package pl.sylwestergladki.split_payment_api.groups.infrastructure.persistance;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUser;
import pl.sylwestergladki.split_payment_api.identity.infrastructure.persistance.AppUserPersistenceAdapter;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class GroupPersistenceAdapterTest {

    @Autowired
    private AppUserPersistenceAdapter userAdapter;

    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17")
                    .withDatabaseName("splitpayment")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private GroupPersistenceAdapter groupAdapter;

    @Test
    void shouldSaveAndFindGroupWithMembers() {
//        given
        AppUser user = AppUser.register(
                "keycloak-test-user-123",
                "test@example.com",
                "Test User"
        );
        AppUser savedUser = userAdapter.save(user);
        Group group = Group.create("Mountain trip", savedUser.id());

//        when
        Group savedGroup = groupAdapter.save(group);
        Group found = groupAdapter.findById(savedGroup.id()).orElseThrow();

//        then
        assertThat(found.id()).isEqualTo(savedGroup.id());
        assertThat(found.name()).isEqualTo("Mountain trip");
        assertThat(found.createdBy()).isEqualTo(savedUser.id());
        assertThat(found.members()).hasSize(1);
        assertThat(found.hasMember(savedUser.id())).isTrue();
    }

    @Test
    void shouldFindAllGroupsByMemberId() {
        // given
        AppUser userA = userAdapter.save(
                AppUser.register(
                        "keycloak-user-a",
                        "user-a@example.com",
                        "User A"
                )
        );

        AppUser userB = userAdapter.save(
                AppUser.register(
                        "keycloak-user-b",
                        "user-b@example.com",
                        "User B"
                )
        );

        Group group1 = Group.create("Trip to Spain", userA.id());
        group1.addMember(userB.id());

        Group group2 = Group.create("Weekend in Berlin", userA.id());

        Group group3 = Group.create("Dinner", userB.id());

        groupAdapter.save(group1);
        groupAdapter.save(group2);
        groupAdapter.save(group3);

        // when
        List<Group> userAGroups =
                groupAdapter.findAllByMemberId(userA.id());

        List<Group> userBGroups =
                groupAdapter.findAllByMemberId(userB.id());

        // then
        assertThat(userAGroups)
                .extracting(Group::name)
                .containsExactlyInAnyOrder(
                        "Trip to Spain",
                        "Weekend in Berlin"
                );

        assertThat(userBGroups)
                .extracting(Group::name)
                .containsExactlyInAnyOrder(
                        "Trip to Spain",
                        "Dinner"
                );
    }

}