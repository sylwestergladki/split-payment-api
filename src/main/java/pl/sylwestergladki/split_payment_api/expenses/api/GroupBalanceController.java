package pl.sylwestergladki.split_payment_api.expenses.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.sylwestergladki.split_payment_api.expenses.application.CalculateGroupBalancesUseCase;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;

import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/balances")
public class GroupBalanceController {

    private final CalculateGroupBalancesUseCase calculateBalancesUseCase;
    private final FindUserIdByKeycloakSubjectUseCase findUserIdUseCase;

    public GroupBalanceController(
            CalculateGroupBalancesUseCase calculateBalancesUseCase,
            FindUserIdByKeycloakSubjectUseCase findUserIdUseCase
    ) {
        this.calculateBalancesUseCase = calculateBalancesUseCase;
        this.findUserIdUseCase = findUserIdUseCase;
    }

    @GetMapping
    public List<MemberBalanceResponse> getBalances(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserId requestedBy =
                findUserIdUseCase.execute(jwt.getSubject());

        return calculateBalancesUseCase.execute(
                        new GroupId(groupId),
                        requestedBy
                )
                .stream()
                .map(balance -> new MemberBalanceResponse(
                        balance.userId().value(),
                        balance.balance().amount()
                ))
                .toList();
    }
}