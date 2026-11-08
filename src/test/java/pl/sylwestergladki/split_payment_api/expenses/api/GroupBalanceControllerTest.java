package pl.sylwestergladki.split_payment_api.expenses.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pl.sylwestergladki.split_payment_api.expenses.application.CalculateGroupBalancesUseCase;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Balance;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.MemberBalance;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;
import pl.sylwestergladki.split_payment_api.security.SecurityConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GroupBalanceController.class)
@Import(SecurityConfig.class)
class GroupBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalculateGroupBalancesUseCase calculateBalancesUseCase;

    @MockitoBean
    private FindUserIdByKeycloakSubjectUseCase findUserIdUseCase;

    @Test
    void shouldReturnBalances() throws Exception {
        UUID groupUuid = UUID.randomUUID();

        UserId anna = new UserId(UUID.randomUUID());
        UserId bartek = new UserId(UUID.randomUUID());

        when(findUserIdUseCase.execute("keycloak-sub"))
                .thenReturn(anna);

        when(calculateBalancesUseCase.execute(
                new GroupId(groupUuid), anna
        )).thenReturn(List.of(
                new MemberBalance(
                        anna,
                        new Balance(new BigDecimal("50.00"))
                ),
                new MemberBalance(
                        bartek,
                        new Balance(new BigDecimal("-50.00"))
                )
        ));

        mockMvc.perform(
                        get("/api/groups/{groupId}/balances", groupUuid)
                                .with(jwt().jwt(token ->
                                        token.subject("keycloak-sub")
                                ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId")
                        .value(anna.value().toString()))
                .andExpect(jsonPath("$[0].balance").value(50.00))
                .andExpect(jsonPath("$[1].userId")
                        .value(bartek.value().toString()))
                .andExpect(jsonPath("$[1].balance").value(-50.00));
    }

    @Test
    void shouldRejectRequestWithoutJwt() throws Exception {
        mockMvc.perform(
                        get("/api/groups/{groupId}/balances",
                                UUID.randomUUID())
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(calculateBalancesUseCase);
    }
}