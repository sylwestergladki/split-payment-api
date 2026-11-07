package pl.sylwestergladki.split_payment_api.groups.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.sylwestergladki.split_payment_api.groups.application.CreateGroupUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.GetGroupDetailsUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.GetUserGroupsUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;
import org.springframework.context.annotation.Import;
import pl.sylwestergladki.split_payment_api.security.SecurityConfig;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(GroupController.class)
@Import(SecurityConfig.class)
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateGroupUseCase createGroupUseCase;

    @MockitoBean
    private FindUserIdByKeycloakSubjectUseCase findUserIdUseCase;

    @MockitoBean
    private GetUserGroupsUseCase getUserGroupsUseCase;

    @MockitoBean
    private GetGroupDetailsUseCase getGroupDetailsUseCase;


    @Test
    void shouldCreateGroup() throws Exception {
        UserId userId = new UserId(UUID.randomUUID());

        Group group = Group.create("Mountain trip", userId);

        when(findUserIdUseCase.execute("keycloak-user-123"))
                .thenReturn(userId);

        when(createGroupUseCase.execute("Mountain trip", userId))
                .thenReturn(group);

        mockMvc.perform(post("/api/groups")
                        .with(jwt().jwt(jwt -> jwt.subject("keycloak-user-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Mountain trip"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mountain trip"))
                .andExpect(jsonPath("$.createdBy").value(userId.value().toString()))
                .andExpect(jsonPath("$.id").value(group.id().value().toString()))
                .andExpect(jsonPath("$.members.length()").value(1));

        verify(findUserIdUseCase).execute("keycloak-user-123");
        verify(createGroupUseCase).execute("Mountain trip", userId);
    }

    @Test
    void shouldRejectEmptyGroupName() throws Exception {
        mockMvc.perform(post("/api/groups")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(createGroupUseCase);
    }

    @Test
    void shouldRejectRequestWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Mountain trip"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUserGroups() throws Exception {
        UserId userId = new UserId(UUID.randomUUID());

        Group group = Group.create("Mountain trip", userId);

        when(findUserIdUseCase.execute("keycloak-user-123"))
                .thenReturn(userId);

        when(getUserGroupsUseCase.execute(userId))
                .thenReturn(List.of(group));

        mockMvc.perform(get("/api/groups")
                        .with(jwt().jwt(jwt -> jwt.subject("keycloak-user-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Mountain trip"))
                .andExpect(jsonPath("$[0].createdBy").value(userId.value().toString()))
                .andExpect(jsonPath("$[0].members.length()").value(1));

        verify(findUserIdUseCase).execute("keycloak-user-123");
        verify(getUserGroupsUseCase).execute(userId);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoGroups() throws Exception {
        UserId userId = new UserId(UUID.randomUUID());

        when(findUserIdUseCase.execute("keycloak-user-123"))
                .thenReturn(userId);

        when(getUserGroupsUseCase.execute(userId))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/groups")
                        .with(jwt().jwt(jwt -> jwt.subject("keycloak-user-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}