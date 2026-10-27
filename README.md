# AstraLab Schema Designing - Spring Boot JPA Database Schema

This Spring Boot application implements the exact database schema specified in the ERD diagram (`svgviewer-output.svg`), covering all tables, columns, primary keys, composite keys, foreign keys, unique constraints, and relationships across both diagram pages.

## Architecture & ERD Analysis

The input diagram contains two logical pages embedded within the Draw.io / Mermaid specification:

1. **Page 1: Data Ingestion Metadata ERD**
   - Focuses on the core 9 ingestion tables: `WORKSPACE`, `DATASET`, `DATASET_VERSION`, `DATASET_FILE`, `DATASET_COLUMN`, `INGESTION_JOB`, `INGESTION_METRICS`, `INGESTION_STATUS_HISTORY`, and `COLUMN_PROFILE`.
   - Represents the isolated ingestion subsystem prior to enterprise identity and tenancy integration.

2. **Page 2: Full Enterprise Schema (Page-2)**
   - Unifies and expands all 9 ingestion tables from Page 1 with Identity, Tenancy, Access Control, Tagging, and Favorites into **22 cohesive tables**.
   - Foreign key relationships to `APP_USER` and `TENANT` normalize the `created_by`, `uploaded_by`, `submitted_by`, and `changed_by` references into relational foreign keys (`owner_user_id`, `created_by_user_id`, `uploaded_by_user_id`, `submitted_by_user_id`, `changed_by_user_id`, `tenant_id`).
   - Adds enterprise columns (`status` on `DATASET_VERSION`, `visibility` and `deleted_at` on `DATASET`, `checksum_algorithm` on `DATASET_FILE`, `description` and `created_at` on `DATASET_COLUMN`, and `standard_deviation` on `COLUMN_PROFILE`).

---

## Complete Table Catalog (22 Tables)

### 1. Identity & Organization
| Table | Primary Key | Foreign Keys / Constraints | Description |
| :--- | :--- | :--- | :--- |
| `tenant` | `id` (VARCHAR) | - | Multi-tenant organization boundaries |
| `team` | `id` (VARCHAR) | `tenant_id` -> `tenant(id)` | Teams within a tenant |
| `app_user` | `id` (VARCHAR) | `tenant_id` -> `tenant(id)`, `team_id` -> `team(id)`, UK(`keycloak_id`), UK(`username`) | System users and identity |
| `user_group` | `id` (BIGINT) | `tenant_id` -> `tenant(id)`, UK(`code`) | User permission groups |
| `user_group_member` | `(group_id, user_id)` (Composite) | `group_id` -> `user_group(id)`, `user_id` -> `app_user(id)`, `added_by_user_id` -> `app_user(id)` | Group membership join table |
| `workspace` | `id` (VARCHAR) | `tenant_id` -> `tenant(id)`, `created_by_user_id` -> `app_user(id)` | Workspaces within tenants |
| `workspace_member` | `id` (BIGINT) | `workspace_id` -> `workspace(id)`, `user_id` -> `app_user(id)`, `added_by_user_id` -> `app_user(id)` | Workspace membership & roles |

### 2. Dataset Catalog
| Table | Primary Key | Foreign Keys / Constraints | Description |
| :--- | :--- | :--- | :--- |
| `dataset` | `id` (VARCHAR) | `workspace_id` -> `workspace(id)`, `owner_user_id` -> `app_user(id)` | Top-level datasets |
| `dataset_version` | `id` (VARCHAR) | `dataset_id` -> `dataset(id)`, `created_by_user_id` -> `app_user(id)` | Versioned schema and revisions |
| `dataset_file` | `id` (VARCHAR) | `dataset_version_id` -> `dataset_version(id)`, `uploaded_by_user_id` -> `app_user(id)` | Storage bucket file metadata & checksums |
| `dataset_column` | `id` (VARCHAR) | `dataset_version_id` -> `dataset_version(id)` | Column schema definitions, data types, precision |

