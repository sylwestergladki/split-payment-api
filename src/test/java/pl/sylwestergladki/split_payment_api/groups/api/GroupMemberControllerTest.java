package pl.sylwestergladki.split_payment_api.groups.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.sylwestergladki.split_payment_api.groups.application.AddGroupMemberUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;
import pl.sylwestergladki.split_payment_api.security.SecurityConfig;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupMemberController.class)
@Import(SecurityConfig.class)
class GroupMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddGroupMemberUseCase addGroupMemberUseCase;

    @MockitoBean
    private FindUserIdByKeycloakSubjectUseCase findUserIdUseCase;

    @Test
    void shouldAddMemberToGroup() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID newMemberId = UUID.randomUUID();

        UserId requester = new UserId(UUID.randomUUID());

        when(findUserIdUseCase.execute("keycloak-sub"))
                .thenReturn(requester);

        mockMvc.perform(
                        post("/api/groups/{groupId}/members", groupId)
                                .with(jwt().jwt(token ->
                                        token.subject("keycloak-sub")
                                ))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "userId": "%s"
                                        }
                                        """.formatted(newMemberId))
                )
                .andExpect(status().isNoContent());

        verify(addGroupMemberUseCase).execute(
                new GroupId(groupId),
                new UserId(newMemberId),
                requester
        );
    }

    @Test
    void shouldRejectRequestWithoutJwt() throws Exception {
        mockMvc.perform(
                        post("/api/groups/{groupId}/members", UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "userId": "11111111-1111-1111-1111-111111111111"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(addGroupMemberUseCase);
    }

    @Test
    void shouldRejectRequestWithoutUserId() throws Exception {
        mockMvc.perform(
                        post("/api/groups/{groupId}/members", UUID.randomUUID())
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(addGroupMemberUseCase);
    }
}