package pl.sylwestergladki.split_payment_api.groups.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pl.sylwestergladki.split_payment_api.groups.application.CreateGroupUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.GetGroupDetailsUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.GetUserGroupsUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.Group;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final CreateGroupUseCase createGroupUseCase;
    private final FindUserIdByKeycloakSubjectUseCase findUserIdUseCase;
    private final GetUserGroupsUseCase getUserGroupsUseCase;
    private final GetGroupDetailsUseCase getGroupDetailsUseCase;

    public GroupController(
            CreateGroupUseCase createGroupUseCase,
            FindUserIdByKeycloakSubjectUseCase findUserIdUseCase, GetUserGroupsUseCase getUserGroupsUseCase, GetGroupDetailsUseCase getGroupDetailsUseCase
    ) {
        this.createGroupUseCase = createGroupUseCase;
        this.findUserIdUseCase = findUserIdUseCase;
        this.getUserGroupsUseCase = getUserGroupsUseCase;
        this.getGroupDetailsUseCase = getGroupDetailsUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String keycloakSubject = jwt.getSubject();

        UserId creatorId = resolveUserId(keycloakSubject);

        Group group = createGroupUseCase.execute(
                request.name(),
                creatorId
        );

        return toResponse(group);
    }

    @GetMapping
    public List<GroupResponse> getMyGroups(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserId userId = findUserIdUseCase.execute(jwt.getSubject());

        return getUserGroupsUseCase.execute(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{groupId}")
    public GroupResponse getGroupDetails(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserId userId = findUserIdUseCase.execute(jwt.getSubject());

        Group group = getGroupDetailsUseCase.execute(
                new GroupId(groupId),
                userId
        );

        return toResponse(group);
    }

    private UserId resolveUserId(String keycloakSubject) {
        return findUserIdUseCase.execute(keycloakSubject);
    }

    private GroupResponse toResponse(Group group) {
        return new GroupResponse(
                group.id().value(),
                group.name(),
                group.createdBy().value(),
                group.createdAt(),
                group.members().stream()
                        .map(member -> new GroupResponse.GroupMemberResponse(
                                member.userId().value(),
                                member.joinedAt()
                        ))
                        .toList()
        );
    }
}
