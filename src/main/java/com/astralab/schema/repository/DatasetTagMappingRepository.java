package com.astralab.schema.repository;

import com.astralab.schema.entity.DatasetTagMapping;
import com.astralab.schema.entity.DatasetTagMappingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetTagMappingRepository extends JpaRepository<DatasetTagMapping, DatasetTagMappingId> {
}
