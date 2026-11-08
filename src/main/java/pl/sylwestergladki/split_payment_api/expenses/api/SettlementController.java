package pl.sylwestergladki.split_payment_api.expenses.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pl.sylwestergladki.split_payment_api.expenses.application.GetGroupSettlementsUseCase;
import pl.sylwestergladki.split_payment_api.expenses.application.RecordSettlementUseCase;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Settlement;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/settlements")
public class SettlementController {

    private final RecordSettlementUseCase recordSettlementUseCase;
    private final FindUserIdByKeycloakSubjectUseCase findUserIdUseCase;
    private final GetGroupSettlementsUseCase getGroupSettlementsUseCase;

    public SettlementController(
            RecordSettlementUseCase recordSettlementUseCase,
            FindUserIdByKeycloakSubjectUseCase findUserIdUseCase, GetGroupSettlementsUseCase getGroupSettlementsUseCase
    ) {
        this.recordSettlementUseCase = recordSettlementUseCase;
        this.findUserIdUseCase = findUserIdUseCase;
        this.getGroupSettlementsUseCase = getGroupSettlementsUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SettlementResponse recordSettlement(
            @PathVariable UUID groupId,
            @Valid @RequestBody RecordSettlementRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserId requestedBy = findUserIdUseCase.execute(
                jwt.getSubject()
        );

        Settlement settlement = recordSettlementUseCase.execute(
                new GroupId(groupId),
                requestedBy,
                new UserId(request.toUserId()),
                Money.of(request.amount().toPlainString()),
                requestedBy
        );

        return new SettlementResponse(
                settlement.id().value(),
                settlement.groupId().value(),
                settlement.fromUserId().value(),
                settlement.toUserId().value(),
                settlement.amount().amount(),
                settlement.createdAt()
        );
    }


    @GetMapping
    public List<SettlementResponse> getSettlements(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserId requestedBy = findUserIdUseCase.execute(
                jwt.getSubject()
        );

        return getGroupSettlementsUseCase.execute(
                        new GroupId(groupId),
                        requestedBy
                )
                .stream()
                .map(settlement -> new SettlementResponse(
                        settlement.id().value(),
                        settlement.groupId().value(),
                        settlement.fromUserId().value(),
                        settlement.toUserId().value(),
                        settlement.amount().amount(),
                        settlement.createdAt()
                ))
                .toList();
    }

}
