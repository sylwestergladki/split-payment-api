package pl.sylwestergladki.split_payment_api.expenses.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.sylwestergladki.split_payment_api.expenses.application.SuggestGroupSettlementsUseCase;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.SettlementSuggestion;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;
import pl.sylwestergladki.split_payment_api.security.SecurityConfig;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettlementSuggestionController.class)
@Import(SecurityConfig.class)
class SettlementSuggestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SuggestGroupSettlementsUseCase suggestGroupSettlementsUseCase;

    @MockitoBean
    private FindUserIdByKeycloakSubjectUseCase findUserIdByKeycloakSubjectUseCase;

    @Test
    void shouldReturnSettlementSuggestions() throws Exception {
        // given
        UUID groupUuid = UUID.randomUUID();
        GroupId groupId = new GroupId(groupUuid);

        UserId anna = new UserId(UUID.randomUUID());
        UserId john = new UserId(UUID.randomUUID());

        String keycloakSubject = "keycloak-user-123";

        when(findUserIdByKeycloakSubjectUseCase.execute(keycloakSubject))
                .thenReturn(anna);

        when(suggestGroupSettlementsUseCase.execute(groupId, anna))
                .thenReturn(List.of(
                        new SettlementSuggestion(
                                john,
                                anna,
                                Money.of("50.00")
                        )
                ));

        // when + then
        mockMvc.perform(get(
                        "/api/groups/{groupId}/settlements/suggestions",
                        groupUuid
                )
                        .with(jwt().jwt(jwt ->
                                jwt.subject(keycloakSubject)
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromUserId")
                        .value(john.value().toString()))
                .andExpect(jsonPath("$[0].toUserId")
                        .value(anna.value().toString()))
                .andExpect(jsonPath("$[0].amount").value(50.00));

        verify(suggestGroupSettlementsUseCase)
                .execute(groupId, anna);
    }

    @Test
    void shouldReturnEmptyListWhenNoSettlementsAreNeeded() throws Exception {
        // given
        UUID groupUuid = UUID.randomUUID();
        GroupId groupId = new GroupId(groupUuid);

        UserId userId = new UserId(UUID.randomUUID());

        when(findUserIdByKeycloakSubjectUseCase.execute("keycloak-user-123"))
                .thenReturn(userId);

        when(suggestGroupSettlementsUseCase.execute(groupId, userId))
                .thenReturn(List.of());

        // when + then
        mockMvc.perform(get(
                        "/api/groups/{groupId}/settlements/suggestions",
                        groupUuid
                )
                        .with(jwt().jwt(jwt ->
                                jwt.subject("keycloak-user-123")
                        )))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        UUID groupUuid = UUID.randomUUID();

        mockMvc.perform(get(
                        "/api/groups/{groupId}/settlements/suggestions",
                        groupUuid
                ))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(suggestGroupSettlementsUseCase);
    }
}