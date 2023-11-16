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
@Table(name = "chat_otp")
@Data
public class Otp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6576324953564602096L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "otp")
	private String otpNo;

	@Column(name = "username")
	private String username;

	@Column(name = "mobile_number")
	private String mobileNumber;

	@Column(name = "status")
	private Integer status;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

}
