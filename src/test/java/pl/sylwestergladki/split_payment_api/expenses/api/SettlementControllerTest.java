
package pl.sylwestergladki.split_payment_api.expenses.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pl.sylwestergladki.split_payment_api.expenses.application.GetGroupSettlementsUseCase;
import pl.sylwestergladki.split_payment_api.expenses.application.RecordSettlementUseCase;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Settlement;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;
import pl.sylwestergladki.split_payment_api.security.SecurityConfig;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettlementController.class)
@Import(SecurityConfig.class)
class SettlementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecordSettlementUseCase recordSettlementUseCase;

    @MockitoBean
    private FindUserIdByKeycloakSubjectUseCase findUserIdUseCase;

    @MockitoBean
    private GetGroupSettlementsUseCase getGroupSettlementsUseCase;

    @Test
    void shouldRecordSettlement() throws Exception {
        // given
        UUID groupId = UUID.randomUUID();
        UUID annaId = UUID.randomUUID();
        UUID johnId = UUID.randomUUID();

        Settlement settlement = Settlement.create(
                new GroupId(groupId),
                new UserId(johnId),
                new UserId(annaId),
                Money.of("50.00")
        );

        when(findUserIdUseCase.execute("keycloak-bartek"))
                .thenReturn(new UserId(johnId));

        when(recordSettlementUseCase.execute(
                eq(new GroupId(groupId)),
                eq(new UserId(johnId)),
                eq(new UserId(annaId)),
                argThat(money ->
                        money != null &&
                                money.amount().compareTo(
                                        new BigDecimal("50.00")
                                ) == 0
                ),
                eq(new UserId(johnId))
        )).thenReturn(settlement);

        // when + then
        mockMvc.perform(post("/api/groups/{groupId}/settlements", groupId)
                        .with(jwt().jwt(jwt ->
                                jwt.subject("keycloak-bartek")
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "toUserId": "%s",
                                  "amount": 50.00
                                }
                                """.formatted(annaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.groupId")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.fromUserId")
                        .value(johnId.toString()))
                .andExpect(jsonPath("$.toUserId")
                        .value(annaId.toString()))
                .andExpect(jsonPath("$.amount").value(50.00));
    }

    @Test
    void shouldReturnUnauthorizedWithoutJwt() throws Exception {
        UUID groupId = UUID.randomUUID();

        mockMvc.perform(post("/api/groups/{groupId}/settlements", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "toUserId": "%s",
                              "amount": 50.00
                            }
                            """.formatted(UUID.randomUUID())))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(recordSettlementUseCase);
    }

    @Test
    void shouldRejectZeroAmount() throws Exception {
        UUID groupId = UUID.randomUUID();

        mockMvc.perform(post("/api/groups/{groupId}/settlements", groupId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "toUserId": "%s",
                              "amount": 0
                            }
                            """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(recordSettlementUseCase);
    }

    @Test
    void shouldRejectMissingRecipient() throws Exception {
        UUID groupId = UUID.randomUUID();

        mockMvc.perform(post("/api/groups/{groupId}/settlements", groupId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "amount": 50.00
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(recordSettlementUseCase);
    }


    @Test
    void shouldReturnSettlementHistory() throws Exception {
        UUID groupId = UUID.randomUUID();
        UserId anna = new UserId(UUID.randomUUID());
        UserId john = new UserId(UUID.randomUUID());

        Settlement settlement = Settlement.create(
                new GroupId(groupId),
                john,
                anna,
                Money.of("30.00")
        );

        when(findUserIdUseCase.execute("keycloak-anna"))
                .thenReturn(anna);

        when(getGroupSettlementsUseCase.execute(
                new GroupId(groupId),
                anna
        )).thenReturn(List.of(settlement));

        mockMvc.perform(get("/api/groups/{groupId}/settlements", groupId)
                        .with(jwt().jwt(jwt ->
                                jwt.subject("keycloak-anna")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id")
                        .value(settlement.id().value().toString()))
                .andExpect(jsonPath("$[0].fromUserId")
                        .value(john.value().toString()))
                .andExpect(jsonPath("$[0].toUserId")
                        .value(anna.value().toString()))
                .andExpect(jsonPath("$[0].amount").value(30.00));
    }

}
