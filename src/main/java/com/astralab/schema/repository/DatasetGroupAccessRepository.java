package com.astralab.schema.repository;

import com.astralab.schema.entity.DatasetGroupAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetGroupAccessRepository extends JpaRepository<DatasetGroupAccess, String> {
}
