package com.astralab.schema.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "column_profile")
public class ColumnProfile {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingestion_job_id")
    private IngestionJob ingestionJob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_column_id")
    private DatasetColumn datasetColumn;

    @Column(name = "null_count")
    private Long nullCount;

    @Column(name = "distinct_count")
    private Long distinctCount;

    @Column(name = "min_value", columnDefinition = "TEXT")
    private String minValue;

    @Column(name = "max_value", columnDefinition = "TEXT")
    private String maxValue;

    @Column(name = "mean_value", precision = 38, scale = 10)
    private BigDecimal meanValue;

    @Column(name = "standard_deviation", precision = 38, scale = 10)
    private BigDecimal standardDeviation;

    @Column(name = "profiled_at")
    private OffsetDateTime profiledAt;

    public ColumnProfile() {
    }

    public ColumnProfile(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public IngestionJob getIngestionJob() {
        return ingestionJob;
    }

    public void setIngestionJob(IngestionJob ingestionJob) {
        this.ingestionJob = ingestionJob;
    }

    public DatasetColumn getDatasetColumn() {
        return datasetColumn;
    }

    public void setDatasetColumn(DatasetColumn datasetColumn) {
        this.datasetColumn = datasetColumn;
    }

    public Long getNullCount() {
        return nullCount;
    }

    public void setNullCount(Long nullCount) {
        this.nullCount = nullCount;
    }

    public Long getDistinctCount() {
        return distinctCount;
    }

    public void setDistinctCount(Long distinctCount) {
        this.distinctCount = distinctCount;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public BigDecimal getMeanValue() {
        return meanValue;
    }

    public void setMeanValue(BigDecimal meanValue) {
        this.meanValue = meanValue;
    }

    public BigDecimal getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(BigDecimal standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public OffsetDateTime getProfiledAt() {
        return profiledAt;
    }

    public void setProfiledAt(OffsetDateTime profiledAt) {
        this.profiledAt = profiledAt;
    }
}
