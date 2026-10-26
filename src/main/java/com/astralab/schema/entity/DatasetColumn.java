package com.astralab.schema.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dataset_column")
public class DatasetColumn {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_version_id")
    private DatasetVersion datasetVersion;

    @Column(name = "column_name")
    private String columnName;

    @Column(name = "source_data_type", length = 100)
    private String sourceDataType;

    @Column(name = "logical_data_type", length = 100)
    private String logicalDataType;

    @Column(name = "ordinal_position")
    private Integer ordinalPosition;

    @Column(name = "nullable")
    private Boolean nullable;

    @Column(name = "precision_value")
    private Integer precisionValue;

    @Column(name = "scale_value")
    private Integer scaleValue;

    @Column(name = "format_pattern")
    private String formatPattern;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    // Relationships
    @OneToMany(mappedBy = "datasetColumn", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ColumnProfile> columnProfiles = new ArrayList<>();

    public DatasetColumn() {
    }

    public DatasetColumn(String id) {
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

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getSourceDataType() {
        return sourceDataType;
    }

    public void setSourceDataType(String sourceDataType) {
        this.sourceDataType = sourceDataType;
    }

    public String getLogicalDataType() {
        return logicalDataType;
    }

    public void setLogicalDataType(String logicalDataType) {
        this.logicalDataType = logicalDataType;
    }

    public Integer getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(Integer ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    public Boolean getNullable() {
        return nullable;
    }

    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    public Integer getPrecisionValue() {
        return precisionValue;
    }

    public void setPrecisionValue(Integer precisionValue) {
        this.precisionValue = precisionValue;
    }

    public Integer getScaleValue() {
        return scaleValue;
    }

    public void setScaleValue(Integer scaleValue) {
        this.scaleValue = scaleValue;
    }

    public String getFormatPattern() {
        return formatPattern;
    }

    public void setFormatPattern(String formatPattern) {
        this.formatPattern = formatPattern;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ColumnProfile> getColumnProfiles() {
        return columnProfiles;
    }

    public void setColumnProfiles(List<ColumnProfile> columnProfiles) {
        this.columnProfiles = columnProfiles;
    }
}
