package com.astralab.schema.repository;

import com.astralab.schema.entity.DatasetUserAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetUserAccessRepository extends JpaRepository<DatasetUserAccess, String> {
}
