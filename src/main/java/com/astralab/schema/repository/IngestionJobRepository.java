package com.astralab.schema.repository;

import com.astralab.schema.entity.IngestionJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngestionJobRepository extends JpaRepository<IngestionJob, String> {
}
