
package pl.sylwestergladki.split_payment_api.ai.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.sylwestergladki.split_payment_api.ai.application.ParseExpenseUseCase;
import pl.sylwestergladki.split_payment_api.ai.application.model.ResolvedExpense;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;
import pl.sylwestergladki.split_payment_api.security.SecurityConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ParseExpenseController.class)
@Import(SecurityConfig.class)
class ParseExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParseExpenseUseCase parseExpenseUseCase;

    @MockitoBean
    private FindUserIdByKeycloakSubjectUseCase findUserIdUseCase;

    @Test
    void shouldParseExpense() throws Exception {
        // given
        var groupId = UUID.randomUUID();
        var requesterId = new UserId(UUID.randomUUID());
        var annaId = new UserId(UUID.randomUUID());

        var resolvedExpense = new ResolvedExpense(
                "Dinner",
                new BigDecimal("180.00"),
                "PLN",
                requesterId,
                List.of(requesterId, annaId)
        );

        when(findUserIdUseCase.execute("keycloak-user-123"))
                .thenReturn(requesterId);

        when(parseExpenseUseCase.execute(
                eq(new GroupId(groupId)),
                eq(requesterId),
                eq("I paid 180 pln for dinner for me and Anna")
        )).thenReturn(resolvedExpense);

        // when / then
        mockMvc.perform(post(
                        "/api/groups/{groupId}/expenses/parse",
                        groupId
                )
                        .with(jwt().jwt(jwt ->
                                jwt.subject("keycloak-user-123")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "I paid 180 pln for dinner for me and Anna"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Dinner"))
                .andExpect(jsonPath("$.amount").value(180.0))
                .andExpect(jsonPath("$.currency").value("PLN"))
                .andExpect(jsonPath("$.paidByUserId")
                        .value(requesterId.value().toString()))
                .andExpect(jsonPath("$.participantUserIds[0]")
                        .value(requesterId.value().toString()))
                .andExpect(jsonPath("$.participantUserIds[1]")
                        .value(annaId.value().toString()))
                .andExpect(jsonPath("$.participantUserIds.length()")
                        .value(2));

        verify(findUserIdUseCase)
                .execute("keycloak-user-123");

        verify(parseExpenseUseCase).execute(
                new GroupId(groupId),
                requesterId,
                "I paid 180 pln for dinner for me and Anna"
        );
    }

    @Test
    void shouldRejectBlankText() throws Exception {
        // given
        var groupId = UUID.randomUUID();

        // when / then
        mockMvc.perform(post(
                        "/api/groups/{groupId}/expenses/parse",
                        groupId
                )
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "   "
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(parseExpenseUseCase);
        verifyNoInteractions(findUserIdUseCase);
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        // given
        var groupId = UUID.randomUUID();

        // when / then
        mockMvc.perform(post(
                        "/api/groups/{groupId}/expenses/parse",
                        groupId
                )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Dinner 180 pln"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(parseExpenseUseCase);
        verifyNoInteractions(findUserIdUseCase);
    }

    @Test
    void shouldRejectTextLongerThan1000Characters()
            throws Exception {
        // given
        var groupId = UUID.randomUUID();
        var text = "a".repeat(1001);

        // when / then
        mockMvc.perform(post(
                        "/api/groups/{groupId}/expenses/parse",
                        groupId
                )
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "%s"
                                }
                                """.formatted(text)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(parseExpenseUseCase);
        verifyNoInteractions(findUserIdUseCase);
    }
}
