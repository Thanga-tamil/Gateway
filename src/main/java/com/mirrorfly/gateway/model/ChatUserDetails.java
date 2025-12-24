package com.mirrorfly.gateway.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

	@Column(name = "socket_id")
	private String socketId;

	@Column(name = "device_os_version")
	private String deviceOsVersion = "";

	@Column(name = "password")
	private String password;

	@Column(name = "push_server_type")
	private String pushServerType;

	@Column(name = "last_logged_in_at")
	private LocalDateTime lastLoggedInAt;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "user_type")
	private String userType;

    @Column(name = "user_ip", length = 45)
    private String userIp;

    @Column(name = "country_code", length = 45)
    private String countryCode;

}
