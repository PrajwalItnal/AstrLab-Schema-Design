package com.astralab.schema.controller;

import com.astralab.schema.dto.DatasetDetailDto;
import com.astralab.schema.dto.DatasetPreviewDto;
import com.astralab.schema.dto.DatasetSummaryDto;
import com.astralab.schema.service.DatasetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/datasets")
@CrossOrigin(origins = "*")
public class DatasetController {

    private final DatasetService datasetService;

    public DatasetController(DatasetService datasetService) {
        this.datasetService = datasetService;
    }

    /**
     * Upload and register a new dataset (CSV, XLSX, XLS).
     * Saves file persistently, registers metadata in PostgreSQL, and infers schema.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DatasetDetailDto> uploadDataset(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "zone", required = false, defaultValue = "landing") String zone,
            @RequestParam(value = "owner", required = false, defaultValue = "system") String owner,
            @RequestParam(value = "workspaceId", required = false) String workspaceId) {

        DatasetDetailDto created = datasetService.uploadAndRegisterDataset(
                file,
                name,
                description,
                zone,
                owner,
                workspaceId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * List and search datasets with filtering by zone, owner, keyword, and pagination.
     */
    @GetMapping
    public ResponseEntity<Page<DatasetSummaryDto>> listDatasets(
            @RequestParam(value = "zone", required = false) String zone,
            @RequestParam(value = "owner", required = false) String owner,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort) {

        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sortObj);

        Page<DatasetSummaryDto> result = datasetService.listDatasets(zone, owner, search, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * Get detailed metadata and schema columns for a dataset by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DatasetDetailDto> getDatasetById(@PathVariable("id") String id) {
        DatasetDetailDto detail = datasetService.getDatasetDetail(id);
        return ResponseEntity.ok(detail);
    }

    /**
     * Preview dataset columns, inferred data types, and streamed sample rows.
     */
    @GetMapping("/{id}/preview")
    public ResponseEntity<DatasetPreviewDto> getDatasetPreview(
            @PathVariable("id") String id,
            @RequestParam(value = "version", required = false) Integer version,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        DatasetPreviewDto preview = datasetService.getDatasetPreview(id, version, limit);
        return ResponseEntity.ok(preview);
    }

    private Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sortParam.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, property);
    }
}
