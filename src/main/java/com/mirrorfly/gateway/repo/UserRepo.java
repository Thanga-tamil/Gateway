package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mirrorfly.gateway.model.Users;

@Repository
public interface UserRepo extends JpaRepository<Users, String> {

	public Users findByUsername(@Param("username") String username);

	@Transactional
	@Modifying
	@Query("Update Users u set u.password = :password where u.username = :username")
	public void updatePasswordByUsername(@Param("password") String password, @Param("username") String username); 
}
