package com.mirrorfly.gateway.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mirrorfly.gateway.model.ChatUser;
import com.mirrorfly.gateway.model.RosterUser;

@Repository
public interface ChatUserRepo extends JpaRepository<ChatUser, String> {

	public ChatUser findByUsername(@Param("username") String username);

	public ChatUser findByUserIdentifier(@Param("userIdentifier") String userIdentifier);

	List<ChatUser> findBylicenseKey(String licenseKey);

	@Query("Select r from RosterUser r where r.rosterUserKey.username=:username")
	public List<RosterUser> getlistBylicenseKey(@Param(value = "username") String username);

	@Transactional
	@Modifying
	@Query("Update ChatUser c set c.devicetype='',c.mode='',c.deviceToken='',c.voipDeviceToken='' where c.deviceToken = :deviceToken")
	public void updateDeviceTokenAsEmpty(String deviceToken);

}
