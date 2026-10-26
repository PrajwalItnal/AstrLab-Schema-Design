package com.astralab.schema.repository;

import com.astralab.schema.entity.DatasetColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetColumnRepository extends JpaRepository<DatasetColumn, String> {
}