### 3. Ingestion Execution & Profiling
| Table | Primary Key | Foreign Keys / Constraints | Description |
| :--- | :--- | :--- | :--- |
| `ingestion_job` | `id` (VARCHAR) | `dataset_version_id` -> `dataset_version(id)`, `dataset_file_id` -> `dataset_file(id)`, `submitted_by_user_id` -> `app_user(id)` | Ingestion job executions and attempt tracking |
| `ingestion_metrics` | `ingestion_job_id` (VARCHAR) | 1-to-1 with `ingestion_job(id)` (`@MapsId`) | Metrics: rows read/written/rejected, bytes, duration |
| `ingestion_status_history`| `id` (BIGINT) | `ingestion_job_id` -> `ingestion_job(id)`, `changed_by_user_id` -> `app_user(id)` | State transitions and audit trail |
| `column_profile` | `id` (VARCHAR) | `ingestion_job_id` -> `ingestion_job(id)`, `dataset_column_id` -> `dataset_column(id)` | Statistical profiling (nulls, distinct, min, max, mean, stddev) |

### 4. Sharing, Access Control, Ownership & Tagging
| Table | Primary Key | Foreign Keys / Constraints | Description |
| :--- | :--- | :--- | :--- |
| `dataset_user_access` | `id` (VARCHAR) | `dataset_id` -> `dataset(id)`, `user_id` -> `app_user(id)`, `granted_by_user_id` -> `app_user(id)` | Direct user-level access permissions |
| `dataset_team_access` | `id` (VARCHAR) | `dataset_id` -> `dataset(id)`, `team_id` -> `team(id)`, `granted_by_user_id` -> `app_user(id)` | Team-level access permissions |
| `dataset_group_access`| `id` (VARCHAR) | `dataset_id` -> `dataset(id)`, `group_id` -> `user_group(id)`, `granted_by_user_id` -> `app_user(id)` | Group-level access permissions |
| `dataset_owner_history`| `id` (VARCHAR) | `dataset_id` -> `dataset(id)`, `previous_owner_user_id` -> `app_user(id)`, `new_owner_user_id` -> `app_user(id)`, `changed_by_user_id` -> `app_user(id)` | Dataset ownership transfer audit log |
| `dataset_tag` | `id` (VARCHAR) | `tenant_id` -> `tenant(id)`, `created_by_user_id` -> `app_user(id)` | Categorization tags with color definitions |
| `dataset_tag_mapping` | `(dataset_id, tag_id)` (Composite) | `dataset_id` -> `dataset(id)`, `tag_id` -> `dataset_tag(id)`, `assigned_by_user_id` -> `app_user(id)` | Dataset-tag mappings |
| `user_dataset_favorite`| `(user_id, dataset_id)` (Composite) | `user_id` -> `app_user(id)`, `dataset_id` -> `dataset(id)` | User bookmarks and favorite datasets |

---

## Dataset Management & Schema Inference APIs

The application exposes REST APIs for dataset upload, persistent file storage, relational metadata registration, automated schema inference, low-memory preview streaming, and multi-criteria search.

### REST Endpoints
| HTTP Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/datasets/upload` | Multipart file upload (`.csv`, `.xlsx`, `.xls`), persistent storage, metadata registration in PostgreSQL, and automated schema inference |
| `GET` | `/api/v1/datasets` | List and search datasets with pagination; filter by `zone`, `owner`, and `search` keyword |
| `GET` | `/api/v1/datasets/{id}` | Retrieve dataset details, version history, storage details, and inferred schema columns |
| `GET` | `/api/v1/datasets/{id}/preview` | Stream sample preview rows (default: 10, max: 100) directly from disk without loading the whole file into memory |

---

## Technical Stack & Configuration

- **Java Version:** Java 25 (OpenJDK 25.0.3 LTS)
- **Spring Boot:** 3.4.2 (Spring MVC, Spring Data JPA, Hibernate ORM 6.6)
- **File Parsing & Streaming:** Apache Commons CSV (1.12.0) & Apache POI (5.4.0)
- **Database Support:** PostgreSQL & MySQL 8.0
- **Build Tool:** Apache Maven 3.9.9

### Configuration
- [application.properties](file:///c:/AstraLab%20Schema%20Designing/src/main/resources/application.properties): Configured for PostgreSQL with persistent upload storage directory (`app.storage.upload-dir`).
- [schema.sql](file:///c:/AstraLab%20Schema%20Designing/src/main/resources/schema.sql): PostgreSQL DDL reference including `zone` on `dataset`.
- [V1__initial_schema.sql](file:///c:/AstraLab%20Schema%20Designing/src/main/resources/db/migration/V1__initial_schema.sql): Flyway-compatible migration script.

---

## Verification & Tests

```powershell
# Set JAVA_HOME to Java 25
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.3"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

