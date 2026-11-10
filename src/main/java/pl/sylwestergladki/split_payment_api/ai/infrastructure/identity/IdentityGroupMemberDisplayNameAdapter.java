package pl.sylwestergladki.split_payment_api.ai.infrastructure.identity;

import org.springframework.stereotype.Component;
import pl.sylwestergladki.split_payment_api.ai.application.model.ParsingGroupMember;
import pl.sylwestergladki.split_payment_api.ai.application.port.GroupMemberDisplayNameProvider;
import pl.sylwestergladki.split_payment_api.identity.domain.AppUserRepository;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

@Component
public class IdentityGroupMemberDisplayNameAdapter
        implements GroupMemberDisplayNameProvider {

    private final AppUserRepository appUserRepository;

    public IdentityGroupMemberDisplayNameAdapter(
            AppUserRepository appUserRepository
    ) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public List<ParsingGroupMember> findMembers(
            List<UserId> userIds
    ) {
        return appUserRepository.findAllByIds(userIds).stream()
                .map(user -> new ParsingGroupMember(
                        user.id(),
                        user.displayName()
                ))
                .toList();
    }
}
