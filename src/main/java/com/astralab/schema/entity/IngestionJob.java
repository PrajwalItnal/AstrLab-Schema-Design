package com.astralab.schema.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ingestion_job")
public class IngestionJob {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_version_id")
    private DatasetVersion datasetVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_file_id")
    private DatasetFile datasetFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_user_id")
    private AppUser submittedByUser;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "processor_type", length = 100)
    private String processorType;

    @Column(name = "external_job_id")
    private String externalJobId;

    @Column(name = "attempt_number")
    private Integer attemptNumber;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    // Relationships
    // INGESTION_JOB ||--o| INGESTION_METRICS : generates
    @OneToOne(mappedBy = "ingestionJob", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private IngestionMetrics metrics;

    // INGESTION_JOB ||--o{ INGESTION_STATUS_HISTORY : records
    @OneToMany(mappedBy = "ingestionJob", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IngestionStatusHistory> statusHistories = new ArrayList<>();

    // INGESTION_JOB ||--o{ COLUMN_PROFILE : produces
    @OneToMany(mappedBy = "ingestionJob", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ColumnProfile> columnProfiles = new ArrayList<>();

    public IngestionJob() {
    }

    public IngestionJob(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DatasetVersion getDatasetVersion() {
        return datasetVersion;
    }

    public void setDatasetVersion(DatasetVersion datasetVersion) {
        this.datasetVersion = datasetVersion;
    }

    public DatasetFile getDatasetFile() {
        return datasetFile;
    }

    public void setDatasetFile(DatasetFile datasetFile) {
        this.datasetFile = datasetFile;
    }

    public AppUser getSubmittedByUser() {
        return submittedByUser;
    }

    public void setSubmittedByUser(AppUser submittedByUser) {
        this.submittedByUser = submittedByUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcessorType() {
        return processorType;
    }

    public void setProcessorType(String processorType) {
        this.processorType = processorType;
    }

    public String getExternalJobId() {
        return externalJobId;
    }

    public void setExternalJobId(String externalJobId) {
        this.externalJobId = externalJobId;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(OffsetDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public IngestionMetrics getMetrics() {
        return metrics;
    }

    public void setMetrics(IngestionMetrics metrics) {
        this.metrics = metrics;
    }

    public List<IngestionStatusHistory> getStatusHistories() {
        return statusHistories;
    }

    public void setStatusHistories(List<IngestionStatusHistory> statusHistories) {
        this.statusHistories = statusHistories;
    }

    public List<ColumnProfile> getColumnProfiles() {
        return columnProfiles;
    }

    public void setColumnProfiles(List<ColumnProfile> columnProfiles) {
        this.columnProfiles = columnProfiles;
    }
}
