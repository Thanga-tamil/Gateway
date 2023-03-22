package com.mirrorfly.gateway.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "chat_users")
@Data
public class ChatUser implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6576324953564602096L;

	@Id
	@Column(name = "username")
	private String username;

	@Column(name = "email")
	private String emailId;

	@Column(name = "name")
	private String name;

	@Column(name = "status_msg")
	private String statusMsg;

	@Column(name = "country_code")
	private String countryCode;

	@Column(name = "mobile_number")
	private String mobileNumber;

	@Column(name = "resource")
	private String resource;

	@Column(name = "notification")
	private String notification;

	@Column(name = "image")
	private String image;

	@Column(name = "status")
	private String status;

	@Column(name = "is_block")
	private Integer isBlock = 0;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "license_key")
	private String licenseKey;

	@Column(name = "user_identifier")
	private String userIdentifier;

	@Column(name = "is_delete_inprogrees")
	private Integer isDeleteInprogrees;
	
	@Column(columnDefinition = "JSON", name = "metadata")
	private String metaData;

}
