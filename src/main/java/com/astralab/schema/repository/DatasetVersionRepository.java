package com.astralab.schema.repository;

import com.astralab.schema.entity.DatasetVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetVersionRepository extends JpaRepository<DatasetVersion, String> {
    Optional<DatasetVersion> findByDatasetIdAndVersionNumber(String datasetId, Integer versionNumber);
    Optional<DatasetVersion> findFirstByDatasetIdOrderByVersionNumberDesc(String datasetId);
    List<DatasetVersion> findByDatasetIdOrderByVersionNumberDesc(String datasetId);
}
