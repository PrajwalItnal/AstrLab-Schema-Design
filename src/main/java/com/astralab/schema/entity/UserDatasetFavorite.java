package com.astralab.schema.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_dataset_favorite")
public class UserDatasetFavorite {

    @EmbeddedId
    private UserDatasetFavoriteId id = new UserDatasetFavoriteId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("datasetId")
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public UserDatasetFavorite() {
    }

    public UserDatasetFavorite(AppUser user, Dataset dataset) {
        this.user = user;
        this.dataset = dataset;
        this.id = new UserDatasetFavoriteId(user != null ? user.getId() : null, dataset != null ? dataset.getId() : null);
    }

    public UserDatasetFavoriteId getId() {
        return id;
    }

    public void setId(UserDatasetFavoriteId id) {
        this.id = id;
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

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
        if (dataset != null) {
            this.id.setDatasetId(dataset.getId());
        }
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
