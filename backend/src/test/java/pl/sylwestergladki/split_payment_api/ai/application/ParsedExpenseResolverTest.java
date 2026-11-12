package pl.sylwestergladki.split_payment_api.ai.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.sylwestergladki.split_payment_api.ai.application.model.ExpenseParsingContext;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsedExpense;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsingGroupMember;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParsedExpenseResolverTest {

    private ParsedExpenseResolver resolver;

    private UserId requesterId;
    private UserId annaId;
    private ExpenseParsingContext context;

    @BeforeEach
    void setUp() {
        resolver = new ParsedExpenseResolver();

        requesterId = UserId.generate();
        annaId = UserId.generate();

        context = new ExpenseParsingContext(
                requesterId,
                List.of(
                        new ParsingGroupMember(requesterId, "John"),
                        new ParsingGroupMember(annaId, "Anna")
                )
        );
    }

    @Test
    void shouldResolveExpenseParticipants() {
        var parsed = new ParsedExpense(
                "Dinner",
                new BigDecimal("180.00"),
                "PLN",
                "me",
                List.of("me", "Anna")
        );

        var result = resolver.resolve(parsed, context);

        assertThat(result.paidByUserId())
                .isEqualTo(requesterId);

        assertThat(result.participantUserIds())
                .containsExactly(requesterId, annaId);

        assertThat(result.amount())
                .isEqualByComparingTo("180.00");
    }

    @Test
    void shouldRejectUnknownMember() {
        var parsed = new ParsedExpense(
                "Dinner",
                new BigDecimal("180"),
                "PLN",
                "me",
                List.of("me", "Unknown")
        );

        assertThatThrownBy(() -> resolver.resolve(parsed, context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown group member");
    }

    @Test
    void shouldRejectNegativeAmount() {
        var parsed = new ParsedExpense(
                "Dinner",
                new BigDecimal("-10"),
                "PLN",
                "me",
                List.of("me", "Anna")
        );

        assertThatThrownBy(() -> resolver.resolve(parsed, context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount must be positive");
    }

    @Test
    void shouldRejectAmbiguousMember() {
        var anotherAnnaId = UserId.generate();

        var ambiguousContext = new ExpenseParsingContext(
                requesterId,
                List.of(
                        new ParsingGroupMember(requesterId, "John"),
                        new ParsingGroupMember(annaId, "Anna"),
                        new ParsingGroupMember(anotherAnnaId, "Anna")
                )
        );

        var parsed = new ParsedExpense(
                "Dinner",
                new BigDecimal("180"),
                "PLN",
                "me",
                List.of("Anna")
        );

        assertThatThrownBy(
                () -> resolver.resolve(parsed, ambiguousContext)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ambiguous group member");
    }
}
