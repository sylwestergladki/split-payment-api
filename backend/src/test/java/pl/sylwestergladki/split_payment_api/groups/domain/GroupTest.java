package pl.sylwestergladki.split_payment_api.groups.domain;


import org.junit.jupiter.api.Test;
import pl.sylwestergladki.split_payment_api.identity.domain.UserId;

import static org.assertj.core.api.Assertions.*;

class GroupTest {

    @Test
    void shouldCreateGroupWithCreatorAsFirstMember() {
        UserId creator = UserId.generate();

        Group group = Group.create("Mountain trip", creator);

        assertThat(group.name()).isEqualTo("Mountain trip");
        assertThat(group.createdBy()).isEqualTo(creator);
        assertThat(group.members()).hasSize(1);
        assertThat(group.hasMember(creator)).isTrue();
    }

    @Test
    void shouldAddMemberToGroup() {
        UserId creator = UserId.generate();
        UserId anotherUser = UserId.generate();

        Group group = Group.create("Mountain trip", creator);

        group.addMember(anotherUser);

        assertThat(group.members()).hasSize(2);
        assertThat(group.hasMember(anotherUser)).isTrue();
    }

    @Test
    void shouldNotAddSameMemberTwice() {
        UserId creator = UserId.generate();

        Group group = Group.create("Mountain trip", creator);

        assertThatThrownBy(() -> group.addMember(creator))
                .isInstanceOf(IllegalArgumentException.class);
    }
}