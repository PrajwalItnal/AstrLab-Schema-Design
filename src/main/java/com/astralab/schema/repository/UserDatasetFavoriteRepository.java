package com.astralab.schema.repository;

import com.astralab.schema.entity.UserDatasetFavorite;
import com.astralab.schema.entity.UserDatasetFavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDatasetFavoriteRepository extends JpaRepository<UserDatasetFavorite, UserDatasetFavoriteId> {
}
