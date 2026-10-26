package com.astralab.schema.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dataset_file")
public class DatasetFile {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_version_id")
    private DatasetVersion datasetVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private AppUser uploadedByUser;

    @Column(name = "original_filename", length = 512)
    private String originalFilename;

    @Column(name = "file_format", length = 50)
    private String fileFormat;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "storage_bucket")
    private String storageBucket;

    @Column(name = "object_key", length = 1024)
    private String objectKey;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "checksum_algorithm", length = 50)
    private String checksumAlgorithm;

    @Column(name = "uploaded_at")
    private OffsetDateTime uploadedAt;

    // Relationships
    @OneToMany(mappedBy = "datasetFile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IngestionJob> ingestionJobs = new ArrayList<>();

    public DatasetFile() {
    }

    public DatasetFile(String id) {
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

    public AppUser getUploadedByUser() {
        return uploadedByUser;
    }

    public void setUploadedByUser(AppUser uploadedByUser) {
        this.uploadedByUser = uploadedByUser;
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

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getStorageBucket() {
        return storageBucket;
    }

    public void setStorageBucket(String storageBucket) {
        this.storageBucket = storageBucket;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getChecksumAlgorithm() {
        return checksumAlgorithm;
    }

    public void setChecksumAlgorithm(String checksumAlgorithm) {
        this.checksumAlgorithm = checksumAlgorithm;
    }

    public OffsetDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(OffsetDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public List<IngestionJob> getIngestionJobs() {
        return ingestionJobs;
    }

    public void setIngestionJobs(List<IngestionJob> ingestionJobs) {
        this.ingestionJobs = ingestionJobs;
    }
}
