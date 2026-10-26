-- ===================================================================
-- Flyway Migration V1: Initial Database Schema
-- Implements exact schema from ERD (All 22 tables and relationships)
-- ===================================================================

-- 1. TENANT
CREATE TABLE IF NOT EXISTS tenant (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255),
    plan VARCHAR(100),
    environment VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE
);

-- 2. TEAM
CREATE TABLE IF NOT EXISTS team (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) REFERENCES tenant(id) ON DELETE CASCADE,
    name VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE
);

-- 3. APP_USER
CREATE TABLE IF NOT EXISTS app_user (
    id VARCHAR(64) PRIMARY KEY,
    keycloak_id VARCHAR(255) UNIQUE,
    username VARCHAR(255) UNIQUE,
    email VARCHAR(255),
    display_name VARCHAR(255),
    avatar VARCHAR(512),
    designation VARCHAR(255),
    department VARCHAR(255),
    status VARCHAR(50),
    tenant_id VARCHAR(64) REFERENCES tenant(id) ON DELETE SET NULL,
    team_id VARCHAR(64) REFERENCES team(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE
);

-- 4. USER_GROUP
CREATE TABLE IF NOT EXISTS user_group (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) UNIQUE,
    name VARCHAR(255),
    tenant_id VARCHAR(64) REFERENCES tenant(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE
);

-- 5. USER_GROUP_MEMBER (Composite PK: group_id, user_id)
CREATE TABLE IF NOT EXISTS user_group_member (
    group_id BIGINT REFERENCES user_group(id) ON DELETE CASCADE,
    user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE CASCADE,
    added_at TIMESTAMP WITH TIME ZONE,
    added_by_user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE SET NULL,
    PRIMARY KEY (group_id, user_id)
);

-- 6. WORKSPACE
CREATE TABLE IF NOT EXISTS workspace (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) REFERENCES tenant(id) ON DELETE CASCADE,
    name VARCHAR(255),
    description VARCHAR(1000),
    status VARCHAR(50),
    created_by_user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- 7. WORKSPACE_MEMBER
CREATE TABLE IF NOT EXISTS workspace_member (
    id BIGSERIAL PRIMARY KEY,
    workspace_id VARCHAR(64) REFERENCES workspace(id) ON DELETE CASCADE,
    user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE CASCADE,
    workspace_role VARCHAR(100),
    is_default BOOLEAN,
    joined_at TIMESTAMP WITH TIME ZONE,
    added_by_user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE SET NULL
);

-- 8. DATASET
CREATE TABLE IF NOT EXISTS dataset (
    id VARCHAR(64) PRIMARY KEY,
    workspace_id VARCHAR(64) REFERENCES workspace(id) ON DELETE CASCADE,
    owner_user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE SET NULL,
    name VARCHAR(255),
    description TEXT,
    status VARCHAR(50),
    current_version INT,
    visibility VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- 9. DATASET_VERSION
CREATE TABLE IF NOT EXISTS dataset_version (
    id VARCHAR(64) PRIMARY KEY,
    dataset_id VARCHAR(64) REFERENCES dataset(id) ON DELETE CASCADE,
    version_number INT,
    schema_hash VARCHAR(255),
    status VARCHAR(50),
    created_by_user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE
);

-- 10. DATASET_FILE
CREATE TABLE IF NOT EXISTS dataset_file (
    id VARCHAR(64) PRIMARY KEY,
    dataset_version_id VARCHAR(64) REFERENCES dataset_version(id) ON DELETE CASCADE,
    uploaded_by_user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE SET NULL,
    original_filename VARCHAR(512),
    file_format VARCHAR(50),
    mime_type VARCHAR(100),
    file_size_bytes BIGINT,
    storage_bucket VARCHAR(255),
    object_key VARCHAR(1024),
    checksum VARCHAR(255),
    checksum_algorithm VARCHAR(50),
    uploaded_at TIMESTAMP WITH TIME ZONE
);

-- 11. DATASET_COLUMN
CREATE TABLE IF NOT EXISTS dataset_column (
    id VARCHAR(64) PRIMARY KEY,
    dataset_version_id VARCHAR(64) REFERENCES dataset_version(id) ON DELETE CASCADE,
    column_name VARCHAR(255),
    source_data_type VARCHAR(100),
    logical_data_type VARCHAR(100),
    ordinal_position INT,
    nullable BOOLEAN,
    precision_value INT,
    scale_value INT,
    format_pattern VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE
);

-- 12. INGESTION_JOB
CREATE TABLE IF NOT EXISTS ingestion_job (
    id VARCHAR(64) PRIMARY KEY,
    dataset_version_id VARCHAR(64) REFERENCES dataset_version(id) ON DELETE CASCADE,
    dataset_file_id VARCHAR(64) REFERENCES dataset_file(id) ON DELETE SET NULL,
    submitted_by_user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE SET NULL,
    status VARCHAR(50),
    processor_type VARCHAR(100),
    external_job_id VARCHAR(255),
    attempt_number INT,
    submitted_at TIMESTAMP WITH TIME ZONE,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    error_code VARCHAR(100),
    error_message TEXT
);

-- 13. INGESTION_METRICS (1-to-1 with INGESTION_JOB)
CREATE TABLE IF NOT EXISTS ingestion_metrics (
    ingestion_job_id VARCHAR(64) PRIMARY KEY REFERENCES ingestion_job(id) ON DELETE CASCADE,
    rows_read BIGINT,
    rows_written BIGINT,
    rows_rejected BIGINT,
    duplicate_rows BIGINT,
    column_count INT,
    input_bytes BIGINT,
    output_bytes BIGINT,
    processing_duration_ms BIGINT,
    delta_table_path VARCHAR(1024),
    captured_at TIMESTAMP WITH TIME ZONE
);

-- 14. INGESTION_STATUS_HISTORY
CREATE TABLE IF NOT EXISTS ingestion_status_history (
    id BIGSERIAL PRIMARY KEY,
    ingestion_job_id VARCHAR(64) REFERENCES ingestion_job(id) ON DELETE CASCADE,
    from_status VARCHAR(50),
    to_status VARCHAR(50),
    message TEXT,
    changed_by_user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE SET NULL,
    changed_at TIMESTAMP WITH TIME ZONE
);

-- 15. COLUMN_PROFILE
CREATE TABLE IF NOT EXISTS column_profile (
    id VARCHAR(64) PRIMARY KEY,
    ingestion_job_id VARCHAR(64) REFERENCES ingestion_job(id) ON DELETE CASCADE,
    dataset_column_id VARCHAR(64) REFERENCES dataset_column(id) ON DELETE CASCADE,
    null_count BIGINT,
    distinct_count BIGINT,
    min_value TEXT,
    max_value TEXT,
    mean_value DECIMAL(38, 10),
    standard_deviation DECIMAL(38, 10),
    profiled_at TIMESTAMP WITH TIME ZONE
);

-- 16. DATASET_USER_ACCESS
CREATE TABLE IF NOT EXISTS dataset_user_access (
    id VARCHAR(64) PRIMARY KEY,
    dataset_id VARCHAR(64) REFERENCES dataset(id) ON DELETE CASCADE,
    user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE CASCADE,
    access_level VARCHAR(50),
    can_share BOOLEAN,
    can_download BOOLEAN,
    can_upload_new_version BOOLEAN,
    can_delete BOOLEAN,
    granted_by_user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE SET NULL,
    granted_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    revoked_at TIMESTAMP WITH TIME ZONE
);

-- 17. DATASET_TEAM_ACCESS
CREATE TABLE IF NOT EXISTS dataset_team_access (
    id VARCHAR(64) PRIMARY KEY,
    dataset_id VARCHAR(64) REFERENCES dataset(id) ON DELETE CASCADE,
    team_id VARCHAR(64) REFERENCES team(id) ON DELETE CASCADE,
    access_level VARCHAR(50),
    can_share BOOLEAN,
    can_download BOOLEAN,
    can_upload_new_version BOOLEAN,
    can_delete BOOLEAN,
    granted_by_user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE SET NULL,
    granted_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    revoked_at TIMESTAMP WITH TIME ZONE
);

-- 18. DATASET_GROUP_ACCESS
CREATE TABLE IF NOT EXISTS dataset_group_access (
    id VARCHAR(64) PRIMARY KEY,
    dataset_id VARCHAR(64) REFERENCES dataset(id) ON DELETE CASCADE,
    group_id BIGINT REFERENCES user_group(id) ON DELETE CASCADE,
    access_level VARCHAR(50),
    can_share BOOLEAN,
    can_download BOOLEAN,
    can_upload_new_version BOOLEAN,
    can_delete BOOLEAN,
    granted_by_user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE SET NULL,
    granted_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    revoked_at TIMESTAMP WITH TIME ZONE
);

-- 19. DATASET_OWNER_HISTORY
CREATE TABLE IF NOT EXISTS dataset_owner_history (
    id VARCHAR(64) PRIMARY KEY,
    dataset_id VARCHAR(64) REFERENCES dataset(id) ON DELETE CASCADE,
    previous_owner_user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE SET NULL,
    new_owner_user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE SET NULL,
    changed_by_user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE SET NULL,
    change_reason VARCHAR(500),
    changed_at TIMESTAMP WITH TIME ZONE
);

-- 20. DATASET_TAG
CREATE TABLE IF NOT EXISTS dataset_tag (
    id VARCHAR(64) PRIMARY KEY,
    tenant_id VARCHAR(64) REFERENCES tenant(id) ON DELETE CASCADE,
    name VARCHAR(255),
    color VARCHAR(50),
    created_by_user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE
);

-- 21. DATASET_TAG_MAPPING (Composite PK: dataset_id, tag_id)
CREATE TABLE IF NOT EXISTS dataset_tag_mapping (
    dataset_id VARCHAR(64) REFERENCES dataset(id) ON DELETE CASCADE,
    tag_id VARCHAR(64) REFERENCES dataset_tag(id) ON DELETE CASCADE,
    assigned_by_user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE SET NULL,
    assigned_at TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (dataset_id, tag_id)
);

-- 22. USER_DATASET_FAVORITE (Composite PK: user_id, dataset_id)
CREATE TABLE IF NOT EXISTS user_dataset_favorite (
    user_id VARCHAR(64) REFERENCES app_user(id) ON DELETE CASCADE,
    dataset_id VARCHAR(64) REFERENCES dataset(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (user_id, dataset_id)
);
