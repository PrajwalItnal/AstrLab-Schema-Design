package com.astralab.schema.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class DatasetTagMappingId implements Serializable {

    @Column(name = "dataset_id", length = 64)
    private String datasetId;

    @Column(name = "tag_id", length = 64)
    private String tagId;

    public DatasetTagMappingId() {
    }

    public DatasetTagMappingId(String datasetId, String tagId) {
        this.datasetId = datasetId;
        this.tagId = tagId;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetTagMappingId that = (DatasetTagMappingId) o;
        return Objects.equals(datasetId, that.datasetId) && Objects.equals(tagId, that.tagId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datasetId, tagId);
    }
}
