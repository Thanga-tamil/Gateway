package com.mirrorfly.gateway.model;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "license")
@Data
public class License {

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
