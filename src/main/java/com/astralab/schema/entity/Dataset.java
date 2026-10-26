package com.astralab.schema.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dataset")
public class Dataset {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private AppUser ownerUser;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "current_version")
    private Integer currentVersion;

    @Column(name = "visibility", length = 50)
    private String visibility;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    // Relationships
    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DatasetVersion> versions = new ArrayList<>();

    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DatasetUserAccess> userAccessList = new ArrayList<>();

    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DatasetTeamAccess> teamAccessList = new ArrayList<>();

    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DatasetGroupAccess> groupAccessList = new ArrayList<>();

    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DatasetOwnerHistory> ownerHistories = new ArrayList<>();

    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DatasetTagMapping> tagMappings = new ArrayList<>();

    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserDatasetFavorite> favoritedBy = new ArrayList<>();

    public Dataset() {
    }

    public Dataset(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public AppUser getOwnerUser() {
        return ownerUser;
    }

    public void setOwnerUser(AppUser ownerUser) {
        this.ownerUser = ownerUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(Integer currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public List<DatasetVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<DatasetVersion> versions) {
        this.versions = versions;
    }

    public List<DatasetUserAccess> getUserAccessList() {
        return userAccessList;
    }

    public void setUserAccessList(List<DatasetUserAccess> userAccessList) {
        this.userAccessList = userAccessList;
    }

    public List<DatasetTeamAccess> getTeamAccessList() {
        return teamAccessList;
    }

    public void setTeamAccessList(List<DatasetTeamAccess> teamAccessList) {
        this.teamAccessList = teamAccessList;
    }

    public List<DatasetGroupAccess> getGroupAccessList() {
        return groupAccessList;
    }

    public void setGroupAccessList(List<DatasetGroupAccess> groupAccessList) {
        this.groupAccessList = groupAccessList;
    }

    public List<DatasetOwnerHistory> getOwnerHistories() {
        return ownerHistories;
    }

    public void setOwnerHistories(List<DatasetOwnerHistory> ownerHistories) {
        this.ownerHistories = ownerHistories;
    }

    public List<DatasetTagMapping> getTagMappings() {
        return tagMappings;
    }

    public void setTagMappings(List<DatasetTagMapping> tagMappings) {
        this.tagMappings = tagMappings;
    }

    public List<UserDatasetFavorite> getFavoritedBy() {
        return favoritedBy;
    }

    public void setFavoritedBy(List<UserDatasetFavorite> favoritedBy) {
        this.favoritedBy = favoritedBy;
    }
}
