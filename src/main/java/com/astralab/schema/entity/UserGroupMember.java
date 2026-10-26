package com.astralab.schema.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_group_member")
public class UserGroupMember {

    @EmbeddedId
    private UserGroupMemberId id = new UserGroupMemberId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private UserGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "added_at")
    private OffsetDateTime addedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_user_id")
    private AppUser addedByUser;

    public UserGroupMember() {
    }

    public UserGroupMember(UserGroup group, AppUser user) {
        this.group = group;
        this.user = user;
        this.id = new UserGroupMemberId(group != null ? group.getId() : null, user != null ? user.getId() : null);
    }

    public UserGroupMemberId getId() {
        return id;
    }

    public void setId(UserGroupMemberId id) {
        this.id = id;
    }

    public UserGroup getGroup() {
        return group;
    }

    public void setGroup(UserGroup group) {
        this.group = group;
        if (group != null) {
            this.id.setGroupId(group.getId());
        }
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
        if (user != null) {
            this.id.setUserId(user.getId());
        }
    }

    public OffsetDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(OffsetDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public AppUser getAddedByUser() {
        return addedByUser;
    }

    public void setAddedByUser(AppUser addedByUser) {
        this.addedByUser = addedByUser;
    }
}
