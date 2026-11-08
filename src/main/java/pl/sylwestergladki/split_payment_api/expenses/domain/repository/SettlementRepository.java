package pl.sylwestergladki.split_payment_api.expenses.domain.repository;

import pl.sylwestergladki.split_payment_api.expenses.domain.model.Settlement;
import pl.sylwestergladki.split_payment_api.groups.domain.GroupId;

import java.util.List;

public interface SettlementRepository {

    Settlement save(Settlement settlement);

    List<Settlement> findAllByGroupId(GroupId groupId);
}