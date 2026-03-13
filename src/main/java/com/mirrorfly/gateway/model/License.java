package com.mirrorfly.gateway.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "license")
@Data
public class License implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "license_id")
	private Long licenseId;

	@Column(name = "username")
	private String username;

	@Column(name = "sdk")
	private String sdk;

	@Column(name = "expire_date")
	private LocalDate expireDate;

	@Column(name = "activation_key")
	private String activationKey;

	@Column(name = "is_active")
	private Integer isActive;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

}
