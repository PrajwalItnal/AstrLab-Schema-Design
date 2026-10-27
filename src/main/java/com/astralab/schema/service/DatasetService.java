package com.astralab.schema.service;

import com.astralab.schema.dto.*;
import com.astralab.schema.entity.*;
import com.astralab.schema.exception.InvalidFileException;
import com.astralab.schema.exception.ResourceNotFoundException;
import com.astralab.schema.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final DatasetVersionRepository datasetVersionRepository;
    private final DatasetFileRepository datasetFileRepository;
    private final DatasetColumnRepository datasetColumnRepository;
    private final AppUserRepository appUserRepository;
    private final WorkspaceRepository workspaceRepository;
    private final TenantRepository tenantRepository;
    private final FileStorageService fileStorageService;
    private final SchemaInferenceService schemaInferenceService;

    public DatasetService(DatasetRepository datasetRepository,
                          DatasetVersionRepository datasetVersionRepository,
                          DatasetFileRepository datasetFileRepository,
                          DatasetColumnRepository datasetColumnRepository,
                          AppUserRepository appUserRepository,
                          WorkspaceRepository workspaceRepository,
                          TenantRepository tenantRepository,
                          FileStorageService fileStorageService,
                          SchemaInferenceService schemaInferenceService) {
        this.datasetRepository = datasetRepository;
        this.datasetVersionRepository = datasetVersionRepository;
        this.datasetFileRepository = datasetFileRepository;
        this.datasetColumnRepository = datasetColumnRepository;
        this.appUserRepository = appUserRepository;
        this.workspaceRepository = workspaceRepository;
        this.tenantRepository = tenantRepository;
        this.fileStorageService = fileStorageService;
        this.schemaInferenceService = schemaInferenceService;
    }

    @Transactional
    public DatasetDetailDto uploadAndRegisterDataset(MultipartFile file,
                                                    String name,
                                                    String description,
                                                    String zone,
                                                    String owner,
                                                    String workspaceId) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Uploaded file cannot be null or empty.");
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "dataset";
        String fileFormat = schemaInferenceService.detectFormat(originalFilename);
        if ("UNKNOWN".equals(fileFormat)) {
            throw new InvalidFileException("Unsupported file type for '" + originalFilename + "'. Supported formats: .csv, .xlsx, .xls");
        }

        String datasetName = (name != null && !name.trim().isEmpty())
                ? name.trim()
                : stripExtension(originalFilename);

        String effectiveZone = (zone != null && !zone.trim().isEmpty()) ? zone.trim() : "default";

        // 1. Generate unique Dataset ID
        String datasetId = "ds_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        // 2. Store file on persistent storage
        FileStorageService.StoredFileInfo storedFileInfo = fileStorageService.storeFile(file, datasetId);
        Path storedFilePath = fileStorageService.getFilePath(storedFileInfo.getAbsolutePath());

        // 3. Infer Schema & extract initial sample
        InferredSchemaResult inferred = schemaInferenceService.inferSchemaAndSample(storedFilePath, fileFormat, 10);

        // 4. Resolve / provision Owner and Workspace
        AppUser ownerUser = resolveOrCreateUser(owner);
        Workspace workspace = resolveOrCreateWorkspace(workspaceId, ownerUser);

        OffsetDateTime now = OffsetDateTime.now();

        // 5. Persist Dataset
        Dataset dataset = new Dataset(datasetId);
        dataset.setName(datasetName);
        dataset.setDescription(description);
        dataset.setZone(effectiveZone);
        dataset.setOwnerUser(ownerUser);
        dataset.setWorkspace(workspace);
        dataset.setStatus("ACTIVE");
        dataset.setVisibility("INTERNAL");
        dataset.setCurrentVersion(1);
        dataset.setCreatedAt(now);
        dataset.setUpdatedAt(now);
        datasetRepository.save(dataset);

        // 6. Persist DatasetVersion
        String versionId = "ver_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        DatasetVersion version = new DatasetVersion(versionId);
        version.setDataset(dataset);
        version.setVersionNumber(1);
        version.setStatus("AVAILABLE");
        version.setSchemaHash(storedFileInfo.getChecksumSha256());
        version.setCreatedByUser(ownerUser);
        version.setCreatedAt(now);
        datasetVersionRepository.save(version);

        // 7. Persist DatasetFile
        String fileId = "file_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        DatasetFile datasetFile = new DatasetFile(fileId);
        datasetFile.setDatasetVersion(version);
        datasetFile.setUploadedByUser(ownerUser);
        datasetFile.setOriginalFilename(originalFilename);
        datasetFile.setFileFormat(fileFormat);
        datasetFile.setMimeType(file.getContentType());
        datasetFile.setFileSizeBytes(storedFileInfo.getSizeBytes());
        datasetFile.setStorageBucket("local");
        datasetFile.setObjectKey(storedFileInfo.getRelativePath());
        datasetFile.setChecksum(storedFileInfo.getChecksumSha256());
        datasetFile.setChecksumAlgorithm("SHA-256");
        datasetFile.setUploadedAt(now);
        datasetFileRepository.save(datasetFile);

        // 8. Persist Inferred DatasetColumns
        List<DatasetColumn> columnsToSave = new ArrayList<>();
        for (ColumnSchemaDto colDto : inferred.getColumns()) {
            String colId = "col_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            DatasetColumn col = new DatasetColumn(colId);
            col.setDatasetVersion(version);
            col.setColumnName(colDto.getColumnName());
            col.setLogicalDataType(colDto.getLogicalDataType());
            col.setSourceDataType(colDto.getSourceDataType());
            col.setOrdinalPosition(colDto.getOrdinalPosition());
            col.setNullable(colDto.getNullable());
            col.setFormatPattern(colDto.getFormatPattern());
            col.setCreatedAt(now);
            columnsToSave.add(col);
        }
        datasetColumnRepository.saveAll(columnsToSave);

        // 9. Build and return detail response
        return toDetailDto(dataset, version, datasetFile, inferred.getColumns());
    }

    @Transactional(readOnly = true)
    public DatasetPreviewDto getDatasetPreview(String datasetId, Integer versionNumber, int limit) {
        int effectiveLimit = Math.max(1, Math.min(limit, 100)); // bound preview between 1 and 100

        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found with ID: " + datasetId));

        int targetVersion = (versionNumber != null) ? versionNumber : (dataset.getCurrentVersion() != null ? dataset.getCurrentVersion() : 1);

        DatasetVersion version = datasetVersionRepository.findByDatasetIdAndVersionNumber(datasetId, targetVersion)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset version " + targetVersion + " not found for dataset: " + datasetId));

        DatasetFile datasetFile = datasetFileRepository.findFirstByDatasetVersionIdOrderByUploadedAtDesc(version.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dataset file reference missing for version: " + version.getId()));

        if (!fileStorageService.fileExists(datasetFile.getObjectKey())) {
            throw new ResourceNotFoundException("Dataset physical file is unavailable on storage: " + datasetFile.getObjectKey());
        }

        Path filePath = fileStorageService.getFilePath(datasetFile.getObjectKey());

        List<DatasetColumn> columns = datasetColumnRepository.findByDatasetVersionIdOrderByOrdinalPositionAsc(version.getId());
        List<ColumnSchemaDto> columnDtos = columns.stream()
                .map(this::toColumnDto)
                .collect(Collectors.toList());

        List<String> columnNames = columnDtos.stream().map(ColumnSchemaDto::getColumnName).collect(Collectors.toList());

        // Stream and read ONLY the first 'effectiveLimit' rows from disk
        List<Map<String, Object>> previewRows = schemaInferenceService.readSampleRows(
                filePath,
                datasetFile.getFileFormat(),
                columnNames,
                effectiveLimit
        );

        DatasetPreviewDto previewDto = new DatasetPreviewDto();
        previewDto.setDatasetId(dataset.getId());
        previewDto.setDatasetName(dataset.getName());
        previewDto.setDescription(dataset.getDescription());
        previewDto.setZone(dataset.getZone());
        previewDto.setOwner(dataset.getOwnerUser() != null ? dataset.getOwnerUser().getUsername() : null);
        previewDto.setVersionNumber(version.getVersionNumber());
        previewDto.setOriginalFilename(datasetFile.getOriginalFilename());
        previewDto.setFileFormat(datasetFile.getFileFormat());
        previewDto.setFileSizeBytes(datasetFile.getFileSizeBytes());
        previewDto.setTotalColumns(columnDtos.size());
        previewDto.setSampleRowCount(previewRows.size());
        previewDto.setColumns(columnDtos);
        previewDto.setRows(previewRows);

        return previewDto;
    }

    @Transactional(readOnly = true)
    public Page<DatasetSummaryDto> listDatasets(String zone, String owner, String search, Pageable pageable) {
        Specification<Dataset> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude soft-deleted datasets
            predicates.add(cb.isNull(root.get("deletedAt")));

            // Filter by zone
            if (zone != null && !zone.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("zone")), zone.trim().toLowerCase()));
            }

            // Filter by owner (supports matching by username or owner user id)
            if (owner != null && !owner.trim().isEmpty()) {
                String ownerPattern = owner.trim().toLowerCase();
                Predicate ownerUsername = cb.equal(cb.lower(root.get("ownerUser").get("username")), ownerPattern);
                Predicate ownerId = cb.equal(cb.lower(root.get("ownerUser").get("id")), ownerPattern);
                predicates.add(cb.or(ownerUsername, ownerId));
            }

            // Search by dataset name or description
            if (search != null && !search.trim().isEmpty()) {
                String likePattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), likePattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), likePattern);
                predicates.add(cb.or(nameMatch, descMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Dataset> datasets = datasetRepository.findAll(spec, pageable);
        return datasets.map(this::toSummaryDto);
    }

    @Transactional(readOnly = true)
    public DatasetDetailDto getDatasetDetail(String datasetId) {
        Dataset dataset = datasetRepository.findById(datasetId)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found with ID: " + datasetId));

        int currentVersionNumber = dataset.getCurrentVersion() != null ? dataset.getCurrentVersion() : 1;
        DatasetVersion version = datasetVersionRepository.findByDatasetIdAndVersionNumber(datasetId, currentVersionNumber)
                .orElse(null);

        DatasetFile datasetFile = (version != null)
                ? datasetFileRepository.findFirstByDatasetVersionIdOrderByUploadedAtDesc(version.getId()).orElse(null)
                : null;

        List<ColumnSchemaDto> columnDtos = (version != null)
                ? datasetColumnRepository.findByDatasetVersionIdOrderByOrdinalPositionAsc(version.getId()).stream().map(this::toColumnDto).collect(Collectors.toList())
                : Collections.emptyList();

        return toDetailDto(dataset, version, datasetFile, columnDtos);
    }

    // -------------------------------------------------------------
    // HELPERS & MAPPINGS
    // -------------------------------------------------------------
    private DatasetSummaryDto toSummaryDto(Dataset dataset) {
        DatasetSummaryDto dto = new DatasetSummaryDto();
        dto.setId(dataset.getId());
        dto.setName(dataset.getName());
        dto.setDescription(dataset.getDescription());
        dto.setZone(dataset.getZone());
        dto.setOwner(dataset.getOwnerUser() != null ? dataset.getOwnerUser().getUsername() : null);
        dto.setStatus(dataset.getStatus());
        dto.setCurrentVersion(dataset.getCurrentVersion());
        dto.setCreatedAt(dataset.getCreatedAt());
        dto.setUpdatedAt(dataset.getUpdatedAt());

        // Attach file metadata if present
        if (dataset.getCurrentVersion() != null) {
            datasetVersionRepository.findByDatasetIdAndVersionNumber(dataset.getId(), dataset.getCurrentVersion())
                    .ifPresent(v -> datasetFileRepository.findFirstByDatasetVersionIdOrderByUploadedAtDesc(v.getId())
                            .ifPresent(f -> {
                                dto.setOriginalFilename(f.getOriginalFilename());
                                dto.setFileFormat(f.getFileFormat());
                                dto.setFileSizeBytes(f.getFileSizeBytes());
                            }));
        }

        return dto;
    }

    private DatasetDetailDto toDetailDto(Dataset dataset, DatasetVersion version, DatasetFile file, List<ColumnSchemaDto> columns) {
        DatasetDetailDto dto = new DatasetDetailDto();
        dto.setId(dataset.getId());
        dto.setName(dataset.getName());
        dto.setDescription(dataset.getDescription());
        dto.setZone(dataset.getZone());
        dto.setOwner(dataset.getOwnerUser() != null ? dataset.getOwnerUser().getUsername() : null);
        dto.setStatus(dataset.getStatus());
        dto.setCurrentVersion(dataset.getCurrentVersion());
        dto.setVisibility(dataset.getVisibility());
        dto.setWorkspaceId(dataset.getWorkspace() != null ? dataset.getWorkspace().getId() : null);
        dto.setCreatedAt(dataset.getCreatedAt());
        dto.setUpdatedAt(dataset.getUpdatedAt());

        if (file != null) {
            dto.setOriginalFilename(file.getOriginalFilename());
            dto.setFileFormat(file.getFileFormat());
            dto.setFileSizeBytes(file.getFileSizeBytes());
            dto.setStoragePath(file.getObjectKey());
            dto.setChecksum(file.getChecksum());
        }

        dto.setColumns(columns);
        return dto;
    }

    private ColumnSchemaDto toColumnDto(DatasetColumn col) {
        return new ColumnSchemaDto(
                col.getColumnName(),
                col.getSourceDataType(),
                col.getLogicalDataType(),
                col.getOrdinalPosition(),
                col.getNullable(),
                col.getFormatPattern()
        );
    }

    private AppUser resolveOrCreateUser(String usernameOrId) {
        String username = (usernameOrId != null && !usernameOrId.trim().isEmpty())
                ? usernameOrId.trim()
                : "default_user";

        return appUserRepository.findByUsername(username)
                .orElseGet(() -> appUserRepository.findById(username).orElseGet(() -> {
                    Tenant tenant = resolveOrCreateTenant("default-tenant");
                    AppUser user = new AppUser("user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
                    user.setUsername(username);
                    user.setKeycloakId("kc_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
                    user.setEmail(username + "@astralab.io");
                    user.setDisplayName(username);
                    user.setStatus("ACTIVE");
                    user.setTenant(tenant);
                    user.setCreatedAt(OffsetDateTime.now());
                    return appUserRepository.save(user);
                }));
    }

    private Workspace resolveOrCreateWorkspace(String workspaceId, AppUser user) {
        String wsId = (workspaceId != null && !workspaceId.trim().isEmpty()) ? workspaceId.trim() : "default-workspace";
        return workspaceRepository.findById(wsId)
                .orElseGet(() -> {
                    Tenant tenant = user.getTenant() != null ? user.getTenant() : resolveOrCreateTenant("default-tenant");
                    Workspace ws = new Workspace(wsId);
                    ws.setName("Default Workspace");
                    ws.setDescription("Auto-created default workspace");
                    ws.setStatus("ACTIVE");
                    ws.setTenant(tenant);
                    ws.setCreatedByUser(user);
                    ws.setCreatedAt(OffsetDateTime.now());
                    return workspaceRepository.save(ws);
                });
    }

    private Tenant resolveOrCreateTenant(String tenantId) {
        return tenantRepository.findById(tenantId).orElseGet(() -> {
            Tenant t = new Tenant(tenantId);
            t.setName("Default Tenant");
            t.setPlan("ENTERPRISE");
            t.setEnvironment("PRODUCTION");
            t.setCreatedAt(OffsetDateTime.now());
            return tenantRepository.save(t);
        });
    }

    private String stripExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx > 0 ? filename.substring(0, idx) : filename;
    }
}
