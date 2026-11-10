package pl.sylwestergladki.split_payment_api.ai.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pl.sylwestergladki.split_payment_api.ai.application.ParseExpenseUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses/parse")
public class ParseExpenseController {

    private final ParseExpenseUseCase parseExpenseUseCase;
    private final FindUserIdByKeycloakSubjectUseCase findUserIdUseCase;

    public ParseExpenseController(
            ParseExpenseUseCase parseExpenseUseCase,
            FindUserIdByKeycloakSubjectUseCase findUserIdUseCase
    ) {
        this.parseExpenseUseCase = parseExpenseUseCase;
        this.findUserIdUseCase = findUserIdUseCase;
    }

    @PostMapping
    public ResponseEntity<ParseExpenseResponse> parse(
            @PathVariable UUID groupId,
            @Valid @RequestBody ParseExpenseRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var requesterId = findUserIdUseCase.execute(
                jwt.getSubject()
        );

        var parsedExpense = parseExpenseUseCase.execute(
                new GroupId(groupId),
                requesterId,
                request.text()
        );

        return ResponseEntity.ok(
                ParseExpenseResponse.fromResolvedExpense(parsedExpense)
        );
    }
}