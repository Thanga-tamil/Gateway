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

@Entity
@Table(name = "chat_servers")
@Data
public class ChatServerConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6576324953564602096L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer callId;

	@Column(name = "config_id")
	private Integer configId;

	@Column(name = "url")
	private String url;

	@Column(name = "username")
	private String username;

	@Column(name = "password")
	private String password;

	@Column(name = "is_turn")
	private Integer isTurn;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

}
