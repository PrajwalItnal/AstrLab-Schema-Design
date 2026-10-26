package com.astralab.schema.repository;

import com.astralab.schema.entity.ColumnProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ColumnProfileRepository extends JpaRepository<ColumnProfile, String> {
}
