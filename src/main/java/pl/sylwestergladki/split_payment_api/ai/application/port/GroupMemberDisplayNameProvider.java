package pl.sylwestergladki.split_payment_api.ai.application.port;

import pl.sylwestergladki.split_payment_api.ai.application.model.ParsingGroupMember;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import java.util.List;

public interface GroupMemberDisplayNameProvider {

    List<ParsingGroupMember> findMembers(List<UserId> userIds);
}