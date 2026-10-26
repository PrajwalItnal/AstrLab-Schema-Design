package com.astralab.schema.repository;

import com.astralab.schema.entity.DatasetFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetFileRepository extends JpaRepository<DatasetFile, String> {
}
