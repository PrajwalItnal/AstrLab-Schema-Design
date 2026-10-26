package com.astralab.schema.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dataset_version")
public class DatasetVersion {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    private Dataset dataset;

    @Column(name = "version_number")
    private Integer versionNumber;

    @Column(name = "schema_hash")
    private String schemaHash;

    @Column(name = "status", length = 50)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private AppUser createdByUser;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    // Relationships
    @OneToMany(mappedBy = "datasetVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DatasetFile> files = new ArrayList<>();

    @OneToMany(mappedBy = "datasetVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DatasetColumn> columns = new ArrayList<>();

    @OneToMany(mappedBy = "datasetVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IngestionJob> ingestionJobs = new ArrayList<>();

    public DatasetVersion() {
    }

    public DatasetVersion(String id) {
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

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getSchemaHash() {
        return schemaHash;
    }

    public void setSchemaHash(String schemaHash) {
        this.schemaHash = schemaHash;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AppUser getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(AppUser createdByUser) {
        this.createdByUser = createdByUser;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<DatasetFile> getFiles() {
        return files;
    }

    public void setFiles(List<DatasetFile> files) {
        this.files = files;
    }

    public List<DatasetColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<DatasetColumn> columns) {
        this.columns = columns;
    }

    public List<IngestionJob> getIngestionJobs() {
        return ingestionJobs;
    }

    public void setIngestionJobs(List<IngestionJob> ingestionJobs) {
        this.ingestionJobs = ingestionJobs;
    }
}
