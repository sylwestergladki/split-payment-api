package pl.sylwestergladki.split_payment_api.identity.api;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.sylwestergladki.split_payment_api.identity.application.SyncUserProfileUseCase;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUser;

@RestController
@RequestMapping("/api/me")
public class UserController {

    private final SyncUserProfileUseCase syncUserProfileUseCase;

    public UserController(SyncUserProfileUseCase syncUserProfileUseCase) {
        this.syncUserProfileUseCase = syncUserProfileUseCase;
    }

    @GetMapping
    public UserResponse getCurrentUser(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String subject = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String displayName = jwt.getClaimAsString("name");

        AppUser user = syncUserProfileUseCase.execute(
                subject,
                email,
                displayName
        );

        return new UserResponse(
                user.id().value(),
                user.email(),
                user.displayName(),
                user.createdAt()
        );
    }
}