package com.astralab.schema.repository;

import com.astralab.schema.entity.DatasetVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetVersionRepository extends JpaRepository<DatasetVersion, String> {
}
