package com.astralab.schema.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "dataset_tag_mapping")
public class DatasetTagMapping {

    @EmbeddedId
    private DatasetTagMappingId id = new DatasetTagMappingId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("datasetId")
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private DatasetTag tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id")
    private AppUser assignedByUser;

    @Column(name = "assigned_at")
    private OffsetDateTime assignedAt;

    public DatasetTagMapping() {
    }

    public DatasetTagMapping(Dataset dataset, DatasetTag tag) {
        this.dataset = dataset;
        this.tag = tag;
        this.id = new DatasetTagMappingId(dataset != null ? dataset.getId() : null, tag != null ? tag.getId() : null);
    }

    public DatasetTagMappingId getId() {
        return id;
    }

    public void setId(DatasetTagMappingId id) {
        this.id = id;
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

    public DatasetTag getTag() {
        return tag;
    }

    public void setTag(DatasetTag tag) {
        this.tag = tag;
        if (tag != null) {
            this.id.setTagId(tag.getId());
        }
    }

    public AppUser getAssignedByUser() {
        return assignedByUser;
    }

    public void setAssignedByUser(AppUser assignedByUser) {
        this.assignedByUser = assignedByUser;
    }

    public OffsetDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(OffsetDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
