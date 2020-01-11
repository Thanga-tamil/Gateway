package com.mirrorfly.gateway.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	/*
	 * @ManyToOne
	 * 
	 * @JoinColumn(name = "id") private UserRoles userRole;
	 */

}
