package com.astralab.schema.repository;

import com.astralab.schema.entity.DatasetOwnerHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetOwnerHistoryRepository extends JpaRepository<DatasetOwnerHistory, String> {
}
