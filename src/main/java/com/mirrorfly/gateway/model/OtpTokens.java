package com.mirrorfly.gateway.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "otp_tokens")
public class OtpTokens {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String email;

	private String otp;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	private Integer attempts = 0;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now(); // Set createdAt to the current time during insert
	}

	@PreUpdate
	protected void onUpdate() {
		this.createdAt = LocalDateTime.now(); // Update createdAt to current time during update
	}

}
