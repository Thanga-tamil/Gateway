package com.mirrorfly.gateway.model;

import java.time.Duration;
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
@Table(name = "chat_email_tokens")
public class ChatEmailTokens {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String email;

	private String otp;

	@Column(name = "verify_attempt")
	private Integer verifyAttempt = 0;

	@Column(name = "is_verified")
	private Integer isVerified = 0;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "expires_at")
	private Long expiresAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now(); // Set createdAt to the current time during insert
	}

	@PreUpdate
	protected void onUpdate() {
		this.createdAt = LocalDateTime.now(); // Update createdAt to current time during update
	}

	public boolean isOtpExpired() {
		return System.currentTimeMillis() > this.expiresAt;
	}

	public boolean hasMoreThanFiveMinutes() {
		long fiveMinutesInMillis = Duration.ofMinutes(5).toMillis();
		return (this.expiresAt - System.currentTimeMillis()) > fiveMinutesInMillis;
	}

}
