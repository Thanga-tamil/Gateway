package com.mirrorfly.gateway.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "users_admin")
@Data
public class AdminUser {

	@Id
	@Column(name = "username")
	private String username;

	@Column(name = "name")
	private String name;

	@Column(name = "email")
	private String emailId;

	@Column(name = "password")
	private String password;

	@Column(name = "is_active")
	private Integer isActive;

	@Column(name = "remember_token")
	private String rememberToken;

	@Column(name = "image")
	private String image;

	@Column(name = "hashed_password")
	private String hashedPassword;

	@Column(name = "salt_password")
	private String saltPassword;

	@Column(name = "role_id")
	private Integer roleId;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "is_deleted")
	private Integer isDeleted;

}
