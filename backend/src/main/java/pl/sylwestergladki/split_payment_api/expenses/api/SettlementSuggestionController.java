package pl.sylwestergladki.split_payment_api.expenses.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import pl.sylwestergladki.split_payment_api.expenses.application.SuggestGroupSettlementsUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/settlements")
public class SettlementSuggestionController {

    private final SuggestGroupSettlementsUseCase suggestGroupSettlementsUseCase;
    private final FindUserIdByKeycloakSubjectUseCase findUserIdByKeycloakSubjectUseCase;

    public SettlementSuggestionController(
            SuggestGroupSettlementsUseCase suggestGroupSettlementsUseCase,
            FindUserIdByKeycloakSubjectUseCase findUserIdByKeycloakSubjectUseCase
    ) {
        this.suggestGroupSettlementsUseCase = suggestGroupSettlementsUseCase;
        this.findUserIdByKeycloakSubjectUseCase = findUserIdByKeycloakSubjectUseCase;
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<SettlementSuggestionResponse>> getSuggestions(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserId requestedBy =
                findUserIdByKeycloakSubjectUseCase.execute(jwt.getSubject());

        var suggestions = suggestGroupSettlementsUseCase.execute(
                new GroupId(groupId),
                requestedBy
        );

        List<SettlementSuggestionResponse> response = suggestions.stream()
                .map(suggestion -> new SettlementSuggestionResponse(
                        suggestion.fromUserId().value(),
                        suggestion.toUserId().value(),
                        suggestion.amount().amount()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }
}