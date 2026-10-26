package com.astralab.schema.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "keycloak_id", unique = true)
    private String keycloakId;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "avatar", length = 512)
    private String avatar;

    @Column(name = "designation")
    private String designation;

    @Column(name = "department")
    private String department;

    @Column(name = "status", length = 50)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    // Relationships from ERD:
    // APP_USER ||--o{ USER_GROUP_MEMBER : belongs_to
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserGroupMember> groupMemberships = new ArrayList<>();

    // APP_USER ||--o{ WORKSPACE_MEMBER : joins
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkspaceMember> workspaceMemberships = new ArrayList<>();

    // APP_USER ||--o{ WORKSPACE : creates
    @OneToMany(mappedBy = "createdByUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Workspace> createdWorkspaces = new ArrayList<>();

    // APP_USER ||--o{ DATASET : owns
    @OneToMany(mappedBy = "ownerUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Dataset> ownedDatasets = new ArrayList<>();

    // APP_USER ||--o{ DATASET_VERSION : creates
    @OneToMany(mappedBy = "createdByUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DatasetVersion> createdDatasetVersions = new ArrayList<>();

    // APP_USER ||--o{ DATASET_FILE : uploads
    @OneToMany(mappedBy = "uploadedByUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DatasetFile> uploadedDatasetFiles = new ArrayList<>();

    // APP_USER ||--o{ INGESTION_JOB : submits
    @OneToMany(mappedBy = "submittedByUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IngestionJob> submittedIngestionJobs = new ArrayList<>();

    // APP_USER ||--o{ INGESTION_STATUS_HISTORY : changes
    @OneToMany(mappedBy = "changedByUser", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IngestionStatusHistory> statusHistoryChanges = new ArrayList<>();

    // APP_USER ||--o{ DATASET_USER_ACCESS : receives_access
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DatasetUserAccess> receivedUserAccesses = new ArrayList<>();

    // APP_USER ||--o{ USER_DATASET_FAVORITE : bookmarks
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserDatasetFavorite> favoriteDatasets = new ArrayList<>();

    public AppUser() {
    }

    public AppUser(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKeycloakId() {
        return keycloakId;
    }

    public void setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(OffsetDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public List<UserGroupMember> getGroupMemberships() {
        return groupMemberships;
    }

    public void setGroupMemberships(List<UserGroupMember> groupMemberships) {
        this.groupMemberships = groupMemberships;
    }

    public List<WorkspaceMember> getWorkspaceMemberships() {
        return workspaceMemberships;
    }

    public void setWorkspaceMemberships(List<WorkspaceMember> workspaceMemberships) {
        this.workspaceMemberships = workspaceMemberships;
    }

    public List<Workspace> getCreatedWorkspaces() {
        return createdWorkspaces;
    }

    public void setCreatedWorkspaces(List<Workspace> createdWorkspaces) {
        this.createdWorkspaces = createdWorkspaces;
    }

    public List<Dataset> getOwnedDatasets() {
        return ownedDatasets;
    }

    public void setOwnedDatasets(List<Dataset> ownedDatasets) {
        this.ownedDatasets = ownedDatasets;
    }

    public List<DatasetVersion> getCreatedDatasetVersions() {
        return createdDatasetVersions;
    }

    public void setCreatedDatasetVersions(List<DatasetVersion> createdDatasetVersions) {
        this.createdDatasetVersions = createdDatasetVersions;
    }

    public List<DatasetFile> getUploadedDatasetFiles() {
        return uploadedDatasetFiles;
    }

    public void setUploadedDatasetFiles(List<DatasetFile> uploadedDatasetFiles) {
        this.uploadedDatasetFiles = uploadedDatasetFiles;
    }

    public List<IngestionJob> getSubmittedIngestionJobs() {
        return submittedIngestionJobs;
    }

    public void setSubmittedIngestionJobs(List<IngestionJob> submittedIngestionJobs) {
        this.submittedIngestionJobs = submittedIngestionJobs;
    }

    public List<IngestionStatusHistory> getStatusHistoryChanges() {
        return statusHistoryChanges;
    }

    public void setStatusHistoryChanges(List<IngestionStatusHistory> statusHistoryChanges) {
        this.statusHistoryChanges = statusHistoryChanges;
    }

    public List<DatasetUserAccess> getReceivedUserAccesses() {
        return receivedUserAccesses;
    }

    public void setReceivedUserAccesses(List<DatasetUserAccess> receivedUserAccesses) {
        this.receivedUserAccesses = receivedUserAccesses;
    }

    public List<UserDatasetFavorite> getFavoriteDatasets() {
        return favoriteDatasets;
    }

    public void setFavoriteDatasets(List<UserDatasetFavorite> favoriteDatasets) {
        this.favoriteDatasets = favoriteDatasets;
    }
}
