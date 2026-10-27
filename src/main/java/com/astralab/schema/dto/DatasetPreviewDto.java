package com.astralab.schema.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatasetPreviewDto {
    private String datasetId;
    private String datasetName;
    private String description;
    private String zone;
    private String owner;
    private Integer versionNumber;
    private String originalFilename;
    private String fileFormat;
    private Long fileSizeBytes;
    private Integer totalColumns;
    private Integer sampleRowCount;
    private List<ColumnSchemaDto> columns = new ArrayList<>();
    private List<Map<String, Object>> rows = new ArrayList<>();

    public DatasetPreviewDto() {
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public void setFileFormat(String fileFormat) {
        this.fileFormat = fileFormat;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public Integer getTotalColumns() {
        return totalColumns;
    }

    public void setTotalColumns(Integer totalColumns) {
        this.totalColumns = totalColumns;
    }

    public Integer getSampleRowCount() {
        return sampleRowCount;
    }

    public void setSampleRowCount(Integer sampleRowCount) {
        this.sampleRowCount = sampleRowCount;
    }

    public List<ColumnSchemaDto> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnSchemaDto> columns) {
        this.columns = columns;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }
}
