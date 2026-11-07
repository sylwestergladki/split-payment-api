package pl.sylwestergladki.split_payment_api.expenses.api;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import pl.sylwestergladki.split_payment_api.expenses.application.CreateExpenseUseCase;
import pl.sylwestergladki.split_payment_api.expenses.application.GetGroupExpensesUseCase;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Expense;
import pl.sylwestergladki.split_payment_api.expenses.domain.model.Money;
import pl.sylwestergladki.split_payment_api.groups.application.FindUserIdByKeycloakSubjectUseCase;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {

    private final CreateExpenseUseCase createExpenseUseCase;
    private final FindUserIdByKeycloakSubjectUseCase findUserIdUseCase;
    private final GetGroupExpensesUseCase getGroupExpensesUseCase;

    public ExpenseController(
            CreateExpenseUseCase createExpenseUseCase,
            FindUserIdByKeycloakSubjectUseCase findUserIdUseCase, GetGroupExpensesUseCase getGroupExpensesUseCase
    ) {
        this.createExpenseUseCase = createExpenseUseCase;
        this.findUserIdUseCase = findUserIdUseCase;
        this.getGroupExpensesUseCase = getGroupExpensesUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateExpenseRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserId requestedBy = findUserIdUseCase.execute(jwt.getSubject());

        Expense expense = createExpenseUseCase.execute(
                new GroupId(groupId),
                request.description(),
                new Money(request.amount()),
                new UserId(request.paidBy()),
                request.participants().stream()
                        .map(UserId::new)
                        .toList(),
                requestedBy
        );

        return toResponse(expense);
    }

    @GetMapping
    public List<ExpenseResponse> getGroupExpenses(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UserId requestedBy = findUserIdUseCase.execute(jwt.getSubject());

        return getGroupExpensesUseCase.execute(
                        new GroupId(groupId),
                        requestedBy
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.id().value(),
                expense.groupId().value(),
                expense.description(),
                expense.amount().amount(),
                expense.paidBy().value(),
                expense.splits().stream()
                        .map(split -> new ExpenseResponse.ExpenseSplitResponse(
                                split.userId().value(),
                                split.amount().amount()
                        ))
                        .toList(),
                expense.createdAt()
        );
    }
}
