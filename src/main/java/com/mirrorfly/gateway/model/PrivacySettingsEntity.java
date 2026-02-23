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
@Table(name = "privacy_settings")
@Data
public class PrivacySettingsEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "username", nullable = false, length = 255)
	private String username;

	@Column(name = "seen_receipt")
	private Integer seenReceipt;

	@Column(name = "hide_profile")
	private Integer hideProfile;

	@Column(name = "online_status")
	private Integer onlineStatus;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

}
