package com.astralab.schema.repository;

import com.astralab.schema.entity.DatasetTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetTagRepository extends JpaRepository<DatasetTag, String> {
}
