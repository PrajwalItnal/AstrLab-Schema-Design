package com.astralab.schema.repository;

import com.astralab.schema.entity.DatasetTeamAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetTeamAccessRepository extends JpaRepository<DatasetTeamAccess, String> {
}
