package com.astralab.schema.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserDatasetFavoriteId implements Serializable {

    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "dataset_id", length = 64)
    private String datasetId;

    public UserDatasetFavoriteId() {
    }

    public UserDatasetFavoriteId(String userId, String datasetId) {
        this.userId = userId;
        this.datasetId = datasetId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDatasetFavoriteId that = (UserDatasetFavoriteId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(datasetId, that.datasetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, datasetId);
    }
}
