package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.Users;

@Repository
public interface UserRepo extends JpaRepository<Users, String> {

	public Users findByUsername(@Param("username") String username);

}
