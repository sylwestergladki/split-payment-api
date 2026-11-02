package pl.sylwestergladki.split_payment_api.identity.infrastructure.persistance;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUser;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class AppUserPersistenceAdapterIT {

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
    private AppUserPersistenceAdapter adapter;

    @Autowired
    private SpringDataAppUserRepository jpaRepository;

    @Test
    void shouldSaveAndFindUserByKeycloakSubject() {
        // given
        AppUser user = AppUser.register(
                "keycloak-user-123",
                "john@example.com",
                "John"
        );

        // when
        AppUser saved = adapter.save(user);

        Optional<AppUser> found =
                adapter.findByKeycloakSubject("keycloak-user-123");

        // then
        assertThat(saved.id()).isEqualTo(user.id());

        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(user.id());
        assertThat(found.get().keycloakSubject())
                .isEqualTo("keycloak-user-123");
        assertThat(found.get().email()).isEqualTo("john@example.com");
        assertThat(found.get().displayName()).isEqualTo("John");
    }
}