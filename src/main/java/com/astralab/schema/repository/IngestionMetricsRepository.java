package com.astralab.schema.repository;

import com.astralab.schema.entity.IngestionMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngestionMetricsRepository extends JpaRepository<IngestionMetrics, String> {
}
