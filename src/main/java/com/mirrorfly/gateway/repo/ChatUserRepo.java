package com.mirrorfly.gateway.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.ChatUser;
import com.mirrorfly.gateway.model.RosterUser;

@Repository
public interface ChatUserRepo extends JpaRepository<ChatUser, String> {

	public ChatUser findByUsername(@Param("username") String username);

	public ChatUser findByUserIdentifier(@Param("userIdentifier") String userIdentifier);

	List<ChatUser> findBylicenseKey(String licenseKey);

	@Query("Select r from RosterUser r where r.rosterUserKey.username=:username")
	public List<RosterUser> getlistBylicenseKey(@Param(value = "username") String username);

}
