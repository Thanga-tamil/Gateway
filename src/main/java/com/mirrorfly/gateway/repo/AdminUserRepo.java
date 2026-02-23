package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.AdminUser;

@Repository
public interface AdminUserRepo extends JpaRepository<AdminUser, String> {

	@Query("Select u from AdminUser u where u.username = :username and u.isDeleted = 0")
	public AdminUser findByUsername(@Param(value = "username") String username);

	@Query("Select u from AdminUser u where u.emailId = :emailId and u.isDeleted = 0")
	public AdminUser findByEmailId(@Param(value = "emailId") String emailId);

}
