package com.mirrorfly.gateway.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mirrorfly.gateway.dto.ChatUserDto;
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
	@Query("Update ChatUser c set c.devicetype='',c.deviceToken='',c.voipDeviceToken='' where c.deviceToken = :deviceToken")
	public void updateDeviceTokenAsEmpty(String deviceToken);

	@Query(value = "SELECT username, mobilenumber, deviceToken from (SELECT CASE WHEN (CONVERT(AES_DECRYPT(from_base64(c.mobile_number), LEFT(SHA2(c.username, 256), 32), :iv) USING utf8)) IS NOT NULL "
			+ "THEN URL_DECODE(CONVERT(AES_DECRYPT(from_base64(c.mobile_number), LEFT(SHA2(c.username, 256), 32), :iv) USING utf8)) ELSE c.mobile_number END AS mobilenumber,"
			+ " c.username ,c.device_token as deviceToken FROM chat_users c)as tablename WHERE mobilenumber =:mobileNumber", nativeQuery = true)
	public ChatUserDto findByMobileNumber(@Param("mobileNumber") String mobileNumber, @Param("iv") String iv);

}
