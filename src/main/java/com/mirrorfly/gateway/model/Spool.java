package com.mirrorfly.gateway.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import lombok.Data;

@Entity
@Table(name = "spool")
@Data
public class Spool implements Serializable {

	private static final long serialVersionUID = 6576324953564602096L;

	@Id
	@Column(name = "id")
	private Integer id;

	@Column(name = "username")
	private String username;

	@Column(name = "message_id")
	private String messageId;

	@Column(name = "chat_type")
	private String chatType;

	@Column(name = "device_id")
	private String deviceId;

	@Column(name = "created_at")
	@DateTimeFormat(iso = ISO.DATE_TIME)
	private LocalDateTime createdAt;

}
