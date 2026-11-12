package pl.sylwestergladki.split_payment_api.expenses.api;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pl.sylwestergladki.split_payment_api.expenses.application.CreateExpenseUseCase;
import pl.sylwestergladki.split_payment_api.expenses.application.GetGroupExpensesUseCase;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Expense;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.groups.domain.exception.GroupNotFoundException;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;
import pl.sylwestergladki.split_payment_api.security.SecurityConfig;


import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@Import(SecurityConfig.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateExpenseUseCase createExpenseUseCase;

    @MockitoBean
    private FindUserIdByKeycloakSubjectUseCase findUserIdUseCase;

    @MockitoBean
    private GetGroupExpensesUseCase getGroupExpensesUseCase;

    @Test
    void shouldCreateExpense() throws Exception {
        // given
        UUID groupUuid = UUID.randomUUID();
        UserId requester = new UserId(UUID.randomUUID());
        UserId payer = new UserId(UUID.randomUUID());
        UserId participant = new UserId(UUID.randomUUID());

        Expense expense = Expense.create(
                new GroupId(groupUuid),
                "Dinner",
                Money.of("100.00"),
                payer,
                List.of(payer, participant)
        );

        when(findUserIdUseCase.execute("keycloak-sub"))
                .thenReturn(requester);

        when(createExpenseUseCase.execute(
                eq(new GroupId(groupUuid)),
                eq("Dinner"),
                eq(Money.of("100.00")),
                eq(payer),
                eq(List.of(payer, participant)),
                eq(requester)
        )).thenReturn(expense);

        String requestBody = """
                {
                  "description": "Dinner",
                  "amount": "100.00",
                  "paidBy": "%s",
                  "participants": ["%s", "%s"]
                }
                """.formatted(
                payer.value(),
                payer.value(),
                participant.value()
        );

        // when / then
        mockMvc.perform(post("/api/groups/{groupId}/expenses", groupUuid)
                        .with(jwt().jwt(jwt -> jwt.subject("keycloak-sub")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Dinner"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.splits.length()").value(2));

        verify(createExpenseUseCase).execute(
                new GroupId(groupUuid),
                "Dinner",
                Money.of("100.00"),
                payer,
                List.of(payer, participant),
                requester
        );
    }

    @Test
    void shouldRejectRequestWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/groups/{groupId}/expenses", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "description": "Dinner",
                              "amount": 100.00,
                              "paidBy": "11111111-1111-1111-1111-111111111111",
                              "participants": [
                                "11111111-1111-1111-1111-111111111111"
                              ]
                            }
                            """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(createExpenseUseCase);
    }

    @Test
    void shouldRejectZeroAmount() throws Exception {
        mockMvc.perform(post("/api/groups/{groupId}/expenses", UUID.randomUUID())
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "description": "Dinner",
                              "amount": 0,
                              "paidBy": "11111111-1111-1111-1111-111111111111",
                              "participants": [
                                "11111111-1111-1111-1111-111111111111"
                              ]
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createExpenseUseCase);
    }

    @Test
    void shouldRejectEmptyParticipants() throws Exception {
        mockMvc.perform(post("/api/groups/{groupId}/expenses", UUID.randomUUID())
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "description": "Dinner",
                              "amount": 100.00,
                              "paidBy": "11111111-1111-1111-1111-111111111111",
                              "participants": []
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createExpenseUseCase);
    }

    @Test
    void shouldRejectBlankDescription() throws Exception {
        mockMvc.perform(post("/api/groups/{groupId}/expenses", UUID.randomUUID())
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "description": "   ",
                              "amount": 100.00,
                              "paidBy": "11111111-1111-1111-1111-111111111111",
                              "participants": [
                                "11111111-1111-1111-1111-111111111111"
                              ]
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createExpenseUseCase);
    }

    @Test
    void shouldRejectMissingPayer() throws Exception {
        mockMvc.perform(post("/api/groups/{groupId}/expenses", UUID.randomUUID())
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "description": "Dinner",
                              "amount": 100.00,
                              "participants": [
                                "11111111-1111-1111-1111-111111111111"
                              ]
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createExpenseUseCase);
    }

    @Test
    void shouldReturnNotFoundWhenUserIsNotGroupMember() throws Exception {
        UUID groupUuid = UUID.randomUUID();
        UserId requester = new UserId(UUID.randomUUID());
        UserId payer = new UserId(UUID.randomUUID());

        when(findUserIdUseCase.execute("keycloak-sub"))
                .thenReturn(requester);

        when(createExpenseUseCase.execute(
                eq(new GroupId(groupUuid)),
                eq("Kolacja"),
                eq(Money.of("100.00")),
                eq(payer),
                eq(List.of(payer)),
                eq(requester)
        )).thenThrow(new GroupNotFoundException());

        mockMvc.perform(post("/api/groups/{groupId}/expenses", groupUuid)
                        .with(jwt().jwt(token -> token.subject("keycloak-sub")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "description": "Kolacja",
                              "amount": 100.00,
                              "paidBy": "%s",
                              "participants": ["%s"]
                            }
                            """.formatted(payer.value(), payer.value())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnGroupExpenses() throws Exception {
        UUID groupUuid = UUID.randomUUID();
        UserId user = new UserId(UUID.randomUUID());

        Expense expense = Expense.create(
                new GroupId(groupUuid),
                "Kolacja",
                Money.of("100.00"),
                user,
                List.of(user)
        );

        when(findUserIdUseCase.execute("keycloak-sub"))
                .thenReturn(user);

        when(getGroupExpensesUseCase.execute(new GroupId(groupUuid), user))
                .thenReturn(List.of(expense));

        mockMvc.perform(get("/api/groups/{groupId}/expenses", groupUuid)
                        .with(jwt().jwt(token -> token.subject("keycloak-sub"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].description").value("Kolacja"))
                .andExpect(jsonPath("$[0].amount").value(100.00))
                .andExpect(jsonPath("$[0].splits.length()").value(1));
    }
}