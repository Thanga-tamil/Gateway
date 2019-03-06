package com.mirrorfly.gateway.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * A container class to store and retrieve informations from database tables.
 * This include token details with token expire time limit range.
 *
 */

@Entity
@Table(name = "chat_access_tokens")
@Data
public class TokenDetail implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6576324953564602096L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "username")
	private String username;

	@Column(name = "jid")
	private String jid;

	@Column(name = "token")
	private String token;

	@Column(name = "token_expiry_time")
	private LocalDateTime tokenExpiryTime;

	@Column(name = "device_type")
	private String devicetype;

	@Column(name = "client_ip")
	private String clientIp;

	@Column(name = "scopes")
	private String scopes;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "remember_token")
	private String rememberToken;

}
