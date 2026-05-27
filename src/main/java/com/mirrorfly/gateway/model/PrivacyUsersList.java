package com.mirrorfly.gateway.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "privacy_users_list")
@Data
public class PrivacyUsersList implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "username", length = 191)
	private String username;

	@Column(name = "target_user_id", length = 191)
	private String targetUserId;

	@Column(name = "privacy_type")
	private Integer privacyType;

	@Column(name = "device_id", length = 200)
	private String deviceId;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

}
