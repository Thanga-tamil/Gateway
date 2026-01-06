package com.mirrorfly.gateway.repo;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mirrorfly.gateway.model.ChatUserDetails;

@Repository
public interface ChatUsersDetailsRepo extends JpaRepository<ChatUserDetails, String> {

	public List<ChatUserDetails> findByUsername(@Param("username") String username);

	@Query("Select c from ChatUserDetails  c where c.username = :username and c.password = :password")
	public ChatUserDetails findByUsernameAndPassword(@Param("username") String username,
			@Param("password") String password);

	@Query("Select c from ChatUserDetails  c where c.username = :username and c.deviceId = :deviceId")
	public ChatUserDetails findByUsernameAndDeviceId(@Param("username") String username,
			@Param("deviceId") String deviceId);

	@Transactional
	@Modifying
	@Query("Update ChatUserDetails cud set cud.password = :password where cud.username = :username and cud.deviceId = :deviceId")
	public void updatePasswordByUsernameAndDeviceId(@Param("password") String password,
			@Param("username") String username, @Param("deviceId") String deviceId);

	@Query("Select c from ChatUserDetails  c where c.username = :username and c.deviceType = 'web'")
	public List<ChatUserDetails> findByUsernameAndDeviceTypeWeb(@Param("username") String username);

	@Query("Select c from ChatUserDetails  c where c.username = :username and (c.deviceType = 'android' or c.deviceType = 'ios')")
	public List<ChatUserDetails> findByUsernameAndDeviceTypeAndroidIos(@Param("username") String username);

	@Transactional
	@Modifying
	@Query("Update ChatUserDetails c set c.deviceToken='' where c.deviceToken = :deviceToken")
	public void updateDeviceTokenAsEmpty(String deviceToken);

	@Transactional
	@Modifying
	@Query("Update ChatUserDetails c set c.voipDeviceToken='' where c.voipDeviceToken = :voipDeviceToken")
	public void updateVoipDeviceTokenAsEmpty(String voipDeviceToken);

    @Transactional
    @Modifying
    @Query("Update ChatUserDetails c set c.userIp = :userIp, c.countryCode = :countryCode where c.username = :username and c.deviceId = :deviceId")
    int updateIpWithDeviceId(@Param("userIp") String userIp, @Param("deviceId") String deviceId, @Param("username") String username, @Param("countryCode") String countryCode);

	@Transactional
	@Modifying
	@Query("Update ChatUserDetails c set c.userIp = :userIp, c.countryCode = :countryCode, c.deviceId = :deviceId where c.username = :username and c.socketId = :socketId")
	int updateIpWithSocketIdAndDeviceId(@Param("userIp") String ip,  @Param("deviceId") String deviceId, @Param("username") String username, @Param("countryCode") String countryCode, @Param("socketId")String socketId);

	@Transactional
	@Modifying
	@Query("Update ChatUserDetails c set c.userIp = :userIp, c.countryCode = :countryCode where c.username = :username and c.socketId = :socketId")
	int updateIpWithSocketId(@Param("userIp") String ip, @Param("socketId")String socketId, @Param("username") String username, @Param("countryCode") String countryCode);
}