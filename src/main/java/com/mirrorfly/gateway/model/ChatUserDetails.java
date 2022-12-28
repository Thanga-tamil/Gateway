package com.mirrorfly.gateway.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "chat_users_details")
@Data
public class ChatUserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private String id;
	
	@Column(name = "username")
	private String username;

	@Column(name = "device_id")
	private String deviceId = "";

	@Column(name = "device_token")
	private String deviceToken = "";

	@Column(name = "voip_device_token")
	private String voipDeviceToken = "";

	@Column(name = "mode")
	private Integer mode = 1;

	@Column(name = "device_type")
	private String deviceType;

	@Column(name = "device_os_version")
	private String deviceOsVersion = "";
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "last_logged_in_at")
	private LocalDateTime lastLoggedInAt;
	
	@Column(name = "created_at")
	private LocalDateTime createdAt;

}
