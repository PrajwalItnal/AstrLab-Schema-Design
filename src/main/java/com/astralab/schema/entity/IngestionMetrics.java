package com.astralab.schema.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ingestion_metrics")
public class IngestionMetrics {

    @Id
    @Column(name = "ingestion_job_id", length = 64, nullable = false)
    private String ingestionJobId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "ingestion_job_id")
    private IngestionJob ingestionJob;

    @Column(name = "rows_read")
    private Long rowsRead;

    @Column(name = "rows_written")
    private Long rowsWritten;

    @Column(name = "rows_rejected")
    private Long rowsRejected;

    @Column(name = "duplicate_rows")
    private Long duplicateRows;

    @Column(name = "column_count")
    private Integer columnCount;

    @Column(name = "input_bytes")
    private Long inputBytes;

    @Column(name = "output_bytes")
    private Long outputBytes;

    @Column(name = "processing_duration_ms")
    private Long processingDurationMs;

    @Column(name = "delta_table_path", length = 1024)
    private String deltaTablePath;

    @Column(name = "captured_at")
    private OffsetDateTime capturedAt;

    public IngestionMetrics() {
    }

    public IngestionMetrics(String ingestionJobId) {
        this.ingestionJobId = ingestionJobId;
    }

    public String getIngestionJobId() {
        return ingestionJobId;
    }

    public void setIngestionJobId(String ingestionJobId) {
        this.ingestionJobId = ingestionJobId;
    }

    public IngestionJob getIngestionJob() {
        return ingestionJob;
    }

    public void setIngestionJob(IngestionJob ingestionJob) {
        this.ingestionJob = ingestionJob;
        if (ingestionJob != null) {
            this.ingestionJobId = ingestionJob.getId();
        }
    }

    public Long getRowsRead() {
        return rowsRead;
    }

    public void setRowsRead(Long rowsRead) {
        this.rowsRead = rowsRead;
    }

    public Long getRowsWritten() {
        return rowsWritten;
    }

    public void setRowsWritten(Long rowsWritten) {
        this.rowsWritten = rowsWritten;
    }

    public Long getRowsRejected() {
        return rowsRejected;
    }

    public void setRowsRejected(Long rowsRejected) {
        this.rowsRejected = rowsRejected;
    }

    public Long getDuplicateRows() {
        return duplicateRows;
    }

    public void setDuplicateRows(Long duplicateRows) {
        this.duplicateRows = duplicateRows;
    }

    public Integer getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(Integer columnCount) {
        this.columnCount = columnCount;
    }

    public Long getInputBytes() {
        return inputBytes;
    }

    public void setInputBytes(Long inputBytes) {
        this.inputBytes = inputBytes;
    }

    public Long getOutputBytes() {
        return outputBytes;
    }

    public void setOutputBytes(Long outputBytes) {
        this.outputBytes = outputBytes;
    }

    public Long getProcessingDurationMs() {
        return processingDurationMs;
    }

    public void setProcessingDurationMs(Long processingDurationMs) {
        this.processingDurationMs = processingDurationMs;
    }

    public String getDeltaTablePath() {
        return deltaTablePath;
    }

    public void setDeltaTablePath(String deltaTablePath) {
        this.deltaTablePath = deltaTablePath;
    }

    public OffsetDateTime getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(OffsetDateTime capturedAt) {
        this.capturedAt = capturedAt;
    }
}
