package com.astralab.schema.repository;

import com.astralab.schema.entity.DatasetFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetFileRepository extends JpaRepository<DatasetFile, String> {
    List<DatasetFile> findByDatasetVersionId(String datasetVersionId);
    Optional<DatasetFile> findFirstByDatasetVersionIdOrderByUploadedAtDesc(String datasetVersionId);
}
