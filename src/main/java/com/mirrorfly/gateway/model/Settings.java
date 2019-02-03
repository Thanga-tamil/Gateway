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
@Table(name = "chat_settings")
@Data
public class Settings implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6576324953564602096L;

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "domain")
	private String domain;

	@Column(name = "admin_user")
	private String adminUser;

	@Column(name = "secret_key")
	private String secretKey;

	@Column(name = "access_key")
	private String accessKey;

	@Column(name = "bucket_name")
	private String bucketName;

	@Column(name = "bucket_region")
	private String bucketRegion;

	@Column(name = "bucketVersion")
	private String bucketVersion;

	@Column(name = "videoLimit")
	private String videoLimit;

	@Column(name = "mail_login")
	private String mailLogin;

	@Column(name = "audio_limit")
	private String audioLimit;

	@Column(name = "recall_time")
	private String recallTime;

	@Column(name = "private_time")
	private String privateTime;

	@Column(name = "status")
	private String status;

	@Column(name = "feedback_from")
	private String feedbackFrom;

	@Column(name = "feedback_to")
	private String feedbackTo;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

}
