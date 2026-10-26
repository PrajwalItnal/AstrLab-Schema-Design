package com.astralab.schema.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "dataset_owner_history")
public class DatasetOwnerHistory {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_owner_user_id")
    private AppUser previousOwnerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_owner_user_id")
    private AppUser newOwnerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private AppUser changedByUser;

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @Column(name = "changed_at")
    private OffsetDateTime changedAt;

    public DatasetOwnerHistory() {
    }

    public DatasetOwnerHistory(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public AppUser getPreviousOwnerUser() {
        return previousOwnerUser;
    }

    public void setPreviousOwnerUser(AppUser previousOwnerUser) {
        this.previousOwnerUser = previousOwnerUser;
    }

    public AppUser getNewOwnerUser() {
        return newOwnerUser;
    }

    public void setNewOwnerUser(AppUser newOwnerUser) {
        this.newOwnerUser = newOwnerUser;
    }

    public AppUser getChangedByUser() {
        return changedByUser;
    }

    public void setChangedByUser(AppUser changedByUser) {
        this.changedByUser = changedByUser;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(OffsetDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
