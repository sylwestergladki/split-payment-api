package pl.sylwestergladki.split_payment_api.groups.infrastructure.persistance;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "group_members")
@IdClass(GroupMemberId.class)
public class GroupMemberEntity {

    @Id
    @Column(name = "group_id")
    private UUID groupId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private GroupEntity group;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    protected GroupMemberEntity() {
    }

    public GroupMemberEntity(UUID userId, Instant joinedAt) {
        this.userId = userId;
        this.joinedAt = joinedAt;
    }

    public void setGroup(GroupEntity group) {
        this.group = group;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
