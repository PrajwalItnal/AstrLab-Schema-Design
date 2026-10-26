package com.astralab.schema.repository;

import com.astralab.schema.entity.UserGroupMember;
import com.astralab.schema.entity.UserGroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupMemberRepository extends JpaRepository<UserGroupMember, UserGroupMemberId> {
}