# Run all test suites
mvn clean test
```

### Test Suites (11/11 Passed):
- [`MySQLConnectionTest`](file:///c:/AstraLab%20Schema%20Designing/src/test/java/com/astralab/schema/MySQLConnectionTest.java): Verifies connection and existence of all 22 schema tables.
- [`SchemaMappingTest`](file:///c:/AstraLab%20Schema%20Designing/src/test/java/com/astralab/schema/SchemaMappingTest.java): Verifies Hibernate entity metamodel and JPA mappings against H2 (PostgreSQL dialect).
- [`SchemaInferenceServiceTest`](file:///c:/AstraLab%20Schema%20Designing/src/test/java/com/astralab/schema/service/SchemaInferenceServiceTest.java): Verifies column type detection (Integer, Decimal, Boolean, Date, DateTime, String), nullability, fallback on mixed types, and stream row sampling.
- [`DatasetControllerIntegrationTest`](file:///c:/AstraLab%20Schema%20Designing/src/test/java/com/astralab/schema/controller/DatasetControllerIntegrationTest.java): End-to-end integration tests for multipart file upload, schema generation, sample row preview, filtering by zone and owner, keyword search, and 400/404 error responses.

---

## Insomnia Testing & Dummy Data Guide

A comprehensive Insomnia collection and automatic dummy data seeder are included for immediate testing of all `DatasetController` endpoints without manual setup.

### 1. Preloaded Datasets & Sample Files
On application startup, [`DatasetDummyDataInitializer`](file:///c:/AstraLab%20Schema%20Designing/src/main/java/com/astralab/schema/config/DatasetDummyDataInitializer.java) seeds 3 realistic datasets with auto-inferred columns and writes their physical CSV files to disk:

| Dataset ID | Name | Zone | Owner | Workspace | Sample File |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `ds_customers_demo` | Global Customer Demographics | `gold` | `sarah_analyst` | `ws_analytics` | `customers_demo.csv` |
| `ds_ecommerce_orders` | E-Commerce Transaction Stream | `silver` | `david_finance` | `ws_finance` | `ecommerce_orders.csv` |
| `ds_sensor_telemetry` | Factory IoT Edge Telemetry | `landing` | `alex_engineer` | `ws_iot_platform` | `sensor_telemetry.csv` |

Sample files for testing manual multipart file uploads are located in [`data/samples/`](file:///c:/AstraLab%20Schema%20Designing/data/samples):
- `data/samples/sample_customers.csv`
- `data/samples/sample_ecommerce_orders.csv`
- `data/samples/sample_iot_telemetry.csv`

### 2. Importing into Insomnia
1. Open **Insomnia**.
2. Click **Preferences / Settings** or the **Collection Menu** -> **Import / Export**.
3. Select **Import Data** -> **From File**.
4. Choose [`insomnia_collection.json`](file:///c:/AstraLab%20Schema%20Designing/insomnia_collection.json) (or `insomnia_astralab_collection.json`).
5. The workspace **"AstraLab Dataset Metadata & Schema API"** will be created with pre-configured environment variables:
   - `base_url`: `http://localhost:8081`
   - `dataset_customer_id`: `ds_customers_demo`
   - `dataset_orders_id`: `ds_ecommerce_orders`
   - `dataset_telemetry_id`: `ds_sensor_telemetry`

### 3. Running the Server
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25.0.3"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
mvn spring-boot:run
```
Once the server is running on port 8081, execute the 9 preconfigured requests directly in Insomnia.

