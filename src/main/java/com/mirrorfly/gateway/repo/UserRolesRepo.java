package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.UserRoles;

@Repository
public interface UserRolesRepo extends JpaRepository<UserRoles, Integer> {

	@Query("Select v  from  UserRoles v where v.roleId=:roleId")
	public UserRoles findByRoleId(Integer roleId);
}
