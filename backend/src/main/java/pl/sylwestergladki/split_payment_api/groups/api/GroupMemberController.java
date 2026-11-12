package pl.sylwestergladki.split_payment_api.groups.api;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import pl.sylwestergladki.split_payment_api.groups.application.AddGroupMemberUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/members")
public class GroupMemberController {

    private final AddGroupMemberUseCase addGroupMemberUseCase;
    private final FindUserIdByKeycloakSubjectUseCase findUserIdUseCase;

    public GroupMemberController(
            AddGroupMemberUseCase addGroupMemberUseCase,
            FindUserIdByKeycloakSubjectUseCase findUserIdUseCase
    ) {
        this.addGroupMemberUseCase = addGroupMemberUseCase;
        this.findUserIdUseCase = findUserIdUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addMember(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddGroupMemberRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserId requestedBy =
                findUserIdUseCase.execute(jwt.getSubject());

        addGroupMemberUseCase.execute(
                new GroupId(groupId),
                new UserId(request.userId()),
                requestedBy
        );
    }
}