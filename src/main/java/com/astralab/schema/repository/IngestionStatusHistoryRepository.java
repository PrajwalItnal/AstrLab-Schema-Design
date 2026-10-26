package com.astralab.schema.repository;

import com.astralab.schema.entity.IngestionStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngestionStatusHistoryRepository extends JpaRepository<IngestionStatusHistory, Long> {
}
